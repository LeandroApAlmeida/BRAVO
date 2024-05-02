package bravo.gui.dialogs;

import bravo.environment.RootFolder;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;

class TreeCellRenderer extends DefaultTreeCellRenderer {

    
    private final Icon icon = FileSystemView.getFileSystemView().getSystemIcon(
        RootFolder.getThumbnailsFolder(),
        24,
        24
    );

    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, 
    boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel label = new JLabel(value.toString());
        label.setOpaque(true);
        if (sel) {
            label.setBackground(this.getBackgroundSelectionColor());
        } else {
            label.setBackground(tree.getBackground());
        }
        label.setIcon(icon);
        return label;
    }

    
}