package bravo.file;

import bravo.filter.EncryptedFileComparator;
import bravo.environment.RootFolder;
import bravo.filter.FileFilter;
import bravo.filter.DirectoryFilter;
import bravo.environment.CacheCleaner;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.nio.file.attribute.FileTime;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import org.bouncycastle.util.encoders.Base64;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import net.lingala.zip4j.model.FileHeader;
import static bravo.file.AESCipher.IV_LENGTH;
import static bravo.file.AESCipher.BUFFER_SIZE;
import static bravo.file.FileOperation.ADD;
import static bravo.file.FileOperation.REMOVE;
import static bravo.file.FileOperation.EXTRACT;
import static bravo.file.FileOperation.ENCRYPT;
import bravo.utils.ArrayUtils;
import static bravo.utils.ArrayUtils.byteArrayToInt;
import static bravo.utils.ArrayUtils.intToByteArray;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import static bravo.file.AESCipher.KEY_LENGTH;
import java.security.SecureRandom;

/**
 * Classe para gerenciamento do arquivo criptografado. Um arquivo criptografado
 * é um arquivo no formato <i>ZIP</i>, sem compressão, e com uma estrutura 
 * pré-definida, a saber:<br><br>
 * 
 * <b>Diretório METADATA:</b>
 * 
 * <br><br>
 * 
 * No diretório METADATA estão todos os metadados do arquivo. Este diretório
 * contém os seguintes arquivos:
 * 
 * <br><br>
 * 
 * <ul>
 * 
 * <li><b>METADATA/FileTable:</b> Tabela de arquivos. Contém as informações sobre
 * todos os arquivos criptografados contidos na pasta ZIP, incluindo o nome do 
 * arquivo, tamanho do arquivo, data de criação, data de alteração. Todos estes 
 * dados estão criptografados para não se expor informações sensíveis.</li>
 * 
 * <br>
 * 
 * <li><b>METADATA/Test:</b> Contém os dados para a validação da senha do arquivo.
 * A senha é validada com base no hash gerado da mesma, com o uso de SALT para
 * assegurar que sempre serão gerados hash's diferentes, mesmo que se utilize a
 * mesma senha para diversos arquivos.</li>

 * <br>
 * 
 * <li><b>METADATA/Index:</b> Contém o número sequencial do último arquivo que foi
 * adicionado. Cada novo arquivo inserido no ZIP inclementa este número. Este é
 * o responsável por controlar o padrão de nome genérico FileXXXXXXX com que os
 * arquivos criptografados são nomeados.</li>
 * 
 * <br> 
 * 
 * <li><b>METADATA/Version: </b> Contém o número da versão do arquivo bravo que
 * foi realizada a encriptação.</li>
 * 
 * </ul>
 * 
 * <br>
 * 
 * <b>Arquivos Criptografados:</b>
 * 
 * <br><br>
 * 
 * Os arquivos criptografados são todos os arquivos nomeados como FileXXXXXXX. A 
 * sequência XXXXXXX inicia-se em 1 e é inclementada de uma unidade a cada novo
 * arquivo inserido no ZIP. Os nomes reais dos arquivos estão no arquivo de 
 * metadados FileTable, que está criptografado para proteger estas informações.
 * 
 * <br><br>
 * 
 * Exemplo:
 * 
 * <br>
 * 
 * <ul>
 * 
 * <li><b>File0000001</b><br></li>
 * <li><b>File0000002</b><br></li>
 * <li><b>File0000003</b><br></li>
 * <li><b>File0000004</b><br></li>
 * <li><b>File0000005</b><br></li>
 * <li><b>File0000006</b><br></li>
 * <li><b>...</b><br></li>
 * <li><b>File9999999</b><br></li>
 * 
 * </ul>
 * 
 * <br>
 * 
 * @since 1.0
 */
public final class BravoFile extends ZipFile implements CipherListener {
    
    
    /**Versão 1.0*/
    public static final int VERSION_1 = 1;
    
    /**Tamanho do SALT usado para gerar o hash da senha (512 bits).*/
    private final int SALT_LENGTH = KEY_LENGTH * 2;
    
    /**Caracteres inválidos para o nome de arquivos e diretórios.*/
    public static final char[] INVALID_FILE_NAME_CHARACTERS = new char[]{
        '<', '>', ':', '"', '/', '\\', '|', '?', '*', // Windows
        '\0' // Linux
    };

    /**Pasta interna para arquivos de metadados.*/
    private final String METADATA_FOLDER = "METADATA" + FILE_SEPARATOR;
    
    /**Arquivo contendo os metadados dos arquivos criptografados.*/
    private final String FILE_TABLE_NAME = METADATA_FOLDER + "FileTable";
    
    /**Arquivo contendo os bytes para validação da senha do arquivo.*/
    private final String TEST_FILE_NAME = METADATA_FOLDER + "Test";
    
    /**Arquivo contendo os bytes do contador de arquivos criptografados.*/
    private final String INDEX_FILE_NAME = METADATA_FOLDER + "Index";
    
    /**Arquivo contendo os bytes da versão do arquivo.*/
    private final String VERSION_FILE_NAME = METADATA_FOLDER + "Version";
    
    /**TAG para identificação de pasta vazia.*/
    private final String EMPTY_FOLDER_TAG = "[EMPTY_FOLDER_TAG]";
    
    /**Cabeçalhos de arquivos criptografados.*/
    private final ArrayList<EncryptedFileMetadata> fileMetadataList;
    
    /**Ouvintes do processamento de arquivos (inserção/remoção/extração).*/
    private final List<ProcessListener> listeners;
    
    /**Lista de pastas de arquivos.*/
    private final List<String> folders;
    
    /**Hash da senha para encriptação/decriptação de arquivos.*/
    private final byte[] passwordHash;
    
    /**Versão do arquivo.*/
    private int version;
    
    /**Pasta selecionada como raiz para inserção de novos arquivos ao ZIP.*/
    private String rootFolder;
    
    /**Contador sequêncial de arquivos criptografados.*/
    private int internalFileNameIndex = 0;
    
    /**Percentual de processamento do arquivo atual.*/
    private int filePercentage;
    
    /**Percentual total do processamento.*/
    private int totalPercentage;
    
    /**Número total de bytes do arquivo em processamento.*/
    private long fileLength;
    
    /**Número total de bytes em processamento.*/
    private long totalLength;
    
    /**Número de bytes processados do arquivo.*/
    private long fileBytesCounter;
    
    /**Número total de bytes processados.*/
    private long totalBytesCounter;
    
    /**Controle de interrupção do processamento de arquivos.*/
    private boolean abort;
    
    /**Bloqueio para interrupção do processamento de arquivos.*/
    private boolean blockAbort;
    
    /**Instância de FileChannel para bloqueio do arquivo.*/
    private FileChannel fileChannel;
    
    /**Instância de FileLock para bloqueio do arquivo.*/
    private FileLock fileLock;

   
    /**
     * Constructor para a geração de um novo arquivo. Use quando ainda não
     * existir um arquivo e este precisa ser criado.
     * 
     * <br><br>
     * 
     * <b>Obs.:</b> Se o arquivo já existir,ele será sobrescrito, fazendo com
     * que o anterior seja perdido.
     * 
     * @param file arquivo a ser criado.
     * @param password senha do arquivo.
     * @param params parâmetros para o algoritmo Argon2.
     * @param seed semente para o gerador de números pseudo-aleatórios.
     * @throws Exception 
     */
    public BravoFile(File file, char[] password, Argon2Params params, byte[] seed) throws Exception {
        
        super(file);
        
        listeners = new ArrayList<>();
        folders = new ArrayList<>();
        fileMetadataList = new ArrayList<>();
        
        this.version = VERSION_1;
        
        folders.add(FILE_SEPARATOR);

        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom(seed);
        secureRandom.nextBytes(salt);
        
        int iterations = params.getIterations();
        int memory = params.getMemory();
        int parallelism = params.getParallelism();

        // Hash lento e mais seguro da senha com Argon2.
        byte[] hash = new Argon2Hash().getBytes(
            password,
            salt,
            iterations,
            memory,
            parallelism,
            KEY_LENGTH
        );

        passwordHash = hash;
        
        // Hash rápido do hash da senha com SHA-256.
        byte[] hash2 = new SHA256Hash().getBytes(hash);
        
        // Bytes de teste da senha.
        byte[] testBytes = getPasswordTestBytes(hash2);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
            salt.length + 
            (3 * Integer.BYTES) +
            testBytes.length
        );
        
        outputStream.write(salt, 0, salt.length);
        outputStream.write(ArrayUtils.intToByteArray(iterations));
        outputStream.write(ArrayUtils.intToByteArray(memory));
        outputStream.write(ArrayUtils.intToByteArray(parallelism));
        outputStream.write(testBytes);
        
        ByteArrayInputStream inputStream1 = new ByteArrayInputStream(outputStream.toByteArray());
        addStream(inputStream1, TEST_FILE_NAME);
        
        // Grava a versão do arquivo em METADATA/Version.
        byte[] versionBytes = intToByteArray(version);
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(versionBytes);
        addStream(inputStream2, VERSION_FILE_NAME);
        
        for (int i = 0; i < password.length; i++) {
            password[i] = 0x00;
        }
        
        password = null;
        
        updateInternalFileNameIndex();
        
        setComment("Pasta de arquivos.");

        setRootFolder(FILE_SEPARATOR);

    }
    
   
    /**
     * Constructor para a abertura de um arquivo existente. Use para ler o 
     * conteúdo de um arquivo.
     * @param file arquivo a ser aberto.
     * @param password senha do arquivo.
     * @throws Exception 
     */
    public BravoFile(File file, char[] password) throws Exception {
        
        super(file);

        if (file.exists()) {
            
            listeners = new ArrayList<>();
            folders = new ArrayList<>();
          
            boolean isSamePassword;
            
            try (InputStream inputStream = getInputStream(getFileHeader(TEST_FILE_NAME))) {
                
                byte[] salt = inputStream.readNBytes(SALT_LENGTH);
                
                int iterations = ArrayUtils.byteArrayToInt(inputStream.readNBytes(Integer.BYTES));
                
                int memory = ArrayUtils.byteArrayToInt(inputStream.readNBytes(Integer.BYTES));
                
                int parallelism = ArrayUtils.byteArrayToInt(inputStream.readNBytes(Integer.BYTES));
                
                byte[] testBytes1 = inputStream.readNBytes(8);
                
                byte[] hash = new Argon2Hash().getBytes(
                    password,
                    salt,
                    iterations,
                    memory,
                    parallelism,
                    KEY_LENGTH
                );
                
                passwordHash = hash;
                
                byte[] hash2 = new SHA256Hash().getBytes(hash);
                
                byte[] testBytes2 = getPasswordTestBytes(hash2);
                
                isSamePassword = Arrays.equals(testBytes1, testBytes2);
                
            }
            
            if (isSamePassword) {
                
                version = getVersion();
                
                internalFileNameIndex = getInternalFileNameIndex();
                
                fileMetadataList = getFileTable();
                
                folders.addAll(getFolders());

                setRootFolder(FILE_SEPARATOR);
                
            } else {
                
                throw new Exception("A senha do arquivo está incorreta.");
                
            }

        } else {
            
            throw new Exception("Arquivo inexistente.");
            
        }

    }
    
    
    /**
     * Obter os bytes de teste da senha.
     * @param hash hash da senha.
     * @return bytes de teste da senha.
     */
    private byte[] getPasswordTestBytes(byte[] hash) {
        return new byte[] {
            hash[0],
            hash[2],
            hash[4],
            hash[5],
            hash[7],
            hash[9],
            hash[10],
            hash[12]
        };
    }
    
    
    /**
     * Atualizar os metadados dos arquivos criptografados em <i>METADATA/FileTable</i>.
     * @throws Exception
     */
    private void updateFileTable() throws Exception {
        
        switch (version) {
            
            // Versão 1: Serializa o arranjo ArrayList<EncryptedFileMetadata>
            // fileMetadataList para o objeto de ObjectOutputStream. Ao serializar
            // o arranjo, é realizado a encriptação do mesmo, e em seguida é 
            // feita a gravação no arquivo METADATA/FileTable.
            //
            // Nesta versão, o arquivo .bar é dependente de ser lido apenas com 
            // este programa, pois a serialização é dependente de uma implementação
            // específica do Java, e da classe EncryptedFileMetadata na versão
            // correta.
            //
            // Resumindo. O arquivo .bar na versão 1 é dependente de implementação
            // específica em Java. No caso dos algoritmos de encriptação, hash,
            // base64, etc, todos dependentes da biblioteca BouncyCastle, pelo
            // menos teoricamente, podem ser implementados com outras bibliotecas
            // e funcionarão com implementação em outras linguagens. Em linguagem
            // C# seria trivial a implementação, pois a biblioteca BouncyCastle
            // tem uma implementação específica para esta linguagem. Demais operações
            // com streams são universais, e estão em qualquer linguagem de
            // programação de propósito geral.
            
            case VERSION_1 -> {
                
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    outputStream
                );
                objectOutputStream.writeObject(fileMetadataList);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    outputStream.toByteArray()
                );
                ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();

                EncryptedFileMetadata fileMetadata = new EncryptedFileMetadata();

                new AESCipher().encrypt(
                    inputStream,
                    dataOutputStream,
                    fileMetadata,
                    passwordHash
                );

                ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream(
                    16 + dataOutputStream.size()
                );
                zipOutputStream.writeBytes(fileMetadata.getIVBytes());
                zipOutputStream.writeBytes(dataOutputStream.toByteArray());

                ByteArrayInputStream zipInputStream = new ByteArrayInputStream(
                    zipOutputStream.toByteArray()
                );

                addStream(zipInputStream, FILE_TABLE_NAME);

                folders.clear();
                folders.addAll(getFolders());

            }
            
        }

    }
    
    
    /**
     * Obter os metadados dos arquivos criptografados em <i>METADATA/FileTable</i>.
     * @return metadados dos arquivos criptografados. Caso <i>METADATA/FileTable</i>
     * ainda não tenha sido criado, retorna um array vazio.
     * @throws Exception
     */
    private ArrayList<EncryptedFileMetadata> getFileTable() throws Exception {
        
        boolean exists = false;
        
        ArrayList<EncryptedFileMetadata> object;
        
        switch (version) {
            
            // Versão 1: Lê o arquivo METADATA/FileTable, decriptografa a
            // stream, e desserializa o arranjo ArrayList<EncryptedFileMetadata>
            // fileMetadataList que esta stream representa. No caso, este processo
            // é totalmemnte dependente de ser realizado com este programa, pois
            // o processo de serialização/desserialização do objeto é totalmente
            // dependente da linguagem em que ele foi realizado e da versão
            // correta das classes serializadas.

            case VERSION_1 -> {
                
                for (FileHeader zipHeader : getFileHeaders()) {
                    if (zipHeader.getFileName().equals(FILE_TABLE_NAME)) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) return new ArrayList<>();

                try (InputStream zipInputStream = getInputStream(getFileHeader(FILE_TABLE_NAME))) {

                    byte[] ivBytes = zipInputStream.readNBytes(IV_LENGTH);

                    EncryptedFileMetadata fileMetadata = new EncryptedFileMetadata();
                        fileMetadata.setIVBytes(ivBytes
                    );

                    ByteArrayOutputStream encryptedObject = new ByteArrayOutputStream();

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int length;

                    while ((length = zipInputStream.read(buffer)) != -1) {
                        encryptedObject.write(buffer, 0, length);
                    }

                    ByteArrayInputStream dataInputStream = new ByteArrayInputStream(
                        encryptedObject.toByteArray()
                    );

                    ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();

                    new AESCipher().decrypt(
                        dataInputStream,
                        dataOutputStream,
                        fileMetadata,
                        passwordHash
                    );

                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                        dataOutputStream.toByteArray()
                    );

                    try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

                        object = (ArrayList<EncryptedFileMetadata>) objectInputStream.readObject();

                        for (EncryptedFileMetadata fileMetadata2 : object) {
                            fileMetadata2.setZipHeader(
                                getFileHeader(fileMetadata2.getInternalFileName())
                            );
                        }

                    }

                }

                return object;
                
            }
            
            default -> {
                
                return null;
            
            }
            
        }
        
    }
    
    
    /**
     * Atualizar o contador sequêncial para nomeação dos arquivos criptografados
     * gravando-o em <i>METADATA/Index</i>.
     * @throws Exception
     */
    private void updateInternalFileNameIndex() throws Exception {
        byte[] dataBytes = intToByteArray(internalFileNameIndex);
        ByteArrayInputStream zipInputStream = new ByteArrayInputStream(dataBytes);
        addStream(zipInputStream, INDEX_FILE_NAME);
    }
    
    
    /**
     * Obter o contador sequêncial para nomeação de arquivos em <i>METADATA/Index</i>.
     * @return contador sequêncial para nomeação de arquivos.
     * @throws Exception
     */
    private int getInternalFileNameIndex() throws Exception {
        int counter;
        try (InputStream zipInputStream = getInputStream(getFileHeader(INDEX_FILE_NAME))) {
            byte[] dataBytes = zipInputStream.readAllBytes();
            counter = byteArrayToInt(dataBytes);
        }
        return counter;
    }

    
    /**
     * Obter o número da versão do arquivo em <i>METADATA/Version</i>.
     * @return número da versão do arquivo.
     * @throws Exception
     */
    public int getVersion() throws Exception {
        int fileVersion;
        try (InputStream zipInputStream = getInputStream(getFileHeader(VERSION_FILE_NAME))) {
            byte[] dataBytes = zipInputStream.readAllBytes();
            fileVersion = byteArrayToInt(dataBytes);
        }
        return fileVersion;
    }
    
    
    /**
     * Gravar os comentários do arquivo. Os comentários também serão criptografados
     * e convertidos para uma String no formato Base64 antes da gravação.
     * @param comment comentários do arquivo. 
     * @throws Exception 
     */
    @Override
    public void setComment(String comment) throws Exception {
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.writeBytes(comment.getBytes("UTF-8"));

        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            outputStream.toByteArray()
        );

        ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();

        EncryptedFileMetadata fileMetadata = new EncryptedFileMetadata();

        new AESCipher().encrypt(
            inputStream,
            dataOutputStream,
            fileMetadata,
            passwordHash
        );

        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream(16 +
        dataOutputStream.size());
        zipOutputStream.writeBytes(fileMetadata.getIVBytes());
        zipOutputStream.writeBytes(dataOutputStream.toByteArray());

        super.setComment(Base64.toBase64String(zipOutputStream.toByteArray()));
        
    }
    
    
    /**
     * Obter os comentários do arquivo.
     * @return comentários do arquivo.
     * @throws Exception
     */
    @Override
    public String getComment() throws Exception {
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
            Base64.decode(super.getComment())
        );

        byte[] ivBytes = inputStream.readNBytes(IV_LENGTH);
        EncryptedFileMetadata fileMetadata = new EncryptedFileMetadata();
        fileMetadata.setIVBytes(ivBytes);

        ByteArrayOutputStream text = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int length;

        while ((length = inputStream.read(buffer)) != -1) {
            text.write(buffer, 0, length);
        }

        ByteArrayInputStream dataInputStream = new ByteArrayInputStream(text.toByteArray());
        ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();

        new AESCipher().decrypt(
            dataInputStream,
            dataOutputStream,
            fileMetadata,
            passwordHash
        );

        return new String(dataOutputStream.toByteArray(), "UTF-8");
        
    }
    
    
    /**
     * Obter a referência para o objeto {@link EncryptedFileMetadata}. Retorna 
     * null caso não encontre uma correspondência.
     * @param fileName nome de arquivo associado ao objeto a ser referenciado.
     * @return referência para o objeto {@link EncryptedFileMetadata} no arranjo, 
     * ou null, caso não tenha um objeto associado ao nome de arquivo.
     */
    private EncryptedFileMetadata getEncryptedFileMetadata(String fileName) {
        EncryptedFileMetadata ref = null;
        for (EncryptedFileMetadata fileMetadata : fileMetadataList) {
            if (equals(fileMetadata.getFileName(), fileName)) {
                ref = fileMetadata;
                break;
            }
        }
        return ref;
    }
    
    
    /**
     * Adicionar arquivos. Neste método é feita a criptografia do arquivo,
     * logo após ele é inserido.
     * @param fileEncryptionInfoList lista de objetos {@link FileEncryptionInfo}
     * com informações sobre os arquivos a serem adicionados.
     * @throws Exception
     */
    private void addFiles(List<FileEncryptionInfo> fileEncryptionInfoList) throws Exception {
        
        try {

            // Não recicla o índice em caso de erro nas próximas etapas.
            updateInternalFileNameIndex();

            long totalBytes = 0;                

            for (FileEncryptionInfo fileEncryptionInfo : fileEncryptionInfoList) {
                if (fileEncryptionInfo.getInputFile() != null) {
                    long length = fileEncryptionInfo.getInputFile().length();
                    totalBytes += (2 * length) + (length % AESCipher.BLOCK_SIZE);
                }
            }

            reset(false, totalBytes);

            for (ProcessListener listener : listeners) {
                listener.abortBlocked(blockAbort);
            }

            for (FileEncryptionInfo fileEncryptionInfo : fileEncryptionInfoList) {

                if (abort()) break;
                
                EncryptedFileMetadata fileMetadata = fileEncryptionInfo.getFileMetadata();
                File inputFile = fileEncryptionInfo.getInputFile();
                File outputFile = fileEncryptionInfo.getOutputFile();

                if (!isEmptyFolder(fileMetadata)) {
                    
                    if (abort()) break;

                    InputStream inputStream;

                    updateFileInProcess(
                        inputFile.getAbsolutePath(),
                        ENCRYPT,
                        inputFile.length()
                    );

                    try (FileInputStream fileInputStream = new FileInputStream(inputFile);
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {

                        new AESCipher().encrypt(
                            fileInputStream,
                            fileOutputStream,
                            fileMetadata,
                            passwordHash,
                            this
                        );

                        inputStream = new EncryptedFileInputStream(
                            outputFile,
                            this
                        );

                    }

                    if (abort()) break;

                    updateFileInProcess(
                        inputFile.getAbsolutePath(),
                        ADD,
                        inputFile.length()
                    );

                    String internalFileName = fileMetadata.getInternalFileName();

                    addStream(inputStream, internalFileName);

                    FileHeader fileHeader = getFileHeader(internalFileName);
                    fileMetadata.setZipHeader(fileHeader);

                }

                if (!fileMetadataList.contains(fileMetadata)) {

                    fileMetadataList.add(fileMetadata);

                    //Caso o caminho do novo arquivo inserido pertença a um
                    //diretório vazio que está salvo, remove a entrada para
                    //este diretório vazio, pois seu caminho já aparecerá na
                    //listagem com a leitura deste arquivo.
                    String parent = getParentPath(fileMetadata);

                    List<EncryptedFileMetadata> emptyFolders = getEmptyFolders();

                    EncryptedFileMetadata folderToDelete = null;

                    for (EncryptedFileMetadata emptyFolder : emptyFolders) {
                        if (equals(parent, emptyFolder.getFileName())) {
                            folderToDelete = emptyFolder;
                            break;
                        }
                    }

                    if (folderToDelete != null) {
                        fileMetadataList.remove(folderToDelete);
                    }

                }

            }
        
        } finally {

            updateFileTable();
        
        }

    }
    
    
    /**
     * Adicionar um arquivo.
     * @param fileEncryptionInfo objeto {@link FileEncryptionInfo} com as
     * informações sobre o arquivo a ser adicionado.
     * @throws Exception 
     */
    private void addFile(FileEncryptionInfo fileEncryptionInfo) throws Exception {
        List<FileEncryptionInfo> list = new ArrayList<>(1);
        list.add(fileEncryptionInfo);
        addFiles(list);
    }
    
    
    /**
     * Obter as informações sobre o arquivo a ser criptografado e inserido.
     * @param inputFile arquivo de origem.
     * @param folder diretório interno a ser inserido o arquivo.
     * @return informações para a encriptação e inserção do arquivo.
     * @throws Exception 
     */
    private FileEncryptionInfo getFileEncryptionInfo(File inputFile, String folder) throws Exception {
        
        BasicFileAttributes fileAttributes = Files.getFileAttributeView(
            inputFile.toPath(),
            BasicFileAttributeView.class
        ).readAttributes();
        
        String filePath = getRelativeFilePath(inputFile, folder);
        EncryptedFileMetadata fileMetadata = getEncryptedFileMetadata(filePath);
        
        if (fileMetadata == null) {          
            StringBuilder internalFileName = new StringBuilder();
            internalFileName.append("File");
            internalFileName.append(String.format("%07d", ++internalFileNameIndex));
            fileMetadata = new EncryptedFileMetadata(); 
            fileMetadata.setInternalFileName(internalFileName.toString());
            fileMetadata.setFileName(filePath);
        }
        
        fileMetadata.setLastModifiedTime(fileAttributes.lastModifiedTime().toMillis());
        fileMetadata.setCreatedTime(fileAttributes.creationTime().toMillis());
        fileMetadata.setOriginalSize(fileAttributes.size());

        String encryptionFolderPath = RootFolder.getEncryptionFolder().getAbsolutePath();

        File outputFile = new File(
            encryptionFolderPath +
            File.separator + 
            fileMetadata.getInternalFileName() +
            ".tmp"
        );
        
        return (!abort ? new FileEncryptionInfo(inputFile, outputFile, fileMetadata) : null);
        
    }
    
    
    /**
     * Obter todos os arquivos em um diretório e seus subdiretórios.
     * @param folder diretório a obter os arquivos.
     * @return lista de objetos {@link FileEncryptionInfo} com informações sobre
     * os arquivos a serem encriptados do diretório.
     * @throws Exception
     */
    private List<FileEncryptionInfo> getFilesFromFolder(File folder) throws Exception {
        
        List<FileEncryptionInfo> fileEncryptionInfoList = new ArrayList<>();
        
        List<EncryptedFileMetadata> emptyFolders = extractEmptyFolders(folder);
        
        for (EncryptedFileMetadata emptyFolder : emptyFolders) {
            fileEncryptionInfoList.add(new FileEncryptionInfo(emptyFolder));
        }
        
        String folderName = getInternalFolderPath(folder);
        List<File> filesList = new ArrayList<>();
        listFilesFromFolder(folder, filesList);
        
        for (File file : filesList) {
            if (abort()) break;
            FileEncryptionInfo fileEncryptionInfo = getFileEncryptionInfo(file, folderName);
            if (fileEncryptionInfo != null) fileEncryptionInfoList.add(fileEncryptionInfo);            
        }
        
        if (abort()) return fileEncryptionInfoList;

        List<File> subfolders = new ArrayList<>();
        listSubfoldersFromFolder(folder, subfolders);

        for (File subfolder : subfolders) {

            if (abort()) break;

            filesList.clear();

            listFilesFromFolder(subfolder, filesList);                

            if (abort()) break;

            String subfolderName = getInternalFolderPath(folder, subfolder);

            for (File file : filesList) {

                if (abort()) break;

                FileEncryptionInfo fileEncryptionInfo = getFileEncryptionInfo(
                    file,
                    subfolderName
                );

                if (fileEncryptionInfo != null) {
                    fileEncryptionInfoList.add(fileEncryptionInfo);
                }

            }

        }
        
        return fileEncryptionInfoList;
        
    }  
   
    
    /**
     * Adicionar arquivos e diretórios. Na primeira etapa é realizada a encriptação
     * e posteriormente é feita a inserção.
     * @param filesAndFolders arquivos e diretórios a serem inseridos.
     * @param destroySourceFiles se true, destrói os arquivos na origem.
     * @throws Exception
     */
    public void addFilesAndFolders(List<File> filesAndFolders, boolean destroySourceFiles) throws Exception {
        
        try {
            
            List<FileEncryptionInfo> fileEncryptionInfoList = new ArrayList<>();
            
            try {
                
                reset(false, 0);
                
                for (ProcessListener listener : listeners) {
                    listener.abortBlocked(blockAbort);
                }
                
                List<File> filesList = new ArrayList<>();
                List<File> foldersList = new ArrayList<>();
                
                for (File file : filesAndFolders) {
                    if (abort()) break;
                    if (file.isFile()) {
                        filesList.add(file);
                    } else {
                        foldersList.add(file);
                    }
                }
                
                if (abort()) return;
                
                for (File file : filesList) {
                    if (abort()) break;
                    FileEncryptionInfo fileEncryptionInfo = getFileEncryptionInfo(file, null);
                    if (fileEncryptionInfo != null) {
                        fileEncryptionInfoList.add(fileEncryptionInfo);
                    }
                }
                
                if (abort()) return;
                
                for (File folder : foldersList) {
                    if (abort()) break;
                    fileEncryptionInfoList.addAll(getFilesFromFolder(folder));                    
                }
                
                if (abort()) return;
                
                addFiles(fileEncryptionInfoList);
                
                if (abort()) return;
                
                if (destroySourceFiles) {
                    //Destrói os arquivos na origem.
                    CacheCleaner cacheCleaner = new CacheCleaner();
                    listeners.forEach(l -> {cacheCleaner.addListener(l);});
                    cacheCleaner.cleanExternalFiles(filesAndFolders);
                    listeners.forEach(l -> {cacheCleaner.removeListener(l);});
                }
                
            } finally {
                
                blockAbort = false;
                
                for (ProcessListener listener : listeners) {
                    listener.abortBlocked(blockAbort);
                }
                
            }
            
        } finally {
            
            for (ProcessListener listener : listeners) {
                listener.done();
            }
            
        }
        
    }
    
    
    /**
     * Testar o processo de adição de arquivos. O objetivo é listar os
     * arquivos que serão sobrescritos se for realizado o processo.
     * @param filesAndFolders arquivos e pastas a serem inseridos.
     * @return lista de arquivos que serão sobrescritos caso o processo de
     * adição seja realizado.
     * @throws Exception
     */
    public List<File> testAddFilesAndFolders(List<File> filesAndFolders) throws Exception {
        
        List<File> existingFiles = new ArrayList<>();
        
        for (File file : filesAndFolders) {
            
            if (file.isDirectory()) {
                
                String folderName = getInternalFolderPath(file);
                List<File> filesList = new ArrayList<>();
                listFilesFromFolder(file, filesList);
                
                for (File file2 : filesList) {
                    if (fileExists(getRelativeFilePath(file2, folderName))) {
                        existingFiles.add(file2);
                    }
                }
                
                List<File> subfolders = new ArrayList<>();
                listSubfoldersFromFolder(file, subfolders);
                
                for (File subfolder : subfolders) {
                    
                    filesList.clear();
                    
                    listFilesFromFolder(subfolder, filesList);
                    
                    String subfolderName = getInternalFolderPath(file, subfolder);
                    
                    for (File file2 : filesList) {
                        if (fileExists(getRelativeFilePath(file2, subfolderName))) {
                            existingFiles.add(file2);
                        }
                    }
                    
                }
                
            } else {
                
                if (fileExists(getRelativeFilePath(file, null))) {
                    existingFiles.add(file);
                }
                
            }
            
        }
        
        return existingFiles;
        
    }
    
    
    /**
     * Adicionar um novo diretório vazio com base no diretório raiz selecionado.
     * @param folderName nome do novo diretório vazio.
     * @throws Exception
     */
    public void addNewEmptyFolder(String folderName) throws Exception {
        
        if (!containsInvalidCharacters(folderName)) {
            
            EncryptedFileMetadata fileMetadata = createEmptyFolder(folderName);
            
            if (!folderExists(fileMetadata.getFileName())) {
                
                addFile(new FileEncryptionInfo(fileMetadata));
                
            } else {
                
                throw new Exception(
                    "Diretório " +
                    fileMetadata.getFileName() + 
                    " já existe."
                );
                
            }
            
        } else {
            
            throw new Exception(
                "Caracteres inválidos compondo o nome do diretório."
            );
            
        }
        
    }
    
    
    /**
     * Excluir o arquivo.
     * @param fileMetadata metadados do arquivo a ser excluído.
     * @throws Exception
     */
    private void deleteFile(EncryptedFileMetadata fileMetadata) throws Exception {
        
        if (!isEmptyFolder(fileMetadata)) {
            
            updateFileInProcess(
                fileMetadata.getFileName(),
                REMOVE,
                fileMetadata.getOriginalSize()
            );
            
            deleteFile(fileMetadata.getZipHeader());
            
            notify(fileMetadata.getOriginalSize());
            
        }
        
        fileMetadataList.remove(fileMetadata);
        
    }
    
    
    /**
     * Excluir os arquivos e diretórios. Nos casos em que se exclui todos os 
     * arquivos de um diretório, mas não o diretório em si, ele é mantido como
     * diretório vazio.
     * @param filesAndFolders arquivos e diretórios a serem excluídos.
     * @throws Exception
     */
    public void deleteFilesAndFolders(List<String> filesAndFolders) throws Exception {
        
        try {
            
            List<String> parentsList = new ArrayList<>();
            
            try {
                
                List<String> filesAndFoldersCopy = new ArrayList<>(filesAndFolders.size());
                filesAndFoldersCopy.addAll(filesAndFolders);
                List<EncryptedFileMetadata> deletedFiles = new ArrayList<>();
                List<String> deletedFolders = new ArrayList<>();
                
                for (String folder : filesAndFoldersCopy) {
                    if (abort()) break;
                    if (isFolderPath(folder)) {
                        deletedFolders.add(folder);
                        deletedFiles.addAll(getAllFilesFromFolder(folder));
                    }
                }
                
                if (abort()) return;
                
                filesAndFoldersCopy.removeAll(deletedFolders);
                
                for (String file : filesAndFoldersCopy) {            
                    if (abort()) break;
                    String parent = getParentPath(file);
                    if (!parentsList.contains(parent)) {
                        parentsList.add(parent);
                    }
                    deletedFiles.add(getEncryptedFileMetadata(file));
                }
                
                if (abort()) return;
                
                reset(false, calculateSize(deletedFiles, 1));
                
                for (EncryptedFileMetadata fileMetadata : deletedFiles) {
                    if (abort()) break;       
                    deleteFile(fileMetadata);                    
                }
                
                if (abort()) return;
                
                for (String folder : deletedFolders) {
                    if (abort()) break;
                    String parent = getParentPath(folder);
                    if (!parentsList.contains(parent)) {
                        parentsList.add(parent);
                    }
                    EncryptedFileMetadata fileMetadata = getEncryptedFileMetadata(folder);
                    if (fileMetadata != null) {
                        // Remove entrada de diretório vazio.
                        deleteFile(fileMetadata);
                    }
                }

            } finally {
                
               updateFileTable();
               
            }
            
            checkForEmptyFolders(parentsList);
            
        } finally {
            
            for (ProcessListener listener : listeners) {
                listener.done();
            }
            
        }
        
    }
    
    
    /**
     * Verificar se há no nome do arquivo ou diretório algum caractere inválido,
     * que não faz parte do conjunto aceito para as diversas plataformas. Caso um
     * caractere inválido seja detectado, retorna true para a chamada do método.
     * @param fileName nome do arquivo ou diretório.
     * @return true, há caracteres inválidos no nome do arquivo ou diretório,
     * false, não há caracteres inválidos.
     */
    private boolean containsInvalidCharacters(String fileName) {
        boolean contains = false;
        for (int i = 0; i < fileName.length(); i++) {
            if (contains) break;
            for (int j = 0; j < INVALID_FILE_NAME_CHARACTERS.length; j++) {
                if (fileName.charAt(i) == INVALID_FILE_NAME_CHARACTERS[j]) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    
    /**
     * Renomear um arquivo. O nome que será alterado é o que identifica o arquivo,
     * e não o que ele usa internamente, que sempre será FileXXXXXXX.
     * @param fileMetadata metadados do arquivo a ser renomeado.
     * @param newFileName novo nome do arquivo.
     * @throws Exception
     */
    private void renameFile(EncryptedFileMetadata fileMetadata, String newFileName) throws Exception {
        
        if (!containsInvalidCharacters(newFileName)) {
            
            String parent = getParentPath(fileMetadata);
            String newFilePath = (parent.equals(FILE_SEPARATOR) ? parent + 
            newFileName : parent + FILE_SEPARATOR + newFileName);
            
            if (fileExists(newFilePath)) {
                throw new Exception(
                    "Arquivo " + fileMetadata.getFileName() + 
                    " não pode ser renomeado para " + newFilePath + 
                    " pois esse nome de arquivo já está sendo utilizado."
                );
            }
            
            fileMetadata.setFileName(newFilePath);
            
            updateFileTable();
            
        } else {
            
            StringBuilder sb = new StringBuilder();
            sb.append("Caracteres inválidos compondo o nome do arquivo. ");
            sb.append("Não utilize");
            for (int i = 0; i < INVALID_FILE_NAME_CHARACTERS.length - 1; i++) {
                sb.append("    ");
                sb.append(INVALID_FILE_NAME_CHARACTERS[i]);
            }
            sb.append("    no nome.");

            throw new Exception(
                sb.toString()
            );
            
        }
        
    } 
    
    
    /**
     * Renomear um arquivo. O nome que será alterado é o que identifica o arquivo,
     * e não o que ele usa internamente, que sempre será FileXXXXXXX.
     * @param fileName nome atual do arquivo.
     * @param newFileName novo nome do arquivo.
     * @throws Exception
     */
    public void renameFile(String fileName, String newFileName) throws Exception {
        
        if (newFileName.isBlank()) {
            throw new Exception(
                "Novo nome do arquivo não pode estar vazio."
            );
        }
        
        EncryptedFileMetadata fileMetadata = getEncryptedFileMetadata(fileName);
        
        if (fileMetadata != null) {
            
            renameFile(fileMetadata, newFileName);
            
        } else {
            
            throw new Exception(
                "Arquivo " + fileName + " não encontrado."
            );
            
        }
    
    }
    
    
    /**
     * Renomear um diretório.
     * @param folderName nome atual do diretório.
     * @param newFolderName novo nome do diretório. 
     * @throws Exception 
     */
    public void renameFolder(String folderName, String newFolderName) throws Exception {
        
        if (newFolderName.isBlank()) {
            throw new Exception(
                "Novo nome do diretório não pode estar vazio."
            );
        }
        
        boolean containsFolder = false;
        
        for (String folder : folders) {
            if (equals(folderName, folder)) {
                containsFolder = true;
                break;
            }
        }
        
        if (containsFolder) {
            
            if (!folderName.equals(FILE_SEPARATOR)) {

                if (!containsInvalidCharacters(newFolderName)) {

                    String parent = getParentPath(folderName);
                    String newFolderPath = (parent.equals(FILE_SEPARATOR) ? parent + 
                    newFolderName : parent + FILE_SEPARATOR + newFolderName);

                    if (folderExists(newFolderPath)) {
                        throw new Exception(
                            "Diretório " + folderName + 
                            " não pode ser renomeado para " + newFolderPath + 
                            " pois esse nome já está sendo utilizado."
                        );
                    }
                    
                    EncryptedFileMetadata emptyFolderMetadata = getEncryptedFileMetadata(folderName);
                    
                    if (emptyFolderMetadata == null) {

                        List<EncryptedFileMetadata> encryptedFiles = getAllFilesFromFolder(folderName);

                        for (EncryptedFileMetadata fileMetadata : encryptedFiles) {
                            String filePath = fileMetadata.getFileName().replaceFirst(
                                folderName,
                                newFolderPath
                            );
                            fileMetadata.setFileName(filePath);
                        }
                    
                    } else {
                        
                        emptyFolderMetadata.setFileName(getRelativeFolderPath(newFolderName));
                        
                    }

                    updateFileTable();

                } else {
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("Caracteres inválidos compondo o nome do diretório. ");
                    sb.append("Não utilize");
                    for (int i = 0; i < INVALID_FILE_NAME_CHARACTERS.length - 1; i++) {
                        sb.append("    ");
                        sb.append(INVALID_FILE_NAME_CHARACTERS[i]);
                    }
                    sb.append("    no nome.");
                    
                    throw new Exception(
                        sb.toString()
                    );

                }

            } else {

                throw new Exception("Operação inválida.");

            }
            
        } else {
            
            throw new Exception("Diretório " + folderName + " não encontrado.");
            
        }
        
    }
    
    
    /**
     * Mover arquivos e diretórios para um determinado diretório de destino.
     * @param filesAndFolders arquivos e diretórios a serem movidos.
     * @param destinationFolder diretório de destino.
     * @throws Exception 
     */
    public void moveFilesAndFolders(List<String> filesAndFolders, String destinationFolder) throws Exception {
        
        try {
            
            boolean exists = true;
            
            List<String> folders = new ArrayList<>();
        
            for (String file : filesAndFolders) {
                if (!isFilePath(file)) {
                    if (!isFolderPath(file)) {
                        exists = false;
                        break;
                    } else {
                        folders.add(file);
                    }
                }
            }

            if (exists) {

                exists = isFolderPath(destinationFolder);

            }
            
            if (!exists) {

                throw new Exception("Erro ao mover o diretório " + file);

            }
            
            boolean movingToSubfolder = false;
            
            for (String folder : folders) {
                    
                List<String> subFolders = getAllSubfoldersFromFolder(folder);
                
                for (String subFolder : subFolders) {
                    if (equals(destinationFolder, subFolder)) {
                        movingToSubfolder = true;
                        break;
                    }
                }
                
                if (movingToSubfolder) {
                    break;
                }

            }

            if (movingToSubfolder) {
                
                throw new Exception("Erro ao mover o diretório " + file);
                
            }
            
            for (String file : filesAndFolders) {
                if (file.equals(destinationFolder)) {
                    throw new Exception("Erro ao mover o diretório " + file);
                }
            }
            
            List<String> parentsList = new ArrayList<>();
            
            List<String> existingFiles = testMoveFilesAndFolders(
                filesAndFolders, 
                destinationFolder
            );

            if (!existingFiles.isEmpty()) {
                throw new Exception(
                    "Já existe(m) arquivo(s) com o mesmo nome em " +
                    destinationFolder +
                    "."
                );
            }

            try {

                List<String> filesList = new ArrayList<>();
                List<String> foldersList = new ArrayList<>();
                
                for (String file : filesAndFolders) {
                    if (isFilePath(file)) {
                        filesList.add(file);
                    } else {
                        foldersList.add(file);
                    }
                }
                
                for (String file : filesList) {
                    
                    EncryptedFileMetadata fileMetadata = getEncryptedFileMetadata(file);
                    String parent = getParentPath(fileMetadata);
                    
                    if (!equals(parent, destinationFolder)) {
                        if (!parentsList.contains(parent)) {
                            parentsList.add(parent);
                        }
                        String fileName = extractFileName(file);
                        String newFilePath;
                        if (!destinationFolder.equals(FILE_SEPARATOR)) {
                            newFilePath = destinationFolder + FILE_SEPARATOR + fileName;
                        } else {
                            newFilePath = FILE_SEPARATOR + fileName;
                        }
                        fileMetadata.setFileName(newFilePath);
                    }
                    
                }
                
                for (String folder : foldersList) {
                    
                    String parent = getParentPath(folder);
                    
                    if (!equals(parent, destinationFolder)) {
                        
                        parent = destinationFolder;
                        boolean moveFolder = true;
                        
                        do {
                            if (equals(folder, parent)) {
                                moveFolder = false;
                                break;
                            } else {
                                parent = getParentPath(parent);
                            }
                        } while (!parent.equals(FILE_SEPARATOR));
                        
                        if (moveFolder) {
                            
                            parent = getParentPath(folder);
                            
                            if (!parentsList.contains(parent)) {
                                parentsList.add(parent);
                            }
                            
                            String folderName = extractFileName(folder);
                            String newFolderPath;
                            
                            if (!equals(FILE_SEPARATOR, destinationFolder)) {
                                newFolderPath = destinationFolder + FILE_SEPARATOR + 
                                folderName;
                            } else {
                                newFolderPath = FILE_SEPARATOR + folderName;
                            }
                            
                            List<EncryptedFileMetadata> encryptedFiles = getAllFilesFromFolder(folder);
                            
                            for (EncryptedFileMetadata fileMetadata : encryptedFiles) {
                                String newFileName = fileMetadata.getFileName()
                                .replaceFirst(folder, newFolderPath);
                                fileMetadata.setFileName(newFileName);
                            }
                            
                        }
                        
                    }
                    
                }
                
                EncryptedFileMetadata metadata = getEncryptedFileMetadata(destinationFolder);
            
                if (isEmptyFolder(metadata)) {

                    fileMetadataList.remove(metadata);

                }

            } finally {
                
                updateFileTable();
                
            }
            
            checkForEmptyFolders(parentsList);

        } finally {
            
            for (ProcessListener listener : listeners) {
                listener.done();
            }
            
        }
        
    }
    
    
    /**
     * Testar o processo de mover arquivos e diretórios. O objetivo é listar os
     * arquivos e diretórios que têm o mesmo nome no diretório de destino, tornando
     * impossível a operação.
     * @param filesAndFolders arquivos e diretórios a serem movidos.
     * @param destinationFolder diretório de destino.
     * @return lista de arquivos e diretórios com o mesmo nome no diretório de
     * destino.
     * @throws Exception 
     */
    private List<String> testMoveFilesAndFolders(List<String> filesAndFoldes, String destinationFolder) {
        
        List<String> existingFiles = new ArrayList<>();
        
        for (String file : filesAndFoldes) {
            
            if (isFilePath(file)) {
                
                EncryptedFileMetadata fileMetadata = getEncryptedFileMetadata(file);
                String parent = getParentPath(fileMetadata);
                
                if (!equals(parent, destinationFolder)) {
                    
                    String fileName = extractFileName(file);
                    String newFilePath;
                    
                    if (!equals(destinationFolder, FILE_SEPARATOR)) {
                        newFilePath = destinationFolder + FILE_SEPARATOR + fileName;
                    } else {
                        newFilePath = FILE_SEPARATOR + fileName;
                    }
                    
                    if (fileExists(newFilePath)) {
                        existingFiles.add(file);
                    }
                    
                }
                
            } else {
                
                String parent = getParentPath(file);
                
                if (!equals(parent, destinationFolder)) {
                    
                    String folderName = extractFileName(file);
                    String newFolderPath;
                    
                    if (!equals(destinationFolder, FILE_SEPARATOR)) {
                        newFolderPath = destinationFolder + FILE_SEPARATOR + folderName;
                    } else {
                        newFolderPath = FILE_SEPARATOR + folderName; 
                    }
                    
                    if (folderExists(newFolderPath)) {
                        existingFiles.add(file);
                    }
                    
                }
                
            }
            
        }
        
        return existingFiles;
        
    }
    
    
    /**
     * Extrair um arquivo para um diretório em disco. No processo é realizado
     * a descriptografia do arquivo.
     * @param fileMetadata metadados do arquivo a ser renomeado.
     * @param destinationPath diretório aonde o arquivo será extraído.
     * @return arquivo extraído.
     * @throws Exception 
     */
    private File extractFile(EncryptedFileMetadata fileMetadata, String destinationPath) throws Exception {
        
        File destinationFile = getDestinationFile(
            fileMetadata.getFileName(),
            destinationPath
        );
        
        if (!isEmptyFolder(fileMetadata)) {
            
            updateFileInProcess(
                destinationFile.getAbsolutePath(),
                EXTRACT,
                fileMetadata.getOriginalSize()
            );        
            
            File parentFolder = destinationFile.getParentFile();
            if (!parentFolder.exists()) parentFolder.mkdirs();
            
            try (ZipInputStream zipInputStream = getInputStream(fileMetadata.getZipHeader());
            FileOutputStream fileOutputStream = new FileOutputStream(destinationFile)) {

                new AESCipher().decrypt(
                    zipInputStream,
                    fileOutputStream,
                    fileMetadata,
                    passwordHash,
                    this
                );
                
                if (!abort()) {
                    FileTime creationTime = FileTime.fromMillis(fileMetadata.getCreatedTime());
                    FileTime modifiedTime = FileTime.fromMillis(fileMetadata.getLastModifiedTime());
                    Files.setAttribute(destinationFile.toPath(), "basic:creationTime", creationTime);
                    Files.setAttribute(destinationFile.toPath(), "basic:lastModifiedTime", modifiedTime);
                }
                
            }
            
        } else {
            
            destinationFile.mkdirs();
            
        }
        
        return destinationFile;
        
    }

    
    /**
     * Extrair arquivos e diretórios para um diretório em disco.
     * @param filesAndFolders arquivos e diretórias a serem extraídos.
     * @param destinationPath diretório em disco.
     * @return lista de arquivos extraídos.
     * @throws Exception 
     */
    public List<File> extractFilesAndFolders(List<String> filesAndFolders, String destinationPath) throws Exception  {
        
        try {
            
            List<File> filesList = new ArrayList<>();
            
            List<String> filesAndFoldersCopy = new ArrayList<>(filesAndFolders.size());
            filesAndFoldersCopy.addAll(filesAndFolders);
            
            List<EncryptedFileMetadata> encryptedFiles = new ArrayList<>();
            List<String> foldersList = new ArrayList<>();
            
            for (String folderName : filesAndFoldersCopy) {
                if (abort()) break;
                if (isFolderPath(folderName)) {
                    foldersList.add(folderName);
                    encryptedFiles.addAll(getAllFilesFromFolder(folderName));
                }
            }
            
            if (abort()) return filesList;
                
            filesAndFoldersCopy.removeAll(foldersList);

            for (String fileName : filesAndFoldersCopy) {
                if (abort()) break;
                encryptedFiles.add(getEncryptedFileMetadata(fileName));
            }
            
            if (abort()) return filesList;

            reset(false, calculateSize(encryptedFiles, 1));

            for (EncryptedFileMetadata fileMetadata : encryptedFiles) {
                if (abort()) break;
                filesList.add(extractFile(fileMetadata, destinationPath));
            }
            
            if (abort()) return filesList;

            // Cria os diretórios vazios caso exista esta condição.
            for (String folder : foldersList) {
                if (abort()) break;
                EncryptedFileMetadata fileMetadata = getEncryptedFileMetadata(folder);
                if (fileMetadata != null) {
                    filesList.add(extractFile(fileMetadata, destinationPath));
                }
            }
            
            return filesList;
            
        } finally {
            
            for (ProcessListener listener : listeners) {
                listener.done();
            }
            
        }
        
    }
    
    
    /**
     * Extrair arquivos para o diretório de cache. Durante o tempo de sessão
     * estes arquivos estarão decriptografados no diretório, podendo ser lidos
     * e copiados.
     * @param files arquivos a serem extraídos.
     * @return lista dos arquivos extraídos.
     * @throws Exception 
     */
    public List<File> extractFilesToCacheFolder(List<String> files) throws Exception {
        
        try {
            
            List<File> filesList = new ArrayList<>();
            long bytesCounter = 0;
            
            for (String file : files) {
                EncryptedFileMetadata fileMetadata = getEncryptedFileMetadata(file);
                bytesCounter += fileMetadata.getOriginalSize();
            }
            
            reset(false, bytesCounter);
            
            for (String file : files) {
                
                if (abort()) break;
                
                EncryptedFileMetadata fileMetadata = getEncryptedFileMetadata(file);
                String fileName = extractFileName(file);
                String onlyName = fileName;
                String extension = null;
                int idx = fileName.lastIndexOf(".");
                
                if (idx != -1) {
                    onlyName = fileName.substring(0, idx);
                    extension = fileName.substring(idx, fileName.length());
                }
                
                File destinationFile;
                int index = 0;
                
                do {
                    
                    StringBuilder filePath = new StringBuilder();
                    filePath.append(RootFolder.getExtractionFolder().getAbsolutePath());
                    filePath.append(File.separator);
                    filePath.append(onlyName);
                    
                    if (index != 0) {
                        filePath.append(" (");
                        filePath.append(String.valueOf(index));
                        filePath.append(")");
                    }
                    
                    if (extension != null) filePath.append(extension);
                    destinationFile = new File(filePath.toString());
                    index++;
                    
                } while (destinationFile.exists());
                
                File parentFolder = destinationFile.getParentFile();
                
                if (!parentFolder.exists()) {
                    parentFolder.mkdirs();
                }
                
                if (abort()) break;
                
                updateFileInProcess(
                    destinationFile.getAbsolutePath(),
                    EXTRACT,
                    fileMetadata.getOriginalSize()
                );
                
                try (ZipInputStream zipInputStream = getInputStream(fileMetadata.getZipHeader());
                FileOutputStream fileOutputStream = new FileOutputStream(destinationFile)) {
                    
                    new AESCipher().decrypt(
                        zipInputStream,
                        fileOutputStream,
                        fileMetadata,
                        passwordHash,
                        this
                    );
                    
                    if (!abort()) {
                        FileTime creationTime = FileTime.fromMillis(fileMetadata.getCreatedTime());
                        FileTime modifiedTime = FileTime.fromMillis(fileMetadata.getLastModifiedTime());
                        Files.setAttribute(destinationFile.toPath(), "basic:creationTime", creationTime);
                        Files.setAttribute(destinationFile.toPath(), "basic:lastModifiedTime", modifiedTime);
                        Files.setAttribute(destinationFile.toPath(), "dos:readonly", true);
                    }
                    
                }
                
                if (!abort()) {
                    filesList.add(destinationFile);
                }
                
            }
            
            return filesList;
            
        } finally {
            
            for (ProcessListener listener : listeners) {
                listener.done();
            }
            
        }
        
    }
    
    
    /**
     * Testar o processo de extração de arquivos e diretórios. O objetivo é 
     * listar os arquivos que serão sobrescritos se for realizado o processo.
     * @param filesAndFolders arquivos e diretórios a serem extraídos.
     * @param destinationPath diretório de destino.
     * @return lista dos arquivos que serão sobrescritos no destino.
     * @throws Exception 
     */
    public List<File> testExtractFilesAndFolders(List<String> filesAndFolders, 
    String destinationPath) throws Exception  {
        
        List<File> existingFiles = new ArrayList<>();
        
        List<String> filesAndFoldersCopy = new ArrayList<>(filesAndFolders.size());
        filesAndFoldersCopy.addAll(filesAndFolders);
        List<String> foldersList = new ArrayList<>();
        
        for (String folderName : filesAndFoldersCopy) {
            
            if (isFolderPath(folderName)) {
                
                foldersList.add(folderName);
                List<EncryptedFileMetadata> encryptedFiles = getAllFilesFromFolder(folderName);
                
                for (EncryptedFileMetadata fileMetadata : encryptedFiles) {
                    File file = getDestinationFile(
                        fileMetadata.getFileName(),
                        destinationPath
                    );
                    if (file.exists()) {
                        existingFiles.add(file);
                    }
                }
                
            }
            
        }
        
        filesAndFoldersCopy.removeAll(foldersList);
        
        for (String fileName : filesAndFoldersCopy) {
            File file = getDestinationFile(fileName, destinationPath);
            if (file.exists()) {
                existingFiles.add(file);
            }
        }
        
        return existingFiles;
        
    }

    
    /**
     * Extrair todos os arquivos e diretórios para o diretório em disco.
     * @param destinationPath diretório em disco.
     * @throws Exception 
     */
    public void extractAllFiles(String destinationPath) throws Exception {
        
        try {
            
            List<EncryptedFileMetadata> encryptedFiles = getAllFilesFromFolder(rootFolder);
            
            reset(false, calculateSize(encryptedFiles, 1));
            
            for (EncryptedFileMetadata fileMetadata : encryptedFiles) {
                if (abort()) break;
                extractFile(fileMetadata, destinationPath);
            }
            
        } finally {
            
            for (ProcessListener listener : listeners) {
                listener.done();
            }
            
        }
        
    }
    
    
    public void changePassword(String newPassword, Argon2Params params, byte[] seed) throws Exception {
        /*FileEraser fileEraser = new FileEraser();
        fileEraser.addCipherListener(this);        
        try {
            if (!isFilePassword(newPassword)) {
                List<File> list = new ArrayList<>();
                File cacheFolder = RootFolder.getExtractFolder2();
                list.add(cacheFolder);
                reset(true, 0);
                fileEraser.wipeFiles(list);
                int passes = 3 + fileEraser.getPassesByMethod();
                reset(true, calculateSize2(fileMetadataList, passes));
                for (FileMetadata fileMetadata : fileMetadataList) {
                    if (abort()) break;
                    extractFile(fileMetadata, cacheFolder.getAbsolutePath());
                }
                writeTestBytes(newPassword);
                this.password = newPassword;
                fileMetadataList.clear();
                index = 0;        
                writeFileMetadataList();
                setRootFolder(SEPARATOR);
                List<EncryptedFileData> encryptedFiles = new ArrayList<>();
                List<File> filesList = new ArrayList<>();
                listFilesFromFolder(cacheFolder, filesList);
                for (File file : filesList) {
                    EncryptedFileData encryptedFile = getEncryptedFileData(file, null);
                    if (encryptedFile != null) encryptedFiles.add(encryptedFile);
                }
                List<File> subfolders = new ArrayList<>();
                File[] foldersList = cacheFolder.listFiles(new DirectoryFilter());
                if (foldersList != null) {
                    subfolders.addAll(Arrays.asList(foldersList));
                }
                for (File folder : subfolders) {
                    encryptedFiles.addAll(encryptFolder(folder));
                }                
                addEncryptedFiles(encryptedFiles);
                updateFileInProcess(cacheFolder.getAbsolutePath(), WIPE,
                calculateSize(list, fileEraser.getPassesByMethod()));
                fileEraser.wipeFiles(list);
            }
        } finally {
            fileEraser.removeCipherListener(this);
            for (ProcessListener listener : listeners) {
                listener.done();
            }
        }*/
    }
    
    
    /**
     * Obter todos os arquivos criptografados de um diretório e de seus
     * subdiretórios.
     * @param folderName diretório que será obtida a lista de arquivos.
     * @return lista dos arquivos criptografados do diretório.
     */
    private List<EncryptedFileMetadata> getAllFilesFromFolder(String folderName) {
        
        List<EncryptedFileMetadata> filesList = new ArrayList<>();
        
        for (EncryptedFileMetadata fileMetadata : fileMetadataList) {
            
            boolean isParent = false;
            String parent = fileMetadata.getFileName();
            
            do {
                parent = getParentPath(parent);
                if (equals(parent, folderName)) {
                    isParent = true;
                    break;
                }
            } while (!parent.equals(FILE_SEPARATOR));
            
            if (isParent) {
                filesList.add(fileMetadata);
            }
            
        }
        
        return filesList;
        
    }
    
    
    /**
     * Verificar se algum diretório ficou vazio, e criar uma entrada para
     * diretório vazio se isto for verificado. Esta condição pode ocorrer
     * quando se exclui arquivos ou move-os para outro diretório.
     * @param folders diretórios a verificar a condição.
     * @throws Exception 
     */
    private void checkForEmptyFolders(List<String> folders) throws Exception {
        
        boolean blockAbortValue = blockAbort;
        blockAbort = true;
        
        List<FileEncryptionInfo> newEmptyFolders = new ArrayList<>();
        
        for (String folder : folders) {
            
            List<EncryptedFileMetadata> filesList = getAllFilesFromFolder(folder);
            List<String> foldersList = getAllSubfoldersFromFolder(folder);
            
            if (filesList.isEmpty() && foldersList.isEmpty()) {
                EncryptedFileMetadata fileMetadata = new EncryptedFileMetadata();
                fileMetadata.setFileName(folder);
                fileMetadata.setInternalFileName(EMPTY_FOLDER_TAG);
                newEmptyFolders.add(new FileEncryptionInfo(fileMetadata));
            }
            
        }
        
        if (!newEmptyFolders.isEmpty()) {
            addFiles(newEmptyFolders);
        }
        
        blockAbort = blockAbortValue;
        
    } 
    
    
    private String getRelativeFilePath(File file, String folder) throws IOException {    
        StringBuilder sb = new StringBuilder();
        sb.append(rootFolder);
        if (!rootFolder.equals(FILE_SEPARATOR)) sb.append(FILE_SEPARATOR);
        if (folder != null && !folder.equals("")) {
            sb.append(folder);
            sb.append(FILE_SEPARATOR);
        }
        sb.append(file.getName());
        return adaptFilePath(sb.toString());
    }
    
    
    private String getRelativeFolderPath(String folder) throws IOException {    
        StringBuilder sb = new StringBuilder();
        sb.append(rootFolder);
        if (!rootFolder.equals(FILE_SEPARATOR)) sb.append(FILE_SEPARATOR);
        sb.append(folder);
        return adaptFilePath(sb.toString());
    }
    
    
    private String getInternalFolderPath(File folder, File subfolder) {
        //Exemplo: "C:\Teste"
        String folderPath = folder.getAbsolutePath();
        //"C:\Teste" --> "[XXX]Teste" --> "Teste"
        String folderName = folder.getName();
        //Fará a adaptação no nome do diretório para nome relativo, 
        //utilizado internamente:
        //"C:\Teste\Teste1"
        String subfolderName1 = subfolder.getAbsolutePath();
        //"C:\Teste\Teste1" --> "[XXXXXXXXXX]\Teste1" --> "Teste\Teste1"
        String subfolderName2 = subfolderName1.replace(folderPath, folderName);
        //"Teste\Teste1" --> "Teste/Teste1"
        String subfolderName3 = subfolderName2.replace(File.separator, FILE_SEPARATOR);
        return subfolderName3;
    }
    
    
    private String getInternalFolderPath(File folder) {
        return folder.getName();
    }
    
    
    private String getParentPath(EncryptedFileMetadata fileMetadata) {
        return getParentPath(fileMetadata.getFileName());
    }
    
    
    public String getParentPath(String file) {
        
        int lastIndex = file.lastIndexOf(FILE_SEPARATOR);
        String parentPath = file.substring(0, lastIndex == 0 ? 1 : lastIndex);
        int index = -1;
        
        for (int i = 0; i < folders.size(); i++) {
            if (equals(folders.get(i), parentPath)) {
                index = i;
                break;
            }
        }
        
        if (index != -1) {
            return folders.get(index);
        } else {
            return parentPath;
        }
        
    }
    
    
    private String extractFileName(String file) {
        return file.substring(file.lastIndexOf(FILE_SEPARATOR) + 1, file.length());
    }
    
    
    private File getDestinationFile(String fileName, String destinationPath) {
        
        String destFileName;
        
        if (!rootFolder.equals(FILE_SEPARATOR)) {
            destFileName = destinationPath + File.separator + fileName
            .replaceFirst(rootFolder, "").replace(FILE_SEPARATOR, File.separator);
        } else {
            destFileName = destinationPath + File.separator + fileName
            .replace(FILE_SEPARATOR, File.separator);
        }
        
        return new File(destFileName);
        
    }
    
    
    private boolean equals(String file1, String file2) {
        return file1.toLowerCase().equals(file2.toLowerCase());
    }
    
    
    private boolean fileExists(String fileName) {
        boolean inserted = false;
        for (EncryptedFileMetadata fileMetadata : fileMetadataList) {
            if (equals(fileMetadata.getFileName(), fileName)) {
                inserted = true;
                break;
            }
        }
        return inserted;
    }
    
    
    private boolean folderExists(String folderName) {
        boolean inserted = false;
        for (String folder : folders) {
            if (equals(folder, folderName)) {
                inserted = true;
                break;
            }
        }
        return inserted;
    }

    
    private boolean isFilePath(String path) {
        boolean value = false;
        for (EncryptedFileMetadata fileMetadata : fileMetadataList) {
            if (equals(fileMetadata.getFileName(), path)) {
                value = true;
                break;
            }
        }
        return value;
    }
    
    
    private boolean isFolderPath(String path) {
        boolean value = false;
        for (String folderName : folders) {
            if (equals(path, folderName)) {
                value = true;
                break;
            }
        }
        return value;
    }
    
    
    private boolean isEmptyFolder(EncryptedFileMetadata fileMetadata) {
        return fileMetadata.getInternalFileName().equals(EMPTY_FOLDER_TAG);
    }
    
    
    private String adaptFilePath(String filePath) {
        
        String parent = filePath;
        List<String> filesList = new ArrayList<>();
        
        do {
            filesList.add(extractFileName(parent));
            parent = getParentPath(parent);            
        } while (!parent.equals(FILE_SEPARATOR));
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = filesList.size() - 1; i >= 0; i--) {
            sb.append(FILE_SEPARATOR);
            sb.append(filesList.get(i));           
        }
        
        return sb.toString();
        
    }
    
    
    private EncryptedFileMetadata createEmptyFolder(String folderPath) throws Exception {
        EncryptedFileMetadata fileMetadata = new EncryptedFileMetadata();
        fileMetadata.setFileName(getRelativeFolderPath(folderPath));
        fileMetadata.setInternalFileName(EMPTY_FOLDER_TAG);
        return fileMetadata;
    }
    
    
    private List<EncryptedFileMetadata> extractEmptyFolders(File folder) throws Exception {
        
        List<EncryptedFileMetadata> emptyFolders = new ArrayList<>();
        int filesNumber = 0;
        int foldersNumber = 0;
        
        List<File> filesList = new ArrayList<>();
        List<File> foldersList = new ArrayList<>();
        listFilesFromFolder(folder, filesList);
        filesNumber += filesList.size();
        
        List<File> subfolders = new ArrayList<>();
        listSubfoldersFromFolder(folder, subfolders);  
        foldersNumber += subfolders.size();
        
        for (File subfolder : subfolders) {
            
            filesList.clear();
            foldersList.clear();
            listFilesFromFolder(subfolder, filesList);
            filesNumber += filesList.size();
            
            listSubfoldersFromFolder(subfolder, foldersList);
            
            foldersNumber += foldersList.size();
            
            if (filesList.isEmpty() && foldersList.isEmpty()) {
                String subfolderName = getInternalFolderPath(folder, subfolder);
                emptyFolders.add(createEmptyFolder(subfolderName));
            }
            
        }
        
        if ((filesNumber == 0) && (foldersNumber == 0)) {
            String folderName = getInternalFolderPath(folder);
            emptyFolders.add(createEmptyFolder(folderName));
        }
        
        return emptyFolders;
        
    }
    
    
    private List<String> getFolders() {
        
        List<String> foldersList = new ArrayList<>();
        foldersList.add(FILE_SEPARATOR);
        
        for (EncryptedFileMetadata fileMetadata : fileMetadataList) {
            
            String fileName = fileMetadata.getFileName();
            int lastIndex = fileName.lastIndexOf(FILE_SEPARATOR);
            String parent = fileName.substring(0, lastIndex == 0 ? 1 : lastIndex);
            String[] splitPath = parent.split(FILE_SEPARATOR);
            
            for (int i = 1; i < splitPath.length; i++) {
                
                StringBuilder sb = new StringBuilder();
                
                for (int j = 1; j <= i; j++) {
                    sb.append(FILE_SEPARATOR);
                    sb.append(splitPath[j]);                    
                }
                
                String folderPath = sb.toString();
                
                if (!foldersList.contains(folderPath)) {
                    foldersList.add(folderPath);
                }
                
            }
            
            if (isEmptyFolder(fileMetadata)) {
                if (!foldersList.contains(fileMetadata.getFileName())) {
                    foldersList.add(fileMetadata.getFileName());
                }
            }
            
        }
        
        return foldersList;
        
    }
    
    
    private List<EncryptedFileMetadata> getEmptyFolders() {
        
        List<EncryptedFileMetadata> emptyFolders = new ArrayList<>();
        
        for (EncryptedFileMetadata fileMetadata : fileMetadataList) {
            if (isEmptyFolder(fileMetadata)) {
                emptyFolders.add(fileMetadata);
            }
        }
        
        return emptyFolders;
        
    }
    
    
    /**
     * Obter todos os arquivos criptografados.
     * @return lista com todos os arquivos criptografados.
     */
    public List<FileEntry> getAllFiles() {
        
        List<FileEntry> encryptedFiles = new ArrayList<>();
        
        for (EncryptedFileMetadata fileMetadata : fileMetadataList) {
            encryptedFiles.add(new FileEntry(fileMetadata));
        }
        
        encryptedFiles.sort(new EncryptedFileComparator());
        
        return encryptedFiles;
    }
    
    
    /**
     * Obter os arquivos criptografados de um diretório.
     * @param folderName diretório que será obtida a lista de arquivos.
     * @return lista dos arquivos criptografados do diretório.
     */
    public List<FileEntry> getFilesFromFolder(String folderName) {
        
        List<FileEntry> encryptedFiles = new ArrayList<>();
        
        for (EncryptedFileMetadata fileMetadata : fileMetadataList) {
            
            if (!isEmptyFolder(fileMetadata)) {
                
                String fileName = fileMetadata.getFileName();
                String parent = getParentPath(fileName);
                
                if (equals(folderName, parent)) {
                    encryptedFiles.add(new FileEntry(fileMetadata));
                }
                
            }
            
        }
        
        encryptedFiles.sort(new EncryptedFileComparator());
        
        return encryptedFiles;
        
    }
    
    
    /**
     * Obter os subdiretórios de um diretório.
     * @param folderName diretório que será obtida a lista de subdiretórios.
     * @return lista dos subdiretórios do diretório. 
     */
    public List<FileEntry> getSubfoldersFromFolder(String folderName) {
        
        List<FileEntry> encryptedFiles = new ArrayList<>();
        
        for (String folder : folders) {
            if (!equals(folderName, folder)) {
                String parent = getParentPath(folder);
                if (equals(folderName, parent)) {
                    encryptedFiles.add(new FileEntry(null, folder, 0, 0, 0,
                    true, false));
                }
            }
        }
        
        encryptedFiles.sort(new EncryptedFileComparator());
        
        return encryptedFiles;
        
    }
    
    
    /**
     * Obter todos os subdiretórios de um diretório.
     * @param folderName diretório que será obtida a lista de subdiretórios.
     * @return lista de subdiretórios do diretório.
     */
    private List<String> getAllSubfoldersFromFolder(String folderName) {
        
        List<String> foldersList = new ArrayList<>();
        
        for (String folder : folders) {
            
            if (!equals(folder, folderName)) {
                
                boolean isParent = false;
                String parent = folder;
                
                do {
                    parent = getParentPath(parent);
                    if (equals(parent, folderName)) {
                        isParent = true;
                        break;
                    }
                } while (!parent.equals(FILE_SEPARATOR));
                
                if (isParent) {
                    foldersList.add(folder);
                }
                
            }
            
        }
        
        return foldersList;
        
    }
    
    
    /**
     * Definir o diretório raiz para realizar operações com o arquivo dentro
     * deste. O diretório raiz da hierarquia interna é /.
     * 
     * <br><br>
     * 
     * Se definir como /Teste/Arquivos, este será o diretório raiz. Se inserir
     * um arquivo ou diretório, este será inserido dentro deste diretório. Desta
     * forma, ele funciona como um cursor para o arquivo.
     * @param rootFolder diretório raiz.
     * @throws Exception 
     */
    public void setRootFolder(String rootFolder) throws Exception {
        if (!folders.contains(rootFolder)) {
            throw new Exception("Diretório raiz inválido");
        }
        this.rootFolder = rootFolder;
    }

    
    /**
     * Obter o diretório raiz.
     * @return diretório raiz.
     */
    public String getRootFolder() {
        return rootFolder;
    }
    
    
    /**
     * Obter todos os arquivos no diretório raiz.
     * @return lista dos arquivos do diretório raiz.
     */
    public List<FileEntry> getFilesFromRootFolder() {
        return getFilesFromFolder(rootFolder);
    }
    
    
    /**
     * Obter todos os subdiretórios do diretório raiz.
     * @return lista dos subdiretórios do diretório raiz.
     */
    public List<FileEntry> getSubfoldersFromRootFolder() {
        return getSubfoldersFromFolder(rootFolder);
    }
    
    
    /**
     * Obter a árvore de diretórios internos.
     * @return lista com todos os diretórios internos.
     */
    public List<String> getFoldersTree() {
        List<String> foldersList = new ArrayList<>(folders.size());
        foldersList.addAll(folders);
        return foldersList;
    }
    
    
    /**
     * Obter todos os arquivos de um determinado diretório em disco.
     * @param folder diretório a se obter os arquivos.
     * @param files lista dos arquivos no diretório.
     */
    private void listFilesFromFolder(File folder, List<File> files) {
        Collections.addAll(
            files,
            folder.listFiles(new FileFilter())
        );
    }
 
    
    /**
     * Obter todos os subdiretórios de um determinado diretório em disco.
     * @param folder diretório a se obter os subdiretórios.
     * @param subfolders lista dos subdiretórios no diretório.
     */
    private void listSubfoldersFromFolder(File folder, List<File> subfolders) {
        File[] foldersList = folder.listFiles(new DirectoryFilter());
        if (foldersList != null) {
            for (File subfolder : foldersList) {
                if (abort()) break;
                subfolders.add(subfolder);
                listSubfoldersFromFolder(subfolder, subfolders);
            }
        }
    }
    
    
    private long calculateSize(List<EncryptedFileMetadata> filesMetadata, int passes) {
        long size = 0;
        for (EncryptedFileMetadata fileMetadata : filesMetadata) {
            if (!isEmptyFolder(fileMetadata)) {
                long length = fileMetadata.getOriginalSize();
                size += (length > 0 ? length * passes : 0); 
            }
        }
        return size;
    }
    
    
    public int getNumberOfFiles() {
        int counter = 0;
        for (EncryptedFileMetadata fileMetadata : fileMetadataList) {
            if (!isEmptyFolder(fileMetadata)) {
                counter++;
            }
        }
        return counter;
    }

    
    public void addListener(ProcessListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    
    public void removeListener(ProcessListener listener) {
        int idx = -1;
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i) == listener) {
                idx = i;
                break;
            }
        }
        listeners.remove(idx);
    }

    
    public List<ProcessListener> getListeners() {
        return listeners;
    }
    
    
    private void reset(boolean blockAbort, long totalBytesCounter) {
        this.totalLength = totalBytesCounter;
        this.blockAbort = blockAbort;
        this.totalBytesCounter = 0;
        this.totalPercentage = 0;
        this.abort = false;
    }
    
    
    private void updateFileInProcess(String fileName, FileOperation operation, long fileLength) {
        fileBytesCounter = filePercentage = 0;
        this.fileLength = fileLength;
        for (ProcessListener listener : listeners) {
            listener.updateFile(fileName, operation);
            listener.updateFilePercentage(filePercentage);
        }
    }
    
    
    private void notify(long length) {
        
        if (fileLength > 0) {
            fileBytesCounter += length;
            int percentage = (int)((fileBytesCounter * 100) / fileLength);
            if (percentage > filePercentage) {
                filePercentage = percentage;
                for (ProcessListener listener : listeners) {
                    listener.updateFilePercentage(filePercentage);
                }
            }
        }
        
        if (totalLength > 0) {
            totalBytesCounter += length;
            int percentage = (int)((totalBytesCounter * 100) / totalLength);
            if (percentage > totalPercentage) {
                totalPercentage = percentage;
                for (ProcessListener listener : listeners) {
                    listener.updateTotalPercentage(totalPercentage);
                }
            }
        }
        
    }
    
    
    @Override
    public boolean abort() {
        if (blockAbort) return false;
        for (ProcessListener listener : listeners) {
            if (listener.abort()) {
                abort = true;
                break;
            }
        }
        return abort;
    }

    
    @Override
    public void update(long numberOfBytes) {
        notify(numberOfBytes);
    }
     
    
    private void lock() throws FileNotFoundException, IOException {
        if (fileChannel == null && fileLock == null) {
            fileChannel = new RandomAccessFile(getFile(), "rw").getChannel();
            fileLock = fileChannel.tryLock();
        }
    }
    
    
    private void release() throws IOException {
        if (fileChannel != null && fileLock != null) {
            fileLock.release();
            fileChannel.close();
            fileLock = null;
            fileChannel = null;
        }
    }
    
    
    public void close() throws IOException {
        release();
    }
    
    
}