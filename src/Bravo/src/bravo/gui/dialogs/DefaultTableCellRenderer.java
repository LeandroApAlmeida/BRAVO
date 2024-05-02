package bravo.gui.dialogs;

import bravo.environment.RootFolder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.Date;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

class DefaultTableCellRenderer implements javax.swing.table.TableCellRenderer {
    
    
    public static final int ICON_SIZE = 32;
    private Color gray = new Color(242, 242, 242);
    private File defaultFile;

    
    public DefaultTableCellRenderer() {
        
        defaultFile = new File(
            RootFolder.getThumbnailsFolder().getAbsolutePath() + 
            File.separator + "thumbnail"
        );
        
        if (!defaultFile.exists()) {
            try {
                defaultFile.createNewFile();
            } catch (Exception ex){
            }
        }
        
    }

    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
        
        //Componente de exibição é uma JLabel.
        JLabel label = new JLabel();
        label.setOpaque(true);
        Font font = new Font("tahoma", Font.PLAIN, 12);
        label.setFont(font); 
        label.setBorder(null);
        
        switch (column) {
            
            case 1 -> {
                
                Icon icon;
                File tmpFile;
                
                if ((boolean)table.getValueAt(row, 0)) {                
                    String fileName = (String)value;
                    String extension;
                    int idx = fileName.lastIndexOf(".");
                    if (idx != -1) {
                        extension = fileName.substring(idx, fileName.length());
                    } else {
                        extension = "";
                    }
                    String tmpFileName = RootFolder.getThumbnailsFolder()
                    .getAbsolutePath() + File.separator + "thumbnail" +
                    extension;
                    tmpFile = new File(tmpFileName);
                } else {
                    tmpFile = RootFolder.getThumbnailsFolder();            
                }
                
                try {
                    if (!tmpFile.exists()) {
                        tmpFile.createNewFile();
                    }
                    icon = FileSystemView.getFileSystemView().getSystemIcon(
                        tmpFile,
                        ICON_SIZE,
                        ICON_SIZE
                    );
                } catch (Exception ex) {
                    icon = FileSystemView.getFileSystemView().getSystemIcon(
                        defaultFile,
                        ICON_SIZE,
                        ICON_SIZE
                    );
                }
                
                label.setIcon(icon);
                label.setText((String)value);
                
            }
            
            case 2 -> {
                if ((boolean)table.getValueAt(row, 0)) {
                    label.setText(Formatter.formatSize((long)value) + "  ");
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    label.setText("");
                }
            }
            
            case 3 -> {
                if ((boolean)table.getValueAt(row, 0)) {
                    label.setText(" " + Formatter.formatDate((Date)value));
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    label.setText("");
                }
            }
            
            case 4 -> {
                if ((boolean)table.getValueAt(row, 0)) {
                    label.setText(" " + Formatter.formatDate((Date)value) + " ");
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    label.setText("");
                }
            }
            
        }
        //Coluna 1: Ícone e nome do arquivo.
        //Coluna 2: Tamanho do arquivo (em byte, kilobyte, megabyte).
        //Coluna 3: Data da criação do arquivo formatada como dd/mm/aaaa hh:mm
        //Coluna 4: Data da alteração do arquivo formatada como dd/mm/aaaa hh:mm
        
        if (isSelected) {
            label.setBackground(gray);
            label.setForeground(table.getForeground());
        } else {
            label.setForeground(table.getForeground());
            label.setBackground(table.getBackground());
        }
        
        return label;
        
    }
 
    
}