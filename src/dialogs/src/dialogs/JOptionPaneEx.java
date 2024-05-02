package dialogs;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public final class JOptionPaneEx extends JOptionPane {

    
    private JOptionPaneEx() {
    }

    
    private static ImageIcon getIcon(int messageType) {
        String icon = "/dialogs/";
        switch (messageType) {
            case ERROR_MESSAGE -> icon = icon + "icon1.png";
            case INFORMATION_MESSAGE -> icon = icon + "icon3.png";
            case WARNING_MESSAGE -> icon = icon + "icon4.png";
            case QUESTION_MESSAGE -> icon = icon + "icon2.png";
            case PLAIN_MESSAGE -> icon = icon + "icon3.png";
        }
        ImageIcon imgIcon = new ImageIcon(JOptionPaneEx.class.getResource(icon));
        return imgIcon;
    }
    
    
    public static void showMessageDialog(Component parent, Object message, 
    String title, int messageType) {
        ImageIcon icon = getIcon(messageType);
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            messageType,
            icon
        );
    }
    
    
    public static void showMessageDialog(Component parent, Object message,
    String title) {
        JOptionPaneEx.showMessageDialog(
            parent,
            message,
            title,
            PLAIN_MESSAGE
        );
    }
    
    
    public static int showConfirmDialog(Component parent, Object message, 
    String title, int optionType, int messageType) {
        ImageIcon icon = getIcon(messageType);
        return JOptionPane.showConfirmDialog(
            parent,
            message,
            title,
            optionType,
            messageType,
            icon
        );
    }
    
    
    public static int showConfirmation(Component parent, Object message, 
    String title) {
        return JOptionPaneEx.showConfirmDialog(
            parent,
            message,
            title,
            YES_NO_OPTION,
            QUESTION_MESSAGE
        );
    }
    
    
}