package bravo.file;

import java.io.File;

final class FileEncryptionInfo {

    
    private File inputFile;
    
    private File outputFile;
    
    private EncryptedFileMetadata fileMetadata;

    
    public FileEncryptionInfo(File inputFile, File outputFile,
    EncryptedFileMetadata fileMetadata) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.fileMetadata = fileMetadata;
    }
    
    
    public FileEncryptionInfo(EncryptedFileMetadata fileMetadata) {
        this(null, null, fileMetadata);
    }

    
    public File getInputFile() {
        return inputFile;
    }
    
    
    public File getOutputFile() {
        return outputFile;
    }

    
    public EncryptedFileMetadata getFileMetadata() {
        return fileMetadata;
    }
    
    
}