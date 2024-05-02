package bravo.filter;

import java.io.File;

public final class FileFilter implements java.io.FileFilter {
    
    
    @Override
    public boolean accept(File pathname) {
        return pathname.isFile();
    }

    
}