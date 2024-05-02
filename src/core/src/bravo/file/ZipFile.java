package bravo.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import static net.lingala.zip4j.util.InternalZipConstants.ZIP_FILE_SEPARATOR;

/**
 * Classe para ler e gravar dados em um arquivo no formaro ZIP. A classe se utiliza
 * da biblioteca Zip4j de autoria de Srikanth Reddy Lingala, disponível em
 * <a href="https://github.com/srikanth-lingala/zip4j">https://github.com/srikanth-lingala/zip4j</a>
 * para ralizar estas operações.
 * 
 * <br><br>
 * 
 * A opção pela biblioteca foi pela praticidade oferecida para gerenciamento do
 * arquivo ZIP oferecida pela mesma, porém, em momento algum se utiliza a função
 * de criptografia interna à esta biblioteca, optando para isso pela biblioteca
 * BouncyCastle disponível em  <a href="https://www.bouncycastle.org/">https://www.bouncycastle.org/</a>.
 * 
 * @since 2.0
 */
class ZipFile {
    
    
    /**Separador interno de arquivos e pastas. Segue a convenção do pacote Zip4j.*/
    public static final String FILE_SEPARATOR = ZIP_FILE_SEPARATOR;
    
    /**Classe para manutenção de arquivos em formato ZIP.*/
    private final net.lingala.zip4j.ZipFile zipFile;
    
    /**Arquivo ZIP em disco.*/
    protected File file;

    
    /**
     * Constructor padrão.
     * @param file arquivo ZIP.
     */
    public ZipFile(File file) {
        this.file = file;
        zipFile = new net.lingala.zip4j.ZipFile(file);
        zipFile.setBufferSize(4096);
    }
    
    
    /**
     * Gravar o stream do arquivo no ZIP. No caso, não haverá compressão dos 
     * arquivos inseridos, pois esta stream contém bytes criptografados, portanto,
     * sem nenhum padrão repetitivo que permita a compactação.
     * @param inputStream stream do arquivo.
     * @param fileNameInZip nome interno do arquivo no ZIP.
     * @throws ZipException 
     */
    protected void addStream(InputStream inputStream, String fileNameInZip) throws ZipException {
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptionMethod(EncryptionMethod.NONE);     
        zipParameters.setCompressionMethod(CompressionMethod.STORE);
        zipParameters.setRootFolderNameInZip("");
        zipParameters.setOverrideExistingFilesInZip(true);
        zipParameters.setFileNameInZip(fileNameInZip);
        zipFile.addStream(inputStream, zipParameters);
    }
    
    
    /**
     * Obter o stream de entrada do arquivo no ZIP.
     * @param fileHeader cabeçalho do arquivo.
     * @return stream de entrada do arquivo no ZIP.
     * @throws IOException 
     */
    protected ZipInputStream getInputStream(FileHeader fileHeader) throws IOException {
        return zipFile.getInputStream(fileHeader);
    }
    
    
    /**
     * Remover o arquivo do ZIP.
     * @param fileHeader cabeçalho do arquivo.
     * @throws ZipException 
     */
    protected void deleteFile(FileHeader fileHeader) throws ZipException {
        zipFile.removeFile(fileHeader);
    }
    
    
    /**
     * Obter os cabeçalhos de todos os arquivos no ZIP.
     * @return lista com os cabeçalhos de todos os arquivos no ZIP.
     * @throws ZipException 
     */
    protected List<FileHeader> getFileHeaders() throws ZipException {
        return zipFile.getFileHeaders();
    }
    
    
    /**
     * Obter o cabeçalho do arquivo no ZIP.
     * @param fileName nome do arquivo.
     * @return cabeçalho do arquivo no ZIP.
     * @throws ZipException 
     */
    protected FileHeader getFileHeader(String fileName) throws ZipException {
        return zipFile.getFileHeader(fileName);
    }
    
    
    /**
     * Gravar o comentário do arquivo ZIP.
     * @param comment
     * @throws ZipException 
     */
    protected void setComment(String comment) throws Exception {
        zipFile.setComment(comment);
    }
    
    
    /**
     * Obter o comentário do arquivo ZIP.
     * @return comentário do arquivo ZIP.
     * @throws ZipException 
     */
    protected String getComment() throws Exception {
        return zipFile.getComment();
    }
    
    
    /**
     * Obter o arquivo ZIP.
     * @return arquivo ZIP.
     */
    public File getFile() {
        return file;
    }
    
    
}
