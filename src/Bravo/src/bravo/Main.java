package bravo;

import bravo.environment.CacheCleaner;
import javax.swing.UIManager;
import bravo.gui.dialogs.MainForm;
import bravo.environment.Installer;
import dialogs.ErrorDialog;
import java.io.File;

/**
 * Classe principal para a execução do programa.
 * 
 * @since 1.0
 */
public class Main {
    
    
    // Definição de variáveis do ambiente.
    static {
        
        System.setProperty("bravo.file_description", "Bravo Archive (*.bar)");
        System.setProperty("bravo.file_extension", "bar");
        System.setProperty("bravo.version_number", "2.0");
        System.setProperty("bravo.version_author", "Leandro Ap. Almeida");
        System.setProperty("bravo.version_date", "25 de Abril de 2024");
        
        // Aparência da UI é a mesma da plataforma subjacente.
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex){
        }
        
    }
    
    
    /**
     * Rotinas de inicialização do sistema:<br><br>
     * 
     * Verifica os parâmetros externos passados para o programa. Caso não haja 
     * nenhum parâmetro, apenas abre o programa. Há dois parâmetros possíveis. 
     * São eles: <br><br>
     * 
     * <i>/i</i> Instalação do programa na plataforma corrente, com a criação de
     * ícone na área de trabalho e associação do arquivo .bar ao programa. <br><br>
     * 
     * <i>[path]</i> Caminho de um arquivo .bar a ser aberto pelo programa. <br><br>
     * 
     * No caso da instalação, não inicializa sessão.
     * 
     * @param args Parâmetros externos para o programa:<br><br>
     * 
     * <i>/i</i> Instalação do programa na plataforma corrente, com a criação de
     * ícone na área de trabalho e associação do arquivo .bar ao programa. <br><br>
     * 
     * <i>[path]</i> Caminho de um arquivo .bar a ser aberto pelo programa.
     */
    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(() -> {
            
            switch (args.length) {
                
                case 1 -> {
                    
                    switch (args[0]) {
                        
                        case "/i" -> {
                            try {
                                new Installer().install();
                                new CacheCleaner().cleanCurrentSessionCache();
                                System.exit(0);                        
                            } catch (Exception ex) {
                                ErrorDialog.showException(
                                    null,
                                    "ERRO NA INSTALAÇÃO",
                                    ex
                                );
                            }
                        }
                        
                        default -> {
                            MainForm mainForm = new MainForm();
                            mainForm.setVisible(true);
                            File bravoFile = new File(args[0]);
                            if (bravoFile.exists()) {
                                mainForm.openFile(bravoFile);
                            }
                        }
                        
                    }
                    
                }
                
                default -> {
                    new MainForm().setVisible(true);
                }
                
            }
            
        });
        
    }
    
    
}