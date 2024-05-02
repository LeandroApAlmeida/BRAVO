package bravo.gui.dialogs;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

class FileTransferable implements Transferable {
    
    
    private final List<File> files;
    
    private final DataFlavor[] flavors;

    
    public FileTransferable(List<File> files) {
        this.files = Collections.unmodifiableList(files);
        this.flavors = new DataFlavor[] {DataFlavor.javaFileListFlavor};
    }

    
    @Override 
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,
    IOException {
        if (isDataFlavorSupported(flavor)) {
            return this.files;
        } else {
            return null;
        }
    }

    
    @Override 
    public DataFlavor[] getTransferDataFlavors() {
        return this.flavors;
    }

    
    @Override 
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.javaFileListFlavor.equals(flavor);
    }

    
}