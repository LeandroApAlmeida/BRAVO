package bravo.gui.dialogs;

import bravo.environment.RootFolder;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.filechooser.FileSystemView;

class DefaultListCellRenderer implements ListCellRenderer {

    
    @Override
    public Component getListCellRendererComponent(JList arg0, Object arg1, 
    int arg2, boolean arg3, boolean arg4) {
        
        JLabel label = new JLabel((String) arg1);
        label.setOpaque(true);
        label.setBorder(null);
        
        if (arg0.getModel().getSize() > 0) {
            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(
            RootFolder.getThumbnailsFolder());
            label.setIcon(icon);
        }
        
        if (arg3) label.setBackground(Color.GRAY);
        if (arg4) label.setBackground(Color.GRAY);
        
        return label;
        
    }
    
    
}