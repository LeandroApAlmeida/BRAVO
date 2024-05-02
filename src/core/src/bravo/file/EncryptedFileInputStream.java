package bravo.file;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Classe para leitura de um arquivo com notificação de status de processo. O 
 * objetivo desta classe é de fornecer as funcionalidades da classe {@link FileInputStream}
 * com notificação de cada byte lido do arquivo para instâncias que implementam
 * a interface {@link CipherListener}. Como a implementação para leitura e escrita
 * do arquivo ZIP é interna ao pacote Zip4j, a única forma de ter notificação do
 * processo de escrita no ZIP é por meio da personalização de um input stream que 
 * realize esta etapa. Na etapa de gravação do arquivo ao invés de usar o método
 * {@link net.lingala.zip4j.ZipFile#addFile(java.io.File, net.lingala.zip4j.model.ZipParameters)} 
 * deve-se usar o método {@link net.lingala.zip4j.ZipFile#addStream(java.io.InputStream, net.lingala.zip4j.model.ZipParameters)}
 * 
 * @since 1.0
 */
final class EncryptedFileInputStream extends FileInputStream {
    
    
    /**Ouvintes do processo de encriptação de arquivos.*/
    private final CipherListener[] listeners;
    
    
    /**
     * Constructor da classe.
     * @param file arquivo para leitura.
     * @param listeners ouvintes do processo de encriptação.
     */
    public EncryptedFileInputStream(File file, CipherListener... listeners) throws FileNotFoundException {
        super(file);
        this.listeners = listeners;
    }

    
    public EncryptedFileInputStream(FileDescriptor fdObj, CipherListener... listeners) {
        super(fdObj);
        this.listeners = listeners;
    }

    
    public EncryptedFileInputStream(String name, CipherListener... listeners) throws FileNotFoundException {
        super(name);
        this.listeners = listeners;
    }
    
    
    /**
     * Notificar os ouvintes do processo de encriptação.
     * @param length número de bytes processados.
     */
    private void notify(int length) {
        for (CipherListener listener : listeners) {
            listener.update(length);
        }
    }
    
    
    @Override
    public int read(byte[] b) throws IOException {
        int length = super.read(b);
        notify(length);
        return length;
    }

    
    @Override
    public int read() throws IOException {
        notify(1);
        return super.read();
    }

    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int length = super.read(b, off, len);
        if (length != -1) notify(length);
        return length;
    }

    
    @Override
    public byte[] readAllBytes() throws IOException {
        byte[] allBytes = super.readAllBytes();
        notify(allBytes.length);
        return allBytes;
    }

    
    @Override
    public byte[] readNBytes(int len) throws IOException {
        byte[] bytes = super.readNBytes(len);
        notify(bytes.length);
        return bytes;
    }

    
    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        int length = super.readNBytes(b, off, len); 
        if (length != -1) notify(length);
        return length;
    }
    
    
}