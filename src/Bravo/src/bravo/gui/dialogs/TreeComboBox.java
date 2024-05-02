package bravo.gui.dialogs;

import bravo.environment.RootFolder;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
    
public class TreeComboBox extends JComboBox<DefaultMutableTreeNode> {
    
    
    private final Icon icon = FileSystemView.getFileSystemView().getSystemIcon(
        RootFolder.getThumbnailsFolder()
    );
    
    
    public TreeComboBox(TreeModel model) {
        
        super();
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        
        DefaultComboBoxModel<DefaultMutableTreeNode> m = new DefaultComboBoxModel<>();
        Collections.list((Enumeration<?>) root.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(n -> !n.isRoot())
        .forEach(m::addElement);
        
        setModel(m);
        
    }

    
    @Override
    public void updateUI() {
        
        super.updateUI();
        
        JTree jtree = new JTree();
        
        TreeCellRenderer renderer = jtree.getCellRenderer();
        
        ListCellRenderer<? super DefaultMutableTreeNode> r = getRenderer();
        
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            
            Component c = r.getListCellRendererComponent(
                list,
                value,
                index,
                isSelected,
                cellHasFocus
            );
            
            if (value == null) {
                return c;
            }
            
            if (index < 0) {
                String txt = Arrays.stream(value.getPath())
                .filter(DefaultMutableTreeNode.class::isInstance)
                .map(DefaultMutableTreeNode.class::cast)
                .filter(n -> !n.isRoot())
                .map(Objects::toString)
                .collect(Collectors.joining(" / "));
                ((JLabel) c).setText(txt);
                return c;
            } else {
                boolean leaf = value.isLeaf();
                JLabel l = (JLabel) renderer.getTreeCellRendererComponent(
                    jtree,
                    value,
                    isSelected,
                    true,
                    leaf,
                    index,
                    false
                );
                int indent = Math.max(0, value.getLevel() - 1) * icon.getIconWidth();
                l.setBorder(BorderFactory.createEmptyBorder(1, indent + 1, 1, 1));
                l.setIcon(icon);
                return l;
            }
            
        });
        
    }
    
    
}