package bravo.gui.dialogs;

import java.util.Date;

final class Formatter {
   
    
    public static String formatSize(long value) {
        double d;
        String m;
        long bytes = (long) value;                    
        if (bytes < 1024) {
            d = bytes;
            m = "B";
        } else if (bytes >= 1024 && bytes < 1048576) {
            d = bytes / 1024f;
            m = "KB";
        } else if (bytes >= 1048576 && bytes < 1073741824) {
            d = bytes / 1048576f;
            m = "MB";
        } else {
            d = bytes / 1073741824f;
            m = "GB";
        }
        return String.format("%.2f", d) + " " + m;
    }
    
    
    public static String formatDate(Date value) {
        return String.format("%1$td/%1$tm/%1$tY %1$tH:%1$tM", (Date)value);
    }
    
    
}