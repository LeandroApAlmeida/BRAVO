package bravo.environment;

import bravo.file.CipherListener;
import bravo.filter.DirectoryFilter;
import bravo.filter.FileComparator;
import bravo.filter.FileFilter;
import bravo.file.FileOperation;
import bravo.file.ProcessListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Classe para destruição de arquivos em cache, impedindo a recuperação dos
 * mesmos após o encerramento da sessão do programa.
 * 
 * @since 1.0
 */
public final class CacheCleaner {
    
    
    // Padrões de bytes para sobrescrita dos arquivos.
    
    private final byte[] PATTERN_1_BYTE = new byte[] {
        (byte)0x00, (byte)0x11, (byte)0x22,
        (byte)0x33, (byte)0x44, (byte)0x55,
        (byte)0x66, (byte)0x77, (byte)0x88,
        (byte)0x99, (byte)0xAA, (byte)0xBB,
        (byte)0xCC, (byte)0xDD, (byte)0xEE, 
        (byte)0xFF
    };

    private final byte[][] PATTERN_3_BYTES = new byte[][] {
        {(byte)0x92, (byte)0x49, (byte)0x24},
        {(byte)0x49, (byte)0x24, (byte)0x92},
        {(byte)0x24, (byte)0x92, (byte)0x49},
        {(byte)0x6D, (byte)0xB6, (byte)0xDB},
        {(byte)0xB6, (byte)0xDB, (byte)0x6D},
        {(byte)0xDB, (byte)0x6D, (byte)0xB6}
    };
    
    private final byte BYTE_00 = (byte)0x00;
    
    private final byte BYTE_55 = (byte)0x55;
    
    private final byte BYTE_AA = (byte)0xAA;
    
    private final byte BYTE_FF = (byte)0xFF;
    
    
    /**Número padrão de passos para sobrescrita com bytes aleatórios.*/
    private final int RANDOM_PASSES = 10;
    
    /**Tamanho do buffer de escrita.*/
    private final int BUFFER_SIZE = 4096;
    
    /**Nome do diretório de cache.*/
    private final String INSTANCE_FOLDER_NAME = "instance";
    
    /**Método de sobrescrita de arquivos.*/
    private final WipeMethod method;
    
    /**Parâmetros para o método de sobrescrita de arquivos.*/
    private final Object[] params;
    
    /**Ouvintes de processo.*/
    private final List<ProcessListener> processlisteners;
    
    /**Ouvintes de processo.*/
    private final List<CipherListener> cipherListeners;
    
    /**Percentual do processamento do arquivo corrente.*/
    private int filePercentage;
    
    /**Percentual do processamento total.*/
    private int totalPercentage;
    
    /**Total de bytes a processar.*/
    private long totalBytesCounter;
    
    /**Total de bytes do arquivo corrente.*/
    private long fileBytesCounter;
    
    /**Total de bytes processados do arquivo corrente.*/
    private long processedFileBytes;
    
    /**Total de bytes processados.*/
    private long processedTotalBytes;

    
    /**
     * Constructor da classe. Permite a seleção do método de sobrescrita dos
     * arquivos em disco.
     * 
     * @param method método de sobrescrita dos arquivos.
     * @param params parâmetros para o método de sobrescrita de arquivos.
     */
    public CacheCleaner(WipeMethod method, Object... params) {
        processlisteners = new ArrayList<>();
        cipherListeners = new ArrayList<>();
        this.method = method;
        this.params = params;
    }
    
    
    /**
     * Constructor padrão. Usa o método DoD 5220.22-M desenvolvido pelo departamento
     * de defesa dos Estados Unidos.
     */
    public CacheCleaner() {
        this(WipeMethod.DOD522022M_ALGORITHM, false);
    }
    
    
    /**
     * Obter o número de passos (ciclos de substituição) para o método de
     * sobrescrita de arquivos definido.
     * @return número de passos do método de sobrescrita de arquivos.
     */
    public int getPassesByMethod() {
        int passes = 0;
        switch (method) {
            case FIXED_BYTE -> passes = 1;
            case RANDOM_BYTES -> passes = (params.length == 0 ? RANDOM_PASSES : (int)params[1]);
            case BRUCE_SCHNEIER_ALGORITHM -> passes = 7;
            case DOD522022M_ALGORITHM -> passes = ((boolean) params[0] ? 7 : 3);
            case GUTMANN_ALGORITHM -> passes = ((boolean) params[0] ? 18 : 35);
            case VSITR_ALGORITHM -> passes = 8;
        }
        return passes;
    }

    
    /**
     * Limpar o cache da sessão corrente.
     * @throws Exception
     */
    public void cleanCurrentSessionCache() throws Exception {
        try {
            File cacheFolder = RootFolder.getSessionFolder();
            List<File> list = new ArrayList<>();
            list.add(cacheFolder);
            reset(calculateSize(list, getPassesByMethod()));
            wipeFilesAndFolders(list);
        } finally {
            for (ProcessListener listener : processlisteners) {
                listener.done();
            }
        }
    }
    
    
    /**
     * Limpar o cache da sessão anterior. Eventualmente, pode ocorrer de o 
     * programa ser encerrado de forma anormal, e, neste caso, vai ficar os
     * arquivos de sessão sem serem destruídos.
     * @throws Exception
     */
    public void cleanPreviousSessionCache() throws Exception {
        
        List<String> activeInstances = new ArrayList<>();
        activeInstances.add(RootFolder.getSessionFolder().getName());
        
        File instanceFolder = new File(RootFolder.getAbsolutePath() + 
        File.separator + INSTANCE_FOLDER_NAME);
        
        if (instanceFolder.exists()) {
            File[] files = instanceFolder.listFiles(new FileFilter());
            for (File file : files) {
                if (!file.delete()) {
                    activeInstances.add(file.getName());
                }
            }
        } else {
            instanceFolder.mkdirs();
        }
        
        final File file = new File(
            instanceFolder.getAbsolutePath() +
            File.separator + 
            RootFolder.getSessionFolder().getName()
        );
        
        final RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        final FileLock fLock = raFile.getChannel().tryLock();
        boolean lock = (fLock != null);
        
        if (lock) {
            Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {                            
                        try {                                
                            fLock.release();
                            raFile.close();
                            file.delete();
                        } catch (IOException ex) {
                        }
                    }
                }
            );
        }
        
        List<File> foldersToDelete = new ArrayList<>();
        File[] folders = RootFolder.getCacheFolder().listFiles(new DirectoryFilter());
        
        for (File folder : folders) {
            if (!activeInstances.contains(folder.getName())) {
                foldersToDelete.add(folder);
            }
        }
        
        if (!foldersToDelete.isEmpty()) {
            cleanExternalFiles(foldersToDelete);
        } else {
            processlisteners.forEach(
                listener -> {listener.done();}
            );
        }
        
    }
    
    
    /**
     * Verificar se há cache de sessão anterior que não foi limpo.
     * @return Se true, há cache de sessão anterior. Se false, não há
     * cache de sessão anterior.
     */
    public boolean previousSessionCacheIsNotEmpty() {
        
        File instanceFolder = new File(
            RootFolder.getAbsolutePath() + 
            File.separator +
            INSTANCE_FOLDER_NAME
        );
        
        if (instanceFolder.exists()) {
            File[] files = instanceFolder.listFiles(new FileFilter());
            if (files.length > 0) return true;
        }
        
        File rootCacheFolder = RootFolder.getCacheFolder();
        
        if (rootCacheFolder.exists()) {
            File[] folders = rootCacheFolder.listFiles(new DirectoryFilter());
            if (folders.length > 0) {
                for (File folder : folders) {
                    if (!folder.equals(RootFolder.getSessionFolder())) {
                        return true;
                    }
                }                
            }
        }
        
        return false;
        
    }

    
    /**
     * Apagar arquivos externos ao diretório de cache.
     * @param filesAndFolders lista dos arquivos e diretórios a serem apagados.
     * @throws java.lang.Exception
     */
    public void cleanExternalFiles(List<File> filesAndFolders) throws Exception {
        try {
            reset(calculateSize(filesAndFolders, getPassesByMethod()));
            wipeFilesAndFolders(filesAndFolders);
        } finally {
            for (ProcessListener listener : processlisteners) {
                listener.done();
            }
        }
    }
    
    
    /**
     * Apagar os arquivos e diretórios passados.
     * @param filesAndFolders lista dos arquivos e diretórios a serem apagados.
     * @throws Exception
     */
    private void wipeFilesAndFolders(List<File> filesAndFolders) throws Exception {
        
        List<File> filesList = new ArrayList<>();
        List<File> foldersList = new ArrayList<>();
        
        for (File file : filesAndFolders) {
            
            if (file.isDirectory()) {
                
                foldersList.add(file);
                listFilesFromFolder(file, filesList);
                
                List<File> subfolders = new ArrayList<>();
                listSubfoldersFromFolder(file, subfolders);
                
                for (File subfolder : subfolders) {
                    listFilesFromFolder(subfolder, filesList);
                }
                
                foldersList.addAll(subfolders);
                
            } else {
                
                filesList.add(file);
                
            }
            
        }
        
        for (File file : filesList) {     
            wipeFile(file);
        }
        
        foldersList.sort(new FileComparator());
        
        for (int i = foldersList.size() -1 ; i >= 0; i--) {
            unNameAndDeleteFile(foldersList.get(i));
        }
        
    }
    
    
    /**
     * Apagar o arquivo passado usando o método de sobrescrita de arquivos
     * definido. O método padrão é DoD 5220.22-M.
     * @param file arquivo a ser apagado.
     * @throws Exception 
     */
    private void wipeFile(File file) throws Exception {
        
        Files.setAttribute(file.toPath(), "dos:readonly", false);
        
        int passes = getPassesByMethod();
        
        updateFileInProcess(file.getAbsolutePath(), file.length(), passes);
        
        switch (method) {
            
            case FIXED_BYTE -> {
                wipeFileWithFixedByteAlgorithm(file, (byte) params[0]);                
            }
            
            case RANDOM_BYTES -> {
                switch (params.length) {
                    case 0 -> wipeFileWithRandomBytesAlgorithm(file);
                    case 2 -> wipeFileWithRandomBytesAlgorithm(file, (int)params[1]);
                }
            }
            
            case BRUCE_SCHNEIER_ALGORITHM -> {
                wipeFileWithBruceSchneierAlgorithm(file);
            }
            
            case DOD522022M_ALGORITHM -> {
                wipeFileWithDoD522022MAlgorithm(file, (boolean) params[0]);
            }
            
            case GUTMANN_ALGORITHM -> {
                wipeFileWithGutmannAlgorithm(file, (boolean) params[0]);
            }
            
            case VSITR_ALGORITHM -> {
                wipeFileWithVSITRAlgorithm(file);
            }
            
        }
        
    }


    /**
     * Apagar o arquivo usando o método de Byte Fixo.
     * @param file arquivo a ser apagado.
     * @param data byte para sobrescrita.
     * @throws IOException 
     */
    private void wipeFileWithFixedByteAlgorithm(File file, byte data) throws IOException {
        writeFileWithOneByte(file, data);
        unNameAndDeleteFile(file);
    }

    
    /**
     * Apagar o arquivo usando o método de Bytes Aleatórios, em 10 passos.
     * @param file arquivo a ser apagado.
     * @throws IOException 
     */
    private void wipeFileWithRandomBytesAlgorithm(File file) throws IOException {
        writeFileWithRandomBytes(file, RANDOM_PASSES);
        unNameAndDeleteFile(file);
    }
    
    
    /**
     * Apagar o arquivo usando o método de Bytes Aleatórios.
     * @param file arquivo a ser apagado.
     * @param passes número de passos.
     * @throws IOException 
     */
    private void wipeFileWithRandomBytesAlgorithm(File file, int passes) throws IOException {
        writeFileWithRandomBytes(file, passes);
        unNameAndDeleteFile(file);
    }

    
    /**
     * Apagar o arquivo usando o método de Gutmann.
     * @param file arquivo a ser apagado.
     * @param floppyMode Se true, processa em 18 passos. Se false, 
     * processa em 35 passos.
     * @throws IOException 
     */
    private void wipeFileWithGutmannAlgorithm(File file, boolean floppyMode) throws IOException {
        
        Integer[] sequence;
        
        if (floppyMode) {
            sequence = new Integer[]{5, 5, 6, 6, 7, 7, 8, 8, 9, 9};
        } else {
            sequence = new Integer[]{
                5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
            };
        }
        
        List<Integer> list = Arrays.asList(sequence);
        Collections.shuffle(list);
        sequence = list.toArray(Integer[]::new);
        
        writeFileWithRandomBytes(file, 4);
        
        for(int i = 0; i < sequence.length; i++) {
            if (sequence[i] == 5) {
                writeFileWithOneByte(file, BYTE_55);
            } else if (sequence[i] == 6) {
                writeFileWithOneByte(file, BYTE_AA);
            } else if (sequence[i] < 10) {
                writeFileWithThreeBytes(file, PATTERN_3_BYTES[sequence[i]-7]);
            } else if (sequence[i] < 26) {
                writeFileWithOneByte(file, PATTERN_1_BYTE[sequence[i]-10]);
            } else {
                writeFileWithThreeBytes(file, PATTERN_3_BYTES[sequence[i]-26]);
            }
        }
        
        writeFileWithRandomBytes(file, 4);
        
        unNameAndDeleteFile(file);
        
    }

    
    /**
     * Apagar o arquivo usando o método VSITR.
     * @param file arquivo a ser apagado.
     * @throws IOException 
     */
    private void wipeFileWithVSITRAlgorithm(File file) throws IOException {
        writeFileWithOneByte(file, BYTE_00);
        writeFileWithOneByte(file, BYTE_FF);
        writeFileWithOneByte(file, BYTE_00);
        writeFileWithOneByte(file, BYTE_FF);
        writeFileWithOneByte(file, BYTE_00);
        writeFileWithOneByte(file, BYTE_FF);
        writeFileWithOneByte(file, BYTE_AA);
        writeFileWithRandomBytes(file, 1);
        unNameAndDeleteFile(file);
    }

    
    /**
     * Apagar o arquivo usando o método de Bruce Schneier.
     * @param file arquivo a ser apagado.
     * @throws IOException 
     */
    private void wipeFileWithBruceSchneierAlgorithm(File file) throws IOException {
        writeFileWithOneByte(file, BYTE_00);
        writeFileWithOneByte(file, BYTE_FF);
        writeFileWithRandomBytes(file, 5);
        unNameAndDeleteFile(file);
    }

    
    /**
     * Apagar o arquivo usando o método DoD 5220.22-M.
     * @param file arquivo a ser apagado.
     * @param extended Se true, usa o método DoD 5220.22-M ECE (7 passos). Se 
     * false, usa o método DoD 5220.22-M (3 passos).
     * @throws IOException 
     */
    private void wipeFileWithDoD522022MAlgorithm(File file, boolean extended) throws IOException {
        if (extended) {
            writeFileWithRandomBytes(file, 1);
            writeFileWithOneByte(file, BYTE_55);
            writeFileWithOneByte(file, BYTE_AA);
            writeFileWithRandomBytes(file, 1);
            writeFileWithOneByte(file, BYTE_00);
            writeFileWithOneByte(file, BYTE_FF);
            writeFileWithRandomBytes(file, 1);
            unNameAndDeleteFile(file);
        } else {
            writeFileWithOneByte(file, BYTE_00);
            writeFileWithOneByte(file, BYTE_FF);
            writeFileWithRandomBytes(file, 1);
            unNameAndDeleteFile(file);
        }
    }


    /**
     * Sobrescrever o arquivo com um byte específico.
     * @param file arquivo a ser sobrescrito.
     * @param data byte a sobrescrever o arquivo.
     * @throws IOException 
     */
    private void writeFileWithOneByte(File file, byte data) throws IOException {
        
        byte[] pattern = new byte[BUFFER_SIZE];
        
        long loops = file.length() / BUFFER_SIZE;
        
        int rest = (int)(file.length() % BUFFER_SIZE);
        
        Arrays.fill(pattern, data);
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            
            for (long i = 1; i <= loops; i++) {
                raf.write(pattern);                
                notify(pattern.length);
            }
            
            if (rest != 0) {
                raf.write(pattern, 0, rest);
                notify(rest);                       
            }
            
        }
        
    }
    
    
    /**
     * Sobrescrever o arquivo com bytes pseudo-aleatórios.
     * @param file arquivo a ser sobrescrito.
     * @param passes número de passos.
     * @throws IOException 
     */
    private void writeFileWithRandomBytes(File file, int passes) throws IOException {
        
        byte[] pattern = new byte[BUFFER_SIZE];

        long loops = file.length() / BUFFER_SIZE;

        int rest = (int) (file.length() % BUFFER_SIZE);

        SecureRandom secureRandom = new SecureRandom();
        
        for (int i = 0; i < passes; i++) {

            try (RandomAccessFile os = new RandomAccessFile(file, "rw")) {

                for (long j = 1; j <= loops; j++) {
                    secureRandom.nextBytes(pattern);
                    os.write(pattern);
                    notify(pattern.length);
                }

                if (rest != 0) {
                    secureRandom.nextBytes(pattern);
                    os.write(pattern, 0, rest);
                    notify(rest);
                }

            }
        }
        
    }

    
    /**
     * Sobrescrever o arquivo com um padrão de 3 bytes.
     * @param file arquivo a ser sobrescrito.
     * @param data padrão de 3 bytes.
     * @throws IOException 
     */
    private void writeFileWithThreeBytes(File file, byte[] data) throws IOException {
        
        int arrayLength = 4095;
        
        byte[] pattern = new byte[arrayLength];
        
        long loops = file.length() / arrayLength;
        
        int rest = (int) (file.length() % arrayLength);
        
        for (int i = 0; i < arrayLength; i = i + 3) {
            pattern[i] = data[0];
            pattern[i+1] = data[1];
            pattern[i+2] = data[2];
        }
        
        try (RandomAccessFile os = new RandomAccessFile(file, "rw")) {
            
            for (long i = 1; i <= loops; i++) {
                os.write(pattern);
                notify(pattern.length);
            }
            
            if (rest != 0) {
                os.write(pattern, 0, rest);
                notify(rest);
            }
            
        }
        
    }


    /**
     * Remover a entrada do arquivo da tabela do sistema de arquivos.
     * @param file arquivo a ser removido.
     * @throws IOException 
     */
    private void unNameAndDeleteFile(File file) throws IOException {
        int fileNameLength = file.getName().length();
        file = generateShorterFilename(file, 0);
        while (fileNameLength > 1) {
            file = generateShorterFilename(file, 1);
            fileNameLength--;
        }
        file.delete();
    }

    
    private File generateShorterFilename(File file, int shorten) throws IOException {
        
        boolean isFile = file.isFile();
        
        String filePath = file.getParent() + "/";
        int nameLength = file.getName().length();
        int point = nameLength - file.getName().lastIndexOf(".") - 1;
        nameLength = nameLength - shorten;
        
        int position = 0;
        String newName = "";
        int randomInt;
        char randomChar;
        boolean renamed;
        
        SecureRandom secureRandom = new SecureRandom();
        
        while(position < nameLength) {
            if (position == point && isFile) {
                newName = "." + newName;
                position++;
            } else {
                randomInt = secureRandom.nextInt(51);
                if (randomInt < 25) {
                    randomInt = randomInt + 65;
                } else {
                    randomInt = randomInt + 72;
                }
                randomChar = (char)randomInt;
                newName = randomChar + newName;
                position++;
            }
        }
        
        if ((point == nameLength) && (nameLength != 1) && isFile) {
            newName = "." + newName.substring(1, newName.length());
        }
        
        File test = new File(filePath + newName);
        
        if (test.exists()) {
            return generateShorterFilename(file, shorten);            
        } else {
            renamed = file.renameTo(new File(filePath + newName));
            if (!renamed) {
                throw new IOException("Arquivo não pode ser renomeado.");
            }
            return new File (filePath + newName);
        }
        
    }

    
    /**
     * Notificar os ouvintes do processamento.
     * @param length número de bytes processados.
     */
    private void notify(long length) {
        
        if (fileBytesCounter > 0) {
            processedFileBytes += length;
            int percentage = (int)((processedFileBytes * 100) / fileBytesCounter);
            if (percentage > filePercentage) {
                filePercentage = percentage;
                for (ProcessListener listener : processlisteners) {
                    listener.updateFilePercentage(filePercentage);
                    listener.updateTotalPercentage(totalPercentage);
                }
            }
        }
        
        if (totalBytesCounter > 0) {
            processedTotalBytes += length;
            int percentage = (int)((processedTotalBytes * 100) / totalBytesCounter);
            if (percentage > totalPercentage) {
                totalPercentage = percentage;
                for (ProcessListener listener : processlisteners) {
                    listener.updateFilePercentage(filePercentage);
                    listener.updateTotalPercentage(totalPercentage);
                }
            }
        }
        
        for (CipherListener listener : cipherListeners) {
            listener.update(length);
        }
        
    } 
    
    
    /**
     * Restaurar contadores.
     * @param totalBytesCounter Número total de bytes a processar.
     */
    private void reset(long totalBytesCounter) {
        this.totalBytesCounter = totalBytesCounter;
        processedTotalBytes = 0;
        totalPercentage = 0;
    }
    
    
    /**
     * Notificar o processamento do arquivo corrente.
     * @param fileName nome do arquivo
     * @param fileSize tamanho do arquivo em bytes.
     * @param passes número de passos do algoritmo de limpeza de arquivos.
     */
    private void updateFileInProcess(String fileName, long fileSize, int passes) {
        processedFileBytes = filePercentage = 0;
        fileBytesCounter = fileSize * passes;
        for (ProcessListener listener : processlisteners) {
            listener.updateFile(fileName, FileOperation.WIPE);
            listener.updateFilePercentage(filePercentage);
        }
    }
    
    
    /**
     * Listar todos os arquivos de um diretório.
     * @param folder diretório
     * @param files lista de arquivos.
     */
    private void listFilesFromFolder(File folder, List<File> files) {
        Collections.addAll(files, folder.listFiles(new FileFilter()));
    }
    
    
    /**
     * Listar todos os subdiretórios de um diretório.
     * @param folder diretório.
     * @param subfolders lista de subdiretórios.
     */
    private void listSubfoldersFromFolder(File folder, List<File> subfolders) {
        File[] folders = folder.listFiles(new DirectoryFilter());
        if (folders != null) {
            for (File subfolder : folders) {
                subfolders.add(subfolder);
                listSubfoldersFromFolder(subfolder, subfolders);
            }
        }
    }

    
    /**
     * Calcular o número de bytes a serem processados.
     * @param filesList lista de arquivos.
     * @param passes número de passos do algoritmo de limpeza.
     * @return número total de bytes a serem processados.
     * @throws Exception 
     */
    private long calculateSize(List<File> filesList, int passes) throws Exception {
        
        List<File> files = new ArrayList<>();
        
        for (File file : filesList) {
            if (file.isDirectory()) {
                listFilesFromFolder(file, files);
                List<File> folders = new ArrayList<>();
                listSubfoldersFromFolder(file, folders);
                for (File folder : folders) {
                    listFilesFromFolder(folder, files);
                }
            } else {
                files.add(file);
            }
        }
        
        long size = 0;
        
        for (File file : files) {
            size += (file.length() > 0 ? file.length() * passes : 0);
        }
        
        return size;
        
    }
    
    
    /**
     * Adicionar um ouvinte de processo.
     * @param listener ouvinte de processo.
     */
    public void addListener(ProcessListener listener) {
        if (!processlisteners.contains(listener)) {
            processlisteners.add(listener);
        }
    }
    
    
    /**
     * Remover um ouvinte de processo.
     * @param listener ouvinte de processo.
     */
    public void removeListener(ProcessListener listener) {
        int idx = -1;
        for (int i = 0; i < processlisteners.size(); i++) {
            if (processlisteners.get(i) == listener) {
                idx = i;
                break;
            }
        }
        processlisteners.remove(idx);
    }
    
    
    /**
     * Adicionar um ouvinte de processo.
     * @param listener ouvinte de processo.
     */
    public void addListener(CipherListener listener) {
        if (!cipherListeners.contains(listener)) {
            cipherListeners.add(listener);
        }
    }
    
    
    /**
     * Remover um ouvinte de processo.
     * @param listener ouvinte de processo.
     */
    public void removeListener(CipherListener listener) {
        int idx = -1;
        for (int i = 0; i < cipherListeners.size(); i++) {
            if (cipherListeners.get(i) == listener) {
                idx = i;
                break;
            }
        }
        cipherListeners.remove(idx);
    }


}