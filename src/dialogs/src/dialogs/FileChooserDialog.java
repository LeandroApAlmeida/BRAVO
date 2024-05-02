package dialogs;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Diálogo para abrir/salvar arquivos.
 * 
 * @since 1.0
 */
public final class FileChooserDialog extends JFileChooser {

    
    /**Flag para controle de execução do evento ActionPerformed somente na classe
    especializada.*/
    private boolean ignoreActionPerformed;
    
    
    /**
     * Criar uma instância de <b>FileChooser</b>. Espera o título do diálogo
     * e os filtros de arquivos a serem visualizados.
     * @param dialogTitle título para exibir na barra de títulos do diálogo.
     * @param filter filtro padrão(obrigatório).
     * @param filters demais filtros para o diálogo, se necessário.
     */
    public FileChooserDialog(String dialogTitle, FileNameExtensionFilter filter, 
    FileNameExtensionFilter... filters) {
        
        setMultiSelectionEnabled(false);
        setAcceptAllFileFilterUsed(false);
        setDialogTitle(dialogTitle);
        setFileFilter(filter);
        
        for (FileNameExtensionFilter fileFilter : filters) {
            setFileFilter(fileFilter);
        }
        
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defaultActionPerformed(e);
            }
        });
        
    }
    
    
    /**
     * Acionado ao clicar no botao "Salvar/abrir" no diálogo. Neste caso, não 
     * fecha o diálogo como na classe base, mas aciona o tratador de evento
     * apenas.
     */
    @Override
    public void approveSelection() {
        //Não vai ignorar o tratador de eventos.
        ignoreActionPerformed = false;
        //Aciona o tratador de eventos.
        fireActionPerformed(APPROVE_SELECTION);        
    }

    
    /**
     * Sobrescrito para aceitar somente os filtros que são instância de
     * FileNameExtensionFilter da qual esta classe depende.
     * @param filter instância de {@link FileNameExtensionFilter}.
     */
    @Override
    public void setFileFilter(FileFilter filter) {
        if (filter instanceof FileNameExtensionFilter) {
            super.setFileFilter(filter);
        }   
    }

    
    /**
     * Evento disparado ao clicar em algum dos botões do diálogo (Abrir/Salvar/Cancelar).
     * @param evt evento disparado ao clicar no botão.
     */
    private void defaultActionPerformed(java.awt.event.ActionEvent evt) { 
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        if (!ignoreActionPerformed) {
            
            //Vai ignorar o tratamento de eventos, que agora só poderá
            //ser acionado na classe base JFileChooser.
            ignoreActionPerformed = true;  
            
            if (evt.getActionCommand().equals(APPROVE_SELECTION)) {
                
                boolean aproveSelection = true;
                
                if (((JFileChooser) evt.getSource()).getDialogType() == SAVE_DIALOG) {
                    
                    //Abriu-se o diálogo para salvar um arquivo.
                    File selectedFile = getSelectedFile();
                    
                    //Verifica se o usuário digitou a extensão do arquivo.
                    FileNameExtensionFilter filter = (FileNameExtensionFilter) 
                    getFileFilter();
                    
                    String[] exts = filter.getExtensions();
                    boolean endsWith = false;
                    
                    for (String ext : exts) {
                        if (selectedFile.getAbsolutePath().endsWith("." + ext)) {
                            endsWith = true;
                        }
                    }
                    
                    if (!endsWith) {
                        String fileName = selectedFile.getAbsolutePath() +
                        "." + exts[0];
                        //Completa com a extensão do arquivo.
                        selectedFile = new File(fileName);
                        setSelectedFile(selectedFile);
                    }
                    
                    //Verifica se há um arquivo com o mesmo nome.
                    if (selectedFile.exists()) {
                        //Pede a confirmação do usuário para sobrescrevê-lo.
                        int opt = javax.swing.JOptionPane.showConfirmDialog(
                            this.getParent(),
                            selectedFile.getAbsolutePath() +
                            " já existe.\nSobrescrever o arquivo existente?",
                            "Atenção!",
                            javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.QUESTION_MESSAGE
                        );
                        if (opt == javax.swing.JOptionPane.NO_OPTION) {
                            aproveSelection = false;
                        }
                    }
                    
                } else {
                    
                   File selectedFile = getSelectedFile();
                   if (!selectedFile.exists()) {
                       aproveSelection = false;
                   }
                   
                }
                
                if (aproveSelection) {
                    super.approveSelection(); 
                }
                
            }
            
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }

    
}