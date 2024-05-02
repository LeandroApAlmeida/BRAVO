package bravo.file;

/**
 * Classe que representa um arquivo encriptado inserido no ZIP. Sua finalidade
 * é fornecer dados sobre o arquivo em questão como seu nome, data de criação,
 * data de alteração, etc para acesso fora do pacote <i>bravo.file</i>.
 * 
 * @since 1.0
 */
public final class FileEntry {
    
    
    /**Nome interno do arquivo como lido pelo ZIP.*/
    private final String internalName;
    
    /**Nome real do arquivo, incluindo seu path completo.*/
    private final String name;
    
    /**Data da última modificação do arquivo em milisegundos.*/
    private final long lastModifiedTime;
    
    /**Data da criação do arquivo em milisegundos.*/
    private final long createdTime;
    
    /**Tamanho do arquivo em bytes.*/
    private final long originalSize;
    
    /**Se true, entrada é referente a uma pasta.*/
    private final boolean isFolder;
    
    /**Se true, entrada é referente a um arquivo.*/
    private final boolean isFile;

    
    /**
     * Constructor da classe com parâmetros para identificação do arquivo. 
     * @param internalFileName nome interno do arquivo como lido pelo ZIP.
     * @param fileName nome real do arquivo, incluindo seu path completo.
     * @param lastModifiedTime data da última modificação do arquivo em milisegundos.
     * @param createdTime data da criação do arquivo em milisegundos.
     * @param originalSize tamanho do arquivo em bytes.
     * @param isFolder se true, entrada é referente a uma pasta.
     * @param isFile se true, entrada é referente a um arquivo.
     */
    public FileEntry(String internalFileName, String fileName, long lastModifiedTime, 
    long createdTime, long originalSize, boolean isFolder, boolean isFile) {
        this.internalName = internalFileName;
        this.name = fileName;
        this.lastModifiedTime = lastModifiedTime;
        this.createdTime = createdTime;
        this.originalSize = originalSize;
        this.isFolder = isFolder;
        this.isFile = isFile;
    }
    
    
    /**
     * Constructor da classe direcionado para uso interno ao pacote <i>bravo.file</i>.
     * @param fileMetadata cabeçalho de arquivo encriptado.
     */
    FileEntry(EncryptedFileMetadata fileMetadata) {
        this(
            fileMetadata.getInternalFileName(), 
            fileMetadata.getFileName(),
            fileMetadata.getLastModifiedTime(), 
            fileMetadata.getCreatedTime(),
            fileMetadata.getOriginalSize(),
            false,
            true
        );
    }

    
    /**
     * Obter o nome interno do arquivo como lido pelo ZIP.
     * @return nome interno do arquivo.
     */
    public String getInternalName() {
        return internalName;
    }

    
    /**
     * Obter o nome real do arquivo, incluindo seu path completo.
     * @return nome real do arquivo.
     */
    public String getName() {
        return name;
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
     * Obter o status de pasta da entrada.
     * @return se true, entrada é referente a uma pasta.
     */
    public boolean isIsFolder() {
        return isFolder;
    }

    
    /**
     * Obter o status de arquivo da entrada.
     * @return se true, entrada é referente a um arquivo.
     */
    public boolean isIsFile() {
        return isFile;
    }

    
    @Override
    public String toString() {
        return name;
    }

    
}