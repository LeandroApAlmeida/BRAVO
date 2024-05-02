package bravo.file;

import java.io.Serializable;
import net.lingala.zip4j.model.FileHeader;


/**
 * Classe que representa um cabeçalho de arquivo encriptado inserido no ZIP. Este
 * cabeçalho, além de guardar os dados de atributos do arquivo, guarda também os 
 * dados sobre a encriptação do mesmo como bytes de SALT, bytes de vetor de 
 * inicialização, entre outros parâmetros.
 * 
 * @since 1.0
 */
final class EncryptedFileMetadata implements Serializable {
    
    
    //@java.io.Serial
    private static final long serialVersionUID = 1L;
    
    /**Cabeçalho do arquivo ZIP conforme especificado pelo pacote Zip4j.*/
    private transient FileHeader zipHeader;
    
    /**Nome interno do arquivo, conforme lido pelo ZIP.*/
    private String internalFileName;
    
    /**Nome real do arquivo, incluindo seu path completo.*/
    private String fileName;
    
    /**Bytes de vetor de inicialização para uso na encriptação/decriptação do arquivo.*/
    private byte[] ivBytes;
    
    /**Bytes do SALT para uso na encriptação/decriptação do arquivo.*/
    private byte[] saltBytes;
    
    /**Data da última modificação do arquivo em milisegundos.*/
    private long lastModifiedTime;
    
    /**Data da criação do arquivo em milisegundos.*/
    private long createdTime;
    
    /**Tamanho do arquivo em bytes.*/
    private long originalSize;

    
    /**
     * Obter o cabeçalho do arquivo ZIP conforme especificado pelo pacote Zip4j.
     * @return cabeçalho do arquivo ZIP conforme especificado pelo pacote Zip4j.
     */
    public FileHeader getZipHeader() {
        return zipHeader;
    }
    
    
    /**
     * Obter o nome interno do arquivo, conforme lido pelo ZIP.
     * @return nome interno do arquivo.
     */
    public String getInternalFileName() {
        return internalFileName;
    }

    
    /**
     * Obter o nome real do arquivo, incluindo seu path completo.
     * @return nome real do arquivo.
     */
    public String getFileName() {
        return fileName;
    }

    
    /**
     * Obter a data da última modificação do arquivo em milisegundos.
     * @return data da última modificação do arquivo.
     */
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    
    /**
     * Obter a data da criação do arquivo em milisegundos.
     * @return data da criação do arquivo.
     */
    public long getCreatedTime() {
        return createdTime;
    }

    
    /**
     * Obter o tamanho do arquivo em bytes.
     * @return tamanho do arquivo.
     */
    public long getOriginalSize() {
        return originalSize;
    }

    
    /**
     * Obter os bytes de vetor de inicialização para uso na encriptação/decriptação
     * do arquivo. 
     * @return bytes de vetor de inicialização.
     */
    public byte[] getIVBytes() {
        return ivBytes;
    }

    
    /**
     * Obter os bytes do SALT para uso na encriptação/decriptação do arquivo.
     * @return 
     */
    public byte[] getSaltBytes() {
        return saltBytes;
    }

    
    /**
     * Difinir o cabeçalho do arquivo ZIP conforme especificado pelo pacote Zip4j.
     * @param fileHeader cabeçalho do arquivo ZIP conforme especificado pelo pacote Zip4j.
     */
    public void setZipHeader(FileHeader fileHeader) {
        this.zipHeader = fileHeader;
    }

    
    /**
     * Definir o nome interno do arquivo, conforme lido pelo ZIP.
     * @param internalFileName nome interno do arquivo.
     */
    public void setInternalFileName(String internalFileName) {
        this.internalFileName = internalFileName;
    }

    
    /**
     * Definir o nome real do arquivo, incluindo seu path completo.
     * @param fileName nome real do arquivo.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    
    /**
     * Definir a data da última modificação do arquivo em milisegundos.
     * @param lastModifiedTime data da última modificação do arquivo.
     */
    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    
    /**
     * Definir a data da criação do arquivo em milisegundos.
     * @param createdTime data da criação do arquivo.
     */
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    
    /**
     * Definir o tamanho do arquivo em bytes.
     * @param originalSize tamanho do arquivo.
     */
    public void setOriginalSize(long originalSize) {
        this.originalSize = originalSize;
    }

    
    /**
     * Definir os bytes de vetor de inicialização para uso na encriptação/decriptação
     * do arquivo.
     * @param ivBytes bytes de vetor de inicialização.
     */
    public void setIVBytes(byte[] ivBytes) {
        this.ivBytes = ivBytes;
    }
    
    
    /**
     * Definir os bytes do SALT para uso na encriptação/decriptação do arquivo.
     * @param saltBytes 
     */ 
    public void setSaltBytes(byte[] saltBytes) {
        this.saltBytes = saltBytes;
    }

    
    /**
     * Comparar instâncias. O critério de comparação é o nome real do arquivo.
     * @param obj outra instância a ser comparada.
     * @return true, se ambas as instâncias tem o mesmo nome de arquivo, false,
     * se os nome de arquivo são diferentes.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof EncryptedFileMetadata)) return false;
        EncryptedFileMetadata ref = (EncryptedFileMetadata) obj;
        return this.fileName.equals(ref.fileName);
    }

    
    @Override
    public String toString() {
        return fileName;
    }

    
}