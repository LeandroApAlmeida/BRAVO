package bravo.gui.dialogs;

import dialogs.FileChooserDialog;
import java.awt.Color;
import java.awt.Cursor;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import bravo.file.BravoFile;
import bravo.file.FileEntry;
import bravo.environment.Config;
import bravo.environment.RootFolder;
import bravo.environment.CacheCleaner;
import bravo.file.Argon2Params;
import dialogs.ErrorDialog;
import dialogs.JOptionPaneEx;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class MainForm extends javax.swing.JFrame implements DropTargetListener {

    
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    private final Desktop desktop = Desktop.getDesktop();
    
    private final FileNameExtensionFilter filter;
    
    private final List<FileEntry> encryptedFiles;
    
    private final Stack<String> foldersStack;
    
    private BravoFile bravoFile = null;
    
    private boolean ctrlLocked = false;
    
    private final DropTarget dropTarget;
    
    private static File currentDirectory1 = null;
    
    private static File currentDirectory2 = null;
    
    private static File currentDirectory3 = null;

    
    public MainForm() {
        
        initComponents();
        
        encryptedFiles = new ArrayList<>();
        foldersStack = new Stack<>();
        dropTarget = new DropTarget(this, this);
        
        URL url = getClass().getResource("/bravo/gui/images/Bravo.png");
        Image image = new ImageIcon(url).getImage();
        setIconImage(image);
        
        String fileDescription = System.getProperty("bravo.file_description");
        String fileExtension = System.getProperty("bravo.file_extension");
        filter = new FileNameExtensionFilter(fileDescription, fileExtension);
        
        setExtendedState(MAXIMIZED_BOTH);
        
        jtpComment.setEnabled(false);
        jtpComment.setBackground(Color.WHITE);
        jlMessage1.setText("");
        jlMessage2.setVisible(false);
        
        closeFile();
        
    }

    
    private void createNewFile() {
        
        Frame _this = this;
        
        new Thread() {
            
            @Override
            public void run() {
                
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                
                FileChooserDialog fileDialog = new FileChooserDialog(
                    "CRIAR NOVO ARQUIVO",
                    filter
                );
                
                int opc = fileDialog.showSaveDialog(_this);
                
                if (opc == FileChooserDialog.APPROVE_OPTION) {          
                    
                    File file = fileDialog.getSelectedFile();
                    currentDirectory1 = file.getParentFile();
                    
                    NewFileDialog newFileDialog = new NewFileDialog(
                        _this,
                        "CRIAR NOVO ARQUIVO   ",
                        file,
                        true
                    );
                    
                    newFileDialog.setVisible(true);
                    
                    if (!newFileDialog.isCanceled()) {
                        
                        RandomBytesDialog randomBytesDialog = new RandomBytesDialog(_this);
                        
                        randomBytesDialog.setVisible(true);
                        
                        byte[] randomBytes = randomBytesDialog.getGeneratedRandomBytes();
                    
                        char[] password = newFileDialog.getPassword();

                        Argon2Params params = newFileDialog.getParams();

                        createFile(file, password, params, randomBytes);

                        params = null;
                        newFileDialog.dispose();
                        newFileDialog = null;
                        randomBytesDialog = null;
                        System.gc();
                    
                    }
                    
                } 
                
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                
            }
            
        }.start();
        
    }
    
    
    private void createFile(File file, char[] password, Argon2Params params,
    byte[] randomBytes) {
        
        jlMessage2.setVisible(true);
        
        try {
            
            bravoFile = null;
            System.gc();
            
            bravoFile = new BravoFile(
                file,
                password,
                params,
                randomBytes
            );
            
            foldersStack.push(bravoFile.getRootFolder());
            jtpComment.setText(bravoFile.getComment()); 
            dropTarget.setActive(true);
            jcbFolders.setEnabled(true);
            
            updateFoldersStack();
            updateTable();
            updateMenu();
            updateToolBar();
            
            jlMessage2.setVisible(false);
            
        } catch (Exception ex) {
            
            jlMessage2.setVisible(false);
            
            ErrorDialog.showException(
                (Frame) this,
                "Erro ao criar o arquivo " + file.getAbsolutePath(),
                ex
            );
            
        }
        
    }
    
    
    private void openFile() {
        
        Frame _this = this;
        
        new Thread() {
            
            @Override
            public void run() {
                
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                
                FileChooserDialog fileDialog = new FileChooserDialog(
                    "ABRIR PASTA",
                    filter
                );
                
                fileDialog.setCurrentDirectory(currentDirectory1);
                
                int opc = fileDialog.showOpenDialog(_this);
                
                if (opc == FileChooserDialog.APPROVE_OPTION) {
                    
                    File file = fileDialog.getSelectedFile();
                    currentDirectory1 = file.getParentFile();
                    
                    PasswordDialog passwordDialog = new PasswordDialog(
                        _this,
                        "ABRIR PASTA",
                        file,
                        false
                    );
                    
                    passwordDialog.setVisible(true);
                    
                    if (!passwordDialog.isCanceled()) {
                    
                        char[] password = passwordDialog.getPassword();

                        openFile(file, password);

                        passwordDialog.dispose();
                        passwordDialog = null;
                        System.gc();
                    
                    }
                    
                }
                
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                
            }
            
        }.start();
        
    }
    
    
    public void openFile(File file) {
        
        Frame _this = this;
        new Thread() {
            
            @Override
            public void run() {
                
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                
                PasswordDialog passwordDialog = new PasswordDialog(
                    _this,
                    "ABRIR ARQUIVO",
                    file,
                    false
                );
                
                passwordDialog.setVisible(true);
                
                if (!passwordDialog.isCanceled()) {

                    char[] password = passwordDialog.getPassword();

                    openFile(file, password);

                    passwordDialog.dispose();
                    passwordDialog = null;
                    System.gc();
                
                }
                
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                
            }
            
        }.start();
        
    }
    
    
    private void openFile(File file, char[] password) {
        
        jlMessage2.setVisible(true);
        
        try {
            
            bravoFile = null;
            System.gc();
            
            bravoFile = new BravoFile(file, password);
            foldersStack.push(bravoFile.getRootFolder());
            jtpComment.setText(bravoFile.getComment()); 
            dropTarget.setActive(true);
            jcbFolders.setEnabled(true);
            
            updateFoldersStack();
            updateTable();
            updateMenu();
            updateToolBar();
            
            jlMessage2.setVisible(false);
            
        } catch (Exception ex) {
            
            jlMessage2.setVisible(false);
            
            ErrorDialog.showException(
                (Frame) this,
                "Erro ao abrir o arquivo " + file.getAbsolutePath(),
                ex
            );
            
        }
        
    }
    
    
    private void closeFile() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        dropTarget.setActive(false);
        jlMessage2.setVisible(true);
        
        if (isFileOpened()) {
            try {
                bravoFile.close();
            } catch (Exception ex) {
                jlMessage2.setVisible(false);
                ErrorDialog.showException(
                    (Frame) this,
                    "Erro ao liberar recursos alocados.",
                    ex
                );
            }
        }
        
        bravoFile = null;
        System.gc();
        
        foldersStack.clear();
        encryptedFiles.clear();
        
        updateFoldersStack();
        updateMenu();
        updateToolBar();
        updateTable();
        
        jtpComment.setText("");        
        jlMessage1.setText("");
        jcbFolders.setEnabled(false);
        jlMessage2.setVisible(false);
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }
    
    
    private void changeFilePassword() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        PasswordDialog passwordDialog = new PasswordDialog(
            this,
            "Trocar a Senha",
            bravoFile.getFile(),
            true
        );
        
        passwordDialog.setVisible(true);
        
        if (!passwordDialog.isCanceled()) {
        
            char[] password = passwordDialog.getPassword();

            passwordDialog = null;
            System.gc();

            if (password != null) {

                Component _this = this;

                Runnable runnable = () -> {
                    try {
                        //bravoFile.changePassword(password);
                    } catch (Exception ex) {
                        ErrorDialog.showException(
                            (Frame) _this,
                            "Erro ao trocar a senha do arquivo.",
                            ex
                        );
                    }
                };

                ProgressDialog1 progressDialog = new ProgressDialog1(
                    this,
                    "TROCANDO A SENHA...",
                    runnable,
                    false
                );

                bravoFile.addListener(progressDialog);

                progressDialog.setVisible(true);

                bravoFile.removeListener(progressDialog);

                updateFoldersStack();
                updateTable();
                updateToolBar();

            }
        
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }
    
    
    private void setRootFolder(String rootFolder) {
        try {
            bravoFile.setRootFolder(rootFolder);
            foldersStack.push(rootFolder);
            updateFoldersStack();
            updateTable();
            updateToolBar();
        } catch (Exception ex) {
            ErrorDialog.showException(
                (Frame) this,
                "Erro ao selecionar a pasta.",
                ex
            );
        }
    }
    
    
    private void setRootFoolder() {
        int selectedIndex = jcbFolders.getSelectedIndex();
        int count = jcbFolders.getItemCount() - (selectedIndex + 1);
        if (count > 0) {
            try {
                for (int i = 1; i <= count; i++) {
                    foldersStack.pop();
                }
                bravoFile.setRootFolder(foldersStack.peek());
                updateFoldersStack();
                updateTable();
                updateToolBar();
            } catch (Exception ex) {
                ErrorDialog.showException(
                    (Frame) this,
                    "ERRO",
                    ex
                );
            }
        }
    }
 
    
    private void addFilesAndFolders() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        FileChooserDialog fileChooserDialog = new FileChooserDialog(
            "Inserir Arquivos",
            null
        );
        
        fileChooserDialog.setFileSelectionMode(FileChooserDialog.FILES_AND_DIRECTORIES);
        fileChooserDialog.setAcceptAllFileFilterUsed(true);
        fileChooserDialog.setMultiSelectionEnabled(true);
        fileChooserDialog.setCurrentDirectory(currentDirectory2);
        
        int opc = fileChooserDialog.showOpenDialog(this);
        
        if (opc == FileChooserDialog.APPROVE_OPTION) {
            File[] files = fileChooserDialog.getSelectedFiles();
            currentDirectory2 = files[0].getParentFile();
            List<File> selectedFiles = new ArrayList<>();
            Collections.addAll(selectedFiles, files);
            addFilesAndFolders(selectedFiles);
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }
    
    
    private void addFilesAndFolders(List<File> filesAndFolders) {
        
        try {
            
            List<File> existingFiles = bravoFile.testAddFilesAndFolders(filesAndFolders);
            boolean add = true;
            
            if (!existingFiles.isEmpty()) {
                OverrideFilesDialog overrideDialog = new OverrideFilesDialog(
                    this, 
                    "ATENÇÃO!",
                    existingFiles
                );
                overrideDialog.setVisible(true);
                add = overrideDialog.override();
            }
            
            if (add) {
                
                DeleteFilesDialog deleteFilesDialog = new DeleteFilesDialog(this, "Atenção!");
                deleteFilesDialog.setVisible(true);
                
                if (!deleteFilesDialog.isCancel()) {
                    
                    final boolean destroyFiles = deleteFilesDialog.isDelete();
                    Component _this = this;
                    
                    Runnable runnable = () -> {
                        try {
                            bravoFile.addFilesAndFolders(filesAndFolders, destroyFiles);
                        } catch (Exception ex) {
                            ErrorDialog.showException(
                                (Frame) _this,
                                "Erro ao inserir o(s) arquivo(s).",
                                ex
                            );
                        }
                    };
                    
                    ProgressDialog1 progressDialog1 = new ProgressDialog1(
                        this,
                        "INSERINDO ARQUIVOS...",
                        runnable,
                        true
                    );
                    
                    bravoFile.addListener(progressDialog1);
                    
                    progressDialog1.setVisible(true);
                    
                    bravoFile.removeListener(progressDialog1);
                    
                    updateFoldersStack();
                    updateTable();
                    updateToolBar();
                    
                }
                
            }
            
        } catch (Exception ex) {
            
            ErrorDialog.showException(
                this,
                "Erro ao inserir o(s) arquivo(s).",
                ex
            );
            
        }
        
    }
    
    
    private void removeFilesAndFolders() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        if (hasFilesInFolder()) {
            
            int opt = JOptionPaneEx.showConfirmDialog(
                this,
                "Excluir os arquivos selecionados?",
                "Atenção!",
                JOptionPaneEx.YES_NO_OPTION,
                JOptionPaneEx.QUESTION_MESSAGE
            );
            
            if (opt == JOptionPaneEx.YES_OPTION) {
                
                List<String> filesList = getSelectedFiles();
                
                if (!filesList.isEmpty()) {
                    
                    Component _this = this;
                    
                    Runnable runnable = () -> {
                        try {
                            bravoFile.deleteFilesAndFolders(filesList);
                        } catch (Exception ex) {
                            ErrorDialog.showException(
                                    (Frame) _this,
                                    "Erro ao excluir o(s) arquivo(s) selecionado(s).",
                                    ex
                            );
                        }
                    };
                    
                    ProgressDialog2 progressDialog = new ProgressDialog2(
                        this,
                        "EXCLUINDO ARQUIVOS...",
                        runnable,
                        true
                    );
                    
                    bravoFile.addListener(progressDialog);
                    
                    progressDialog.setVisible(true);
                    
                    bravoFile.removeListener(progressDialog);
                    
                    updateFoldersStack();
                    updateTable();
                    updateToolBar();
                    
                }
                
            }
            
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }
    
    
    private void extractFilesAndFolders() {
        
        if (hasFilesInFolder()) {
            
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            
            if (hasFilesInFolder()) {
                
                FileChooserDialog fileChooserDialog = new FileChooserDialog(
                    "SELECIONAR DESTINO",
                    null
                );
                
                fileChooserDialog.setFileSelectionMode(FileChooserDialog.DIRECTORIES_ONLY);
                fileChooserDialog.setAcceptAllFileFilterUsed(false);
                fileChooserDialog.setMultiSelectionEnabled(false);
                
                if (currentDirectory3 == null) {
                    currentDirectory3 = bravoFile.getFile().getParentFile();
                }
                
                fileChooserDialog.setCurrentDirectory(currentDirectory3);
                
                int opc = fileChooserDialog.showOpenDialog(this);
                
                if (opc == FileChooserDialog.APPROVE_OPTION) {
                    
                    File directory = fileChooserDialog.getSelectedFile();
                    
                    currentDirectory3 = directory;
                    
                    List<String> filesList = getSelectedFiles();
                    
                    if (!filesList.isEmpty()) {
                        
                        try {
                            
                            String destination = directory.getAbsolutePath();
                            
                            List<File> existingFiles = bravoFile.testExtractFilesAndFolders(
                                filesList,
                                destination
                            );
                            
                            boolean extract = true;
                            
                            if (!existingFiles.isEmpty()) {
                                OverrideFilesDialog overrideDialog = new OverrideFilesDialog(this,
                                "SOBRESCREVER ARQUIVOS", existingFiles);
                                overrideDialog.setVisible(true);
                                extract = overrideDialog.override();
                            }
                            
                            if (extract) {
                                
                                Component _this = this;
                                
                                Runnable runnable = () -> {
                                    try {
                                        bravoFile.extractFilesAndFolders(
                                            filesList, 
                                            destination
                                        );
                                    } catch (Exception ex) {
                                        ErrorDialog.showException(
                                            (Frame) _this,
                                            "Erro ao extrair o(s) arquivo(s) selecionado(s).",
                                            ex
                                        );
                                    }
                                };
                                
                                ProgressDialog1 progressDialog = new ProgressDialog1(
                                    this,
                                    "EXTRAINDO ARQUIVOS...",
                                    runnable,
                                    true
                                );
                                
                                bravoFile.addListener(progressDialog);
                                
                                progressDialog.setVisible(true);
                                
                                bravoFile.removeListener(progressDialog);
                                
                                desktop.open(directory);
                                
                            }
                            
                        } catch (Exception ex) {
                            
                            ErrorDialog.showException(
                                (Frame) this,
                                "Erro ao extrair o(s) arquivo(s) selecionado(s).",
                                ex
                            );
                            
                        }
                        
                    }
                    
                }
                
            }
            
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            
        }
        
    }
    
    
    private void extractAllFilesAndFolders() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        FileChooserDialog fileChooserDialog = new FileChooserDialog(
            "SELECIONAR DESTINO",
            null
        );
        
        fileChooserDialog.setFileSelectionMode(FileChooserDialog.DIRECTORIES_ONLY);
        fileChooserDialog.setAcceptAllFileFilterUsed(false);
        fileChooserDialog.setMultiSelectionEnabled(false);
        
        if (currentDirectory3 == null) {
            currentDirectory3 = bravoFile.getFile().getParentFile();
        }
        
        fileChooserDialog.setCurrentDirectory(currentDirectory3);
        
        int opc = fileChooserDialog.showOpenDialog(this);
        
        if (opc == FileChooserDialog.APPROVE_OPTION) {
            
            final File directory = fileChooserDialog.getSelectedFile();
            currentDirectory3 = directory;
            
            List<String> filesList = new ArrayList<>();
            List<FileEntry> files = bravoFile.getAllFiles();
            
            for (FileEntry file : files) {
                filesList.add(file.getName());
            }
            
            try {
                
                boolean override = true;
                
                List<File> existingFiles = bravoFile.testExtractFilesAndFolders(
                    filesList,
                    directory.getAbsolutePath()
                );
                
                if (!existingFiles.isEmpty()) {
                    OverrideFilesDialog overrideDialog = new OverrideFilesDialog(
                        this,
                        "SOBRESCREVER ARQUIVOS",
                        existingFiles
                    );
                    overrideDialog.setVisible(true);
                    override = overrideDialog.override();
                }
                
                if (override) {
                    
                    Component _this = this;
                    
                    Runnable runnable = () -> {
                        try {
                            bravoFile.extractAllFiles(directory.getAbsolutePath());
                        } catch (Exception ex) {
                            ErrorDialog.showException(
                                (Frame) _this,
                                "Erro ao extrair os arquivos.",
                                ex
                            );
                        }            
                    };
                    
                    ProgressDialog1 progressDialog = new ProgressDialog1(
                        this,
                        "EXTRAINDO ARQUIVOS...",
                        runnable,
                        true
                    );
                    
                    bravoFile.addListener(progressDialog);
                    
                    progressDialog.setVisible(true);
                    
                    bravoFile.removeListener(progressDialog);
                    
                    desktop.open(directory);
                    
                }
                
            } catch (Exception ex) {
                
                ErrorDialog.showException(
                    (Frame) this,
                    "Erro ao extrair os arquivos.",
                    ex
                );
                
            }
            
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }
    
    
    private void extractAndOpenFiles() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        try {
            
            List<String> filesList = new ArrayList<>();
            
            for (int row : jtbFiles.getSelectedRows()) {
                if (encryptedFiles.get(row).isIsFile()) {
                    filesList.add(encryptedFiles.get(row).getName());
                }
            }
            
            if (!filesList.isEmpty()) {
                
                Component _this = this;
                
                Runnable runnable = () -> {
                    try {
                        List<File> files = bravoFile.extractFilesToCacheFolder(filesList);
                        if (!files.isEmpty()) {
                            for (File file : files) {
                                desktop.open(file);
                            }
                        }
                    } catch (Exception ex) {
                        ErrorDialog.showException(
                            (Frame) _this,
                            "Erro ao extrair o(s) arquivo(s) selecionado(s).",
                            ex
                        );
                    }            
                };
                
                ProgressDialog1 progressDialog = new ProgressDialog1(
                    this,
                    "EXTRAINDO ARQUIVO...",
                    runnable,
                    true
                );
                
                bravoFile.addListener(progressDialog);
                
                progressDialog.setVisible(true);
                
                bravoFile.removeListener(progressDialog);
                
            }
            
        } catch (Exception ex) {
            
            ErrorDialog.showException(
                (Frame) this,
                "Erro ao extrair o(s) arquivo(s) selecionado(s).",
                ex
            );
            
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }
    
    
    private void renameFileOrFolder() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        FileEntry file = encryptedFiles.get(jtbFiles.getSelectedRow());
        
        RenameDialog renameDialog = new RenameDialog(
            this, 
            file.isIsFile() ? "RENOMEAR ARQUIVO" : "RENOMEAR PASTA", 
            (String) jtbFiles.getValueAt(jtbFiles.getSelectedRow(), 1)
        );
        
        renameDialog.setVisible(true);
        
        if (!renameDialog.isCanceled()) {
        
            String newFileName = renameDialog.getNewFileName();
            
            try {
                
                if (file.isIsFile()) {
                    bravoFile.renameFile(file.getName(), newFileName);
                } else {
                    bravoFile.renameFolder(file.getName(), newFileName);
                }
                
                updateFoldersStack();
                updateTable();
                updateToolBar();
                
            } catch (Exception ex) {
                
                ErrorDialog.showException(
                    (Frame) this,
                    "ERRO",
                    ex
                );
                
            }
        
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }
    
    
    private void moveFilesAndFoldersTo() {
        
        if (hasFilesInFolder()) {
            
            Frame _this = this;
            
            new Thread() {
                
                @Override
                public void run() {
                    
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    
                    FoldersTreeDialog dialog = new FoldersTreeDialog(
                        _this,
                        "MOVER ARQUIVOS PARA",
                        bravoFile
                    );
                    
                    dialog.setVisible(true);
                    
                    if (dialog.getSelectedFolder() != null) {
                        
                        jlMessage2.setVisible(true);
                        
                        try {
                            
                            String destinationFolder = dialog.getSelectedFolder();
                            List<String> filesAndFolders = getSelectedFiles();
                            
                            bravoFile.moveFilesAndFolders(filesAndFolders, destinationFolder);
                            
                            foldersStack.clear();
                            
                            List<String> parents = new ArrayList<>();
                            String parent = destinationFolder;
                            
                            do {
                                parent = bravoFile.getParentPath(parent);
                                parents.add(parent);
                            } while (!parent.equals(BravoFile.FILE_SEPARATOR));
                            
                            if (!destinationFolder.equals(BravoFile.FILE_SEPARATOR)) {
                                for (int i = parents.size() -1 ; i >= 0; i--) {
                                    foldersStack.push(parents.get(i));
                                }
                            }
                            
                            setRootFolder(destinationFolder);
                            
                        } catch (Exception ex) {
                            
                            ErrorDialog.showException(
                                _this,
                                "Erro ao mover o(s) arquivo(s).",
                                ex
                            );
                            
                        }
                        
                        jlMessage2.setVisible(false);
                        
                    }
                    
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    
                }
                
            }.start();
            
        }
        
    }
    
    
    private void createNewEmptyFolder() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        String folderName = JOptionPaneEx.showInputDialog(
            this,
            "Nome", 
            "CRIAR NOVA PASTA",
            JOptionPaneEx.PLAIN_MESSAGE
        );
        
        if (folderName != null) {
            try {
                bravoFile.addNewEmptyFolder(folderName);
                updateTable();
                updateToolBar();
            } catch (Exception ex) {
                ErrorDialog.showException(
                    (Frame) this,
                    "Erro ao criar nova pasta.",
                    ex
                );
            }
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }
    
    
    private void pasteFilesAndFoldersFromClipboard() {
        
        Transferable contents = clipboard.getContents(null);
        
        boolean hasFiles = (contents != null) && contents.isDataFlavorSupported(
        DataFlavor.javaFileListFlavor);
        
        if (hasFiles) {
            
            try {
                
                List<File> filesList = (List<File>) contents.getTransferData(
                    DataFlavor.javaFileListFlavor
                );
                
                if (filesList != null && !filesList.isEmpty()) {
                    
                    File parent = filesList.get(0).getParentFile();
                    File rootFolder = new File(RootFolder.getAbsolutePath());
                    boolean paste = true;
                    
                    do {
                        if (parent.equals(rootFolder)) {
                            paste = false;
                            break;
                        }
                    } while ((parent = parent.getParentFile()) != null);
                    
                    if (paste) {
                        addFilesAndFolders(filesList);
                    }
                    
                }
                
            } catch (Exception ex){
                
                ErrorDialog.showException(
                    this,
                    "Erro ao excluir o(s) arquivo(s) selecionado(s).",
                    ex
                );
                
            }
            
        }
        
    }
     

    private void setTableModel(TableModel model) {
        
        jtbFiles.setModel(model);
        
        TableCellRenderer renderer = new DefaultTableCellRenderer();
        
        for (int i = 0; i < jtbFiles.getColumnModel().getColumnCount(); i++) {
            jtbFiles.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        
        int columnSize = 140;
        
        jtbFiles.setRowHeight(DefaultTableCellRenderer.ICON_SIZE);
        jtbFiles.getColumnModel().getColumn(0).setMinWidth(0);
        jtbFiles.getColumnModel().getColumn(0).setMaxWidth(0);
        jtbFiles.getColumnModel().getColumn(2).setMinWidth(columnSize);
        jtbFiles.getColumnModel().getColumn(2).setMaxWidth(columnSize);
        jtbFiles.getColumnModel().getColumn(3).setMinWidth(columnSize);
        jtbFiles.getColumnModel().getColumn(3).setMaxWidth(columnSize);
        jtbFiles.getColumnModel().getColumn(4).setMinWidth(columnSize);
        jtbFiles.getColumnModel().getColumn(4).setMaxWidth(columnSize);
        
    }
    
    
    private void updateTable() {
        
        String[] tableColumns = new String[] {
            "",
            "NOME",
            "TAMANHO",
            "CRIADO EM",
            "MODIFICADO"
        };
        
        encryptedFiles.clear();
        
        if (isFileOpened()) {
            
            try {
                
                List<FileEntry> foldersList = bravoFile.getSubfoldersFromRootFolder();
                List<FileEntry> filesList = bravoFile.getFilesFromRootFolder();
                
                int numberOfFolders = foldersList.size();
                int numberOfFiles = filesList.size();
                
                encryptedFiles.addAll(foldersList);
                encryptedFiles.addAll(filesList);
                
                setTableModel(new DefaultTableModel(tableColumns, encryptedFiles.size()) {                
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                });
                
                for (int i = 0; i < encryptedFiles.size(); i++) {
                    
                    FileEntry file = encryptedFiles.get(i);                    
                    jtbFiles.setValueAt(file.isIsFile(),  i, 0);
                    String fileName = file.getName();
                    
                    if (!fileName.equals("...")) {  
                        fileName = extractFileName(fileName);
                    }
                    
                    jtbFiles.setValueAt(fileName,  i, 1);
                    
                    if (file.isIsFile()) {
                        Date creationTime = new Date(file.getCreatedTime());
                        Date lastUpdTime = new Date(file.getLastModifiedTime());                    
                        jtbFiles.setValueAt(file.getOriginalSize(), i, 2);
                        jtbFiles.setValueAt(creationTime, i, 3);
                        jtbFiles.setValueAt(lastUpdTime, i, 4); 
                    } else {
                        jtbFiles.setValueAt(null, i, 2);
                        jtbFiles.setValueAt(null, i, 3);
                        jtbFiles.setValueAt(null, i, 4); 
                    }
                    
                }
                
                jlMessage1.setText(
                    String.valueOf(numberOfFiles) +
                    " arquivos, " +
                    String.valueOf(numberOfFolders) +
                    " pastas."
                );
                
                if (jtbFiles.getRowCount() > 0) {
                    jtbFiles.setRowSelectionInterval(0, 0); 
                }
                
            } catch (Exception ex) {
                
                JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Erro ao listar arquivos.",
                    JOptionPane.ERROR_MESSAGE
                );
                
            }
            
        } else {
            
            setTableModel(new DefaultTableModel(tableColumns, 0));
            
            jlMessage1.setText("");
            
        }
        
    }
    
    
    private void updateFoldersStack() {
        
        if (isFileOpened()) {
            
            List<ActionListener> listeners = new ArrayList<>();
            listeners.addAll(Arrays.asList(jcbFolders.getActionListeners()));
            
            for (ActionListener listener : listeners) {
                jcbFolders.removeActionListener(listener);
            }
            
            String[] items = new String[foldersStack.size()];
            
            for (int i = 0; i < foldersStack.size(); i++) {
                items[i] = foldersStack.get(i);
            }
            
            jcbFolders.setModel(new DefaultComboBoxModel<>(items));
            
            if (jcbFolders.getItemCount() > 0) {
                jcbFolders.setSelectedIndex(jcbFolders.getItemCount() - 1);
            }
            
            jcbFolders.addActionListener((java.awt.event.ActionEvent evt) -> {
                jcbFoldersActionPerformed(evt);
            });
            
            jbUpFolder.setEnabled(foldersStack.size() > 1);
            
        } else {
            
            jcbFolders.setModel(new DefaultComboBoxModel());
            jbUpFolder.setEnabled(false);
            
        }
        
    }
    
    
    private void updateToolBar() {
        if (isFileOpened()) {
            boolean enabled = hasFilesInFolder();
            jbAddFiles.setEnabled(true);
            jbCreateNewFolder.setEnabled(true);
            jbDeleteFiles.setEnabled(enabled);
            jbExtractFiles.setEnabled(enabled);
            jbExtractAllFiles.setEnabled(enabled);
            jbPasteFiles.setEnabled(true);
            jbRenameFile.setEnabled(enabled);
            jbMoveFileTo.setEnabled(enabled);
            pmiDeleteFile.setEnabled(enabled);
            pmiExtractFile.setEnabled(enabled);
            pmiRenameFile.setEnabled(enabled);
            pmiMoveFileTo.setEnabled(enabled);
            pmiOpenFile.setEnabled(enabled);
            jmiFileDetails.setEnabled(true);
        } else {
            jbAddFiles.setEnabled(false);
            jbDeleteFiles.setEnabled(false);
            jbExtractFiles.setEnabled(false);
            jbExtractAllFiles.setEnabled(false);
            jbCreateNewFolder.setEnabled(false);
            jbPasteFiles.setEnabled(false);
            jbRenameFile.setEnabled(false);
            jbMoveFileTo.setEnabled(false);
            pmiDeleteFile.setEnabled(false);
            pmiExtractFile.setEnabled(false);
            pmiMoveFileTo.setEnabled(false);
            pmiRenameFile.setEnabled(false);
            jmiFileDetails.setEnabled(false);
        }
    }
    
    
    private void updateMenu() {        
        if (isFileOpened()) {
            setTitle(bravoFile.getFile().getAbsolutePath());
            jmiCloseFile.setEnabled(true);
            jmiChangePassword.setEnabled(true);
            jmiCommand.setVisible(true);
        } else {
            setTitle(System.getProperty("bravo.version_number"));
            jmiCloseFile.setEnabled(false);
            jmiChangePassword.setEnabled(false);
            jmiCommand.setVisible(false);
        }
    }
    
    
    private boolean isFileOpened() {
        return bravoFile != null;
    }
    
    
    private boolean hasFilesInFolder() {
        return jtbFiles.getRowCount() > 0;
    }

    
    private String extractFileName(String fileName) {
        int lastIndex = fileName.lastIndexOf(BravoFile.FILE_SEPARATOR);
        String shortFileName = fileName.substring(lastIndex + 1, fileName.length());
        if (!shortFileName.equals("")) {
            return shortFileName;
        } else {
            return BravoFile.FILE_SEPARATOR;
        }
    }
    
    
    private void upFoldersHierarchy() {
        try {
            if (foldersStack.size() > 1) {
                foldersStack.pop();
                bravoFile.setRootFolder(foldersStack.peek());
                updateFoldersStack();
                updateTable();
                updateToolBar();
            }
        } catch (Exception ex) {
            ErrorDialog.showException(
                (Frame) this,
                "ERRO",
                ex
            );
        }
    }
    
    
    private List<String> getSelectedFiles() {
        List<String> filesList = new ArrayList<>();
        if (hasFilesInFolder()) {            
            for (int row : jtbFiles.getSelectedRows()) {
                filesList.add(encryptedFiles.get(row).getName());
            }
        }
        return filesList;
    }
    
    
    private void openFileOrFolder() {
        if (encryptedFiles.get(jtbFiles.getSelectedRow()).isIsFolder()) {
            setRootFolder(encryptedFiles.get(jtbFiles.getSelectedRow()).getName());
        } else {
            extractAndOpenFiles();
        }
    }

    
    @Override
    public void setTitle(String title) {
        super.setTitle("Bravo - " + title); 
    }
    
    
    private void clearPreviousSessionsCache() {
        
        CacheCleaner cacheCleaner = new CacheCleaner();
        
        if (cacheCleaner.previousSessionCacheIsNotEmpty()) {
            
            //Destruição de arquivos de sessões anteriores.
            Runnable runnable = () -> {
                try {
                    cacheCleaner.cleanPreviousSessionCache();
                } catch (Exception ex) {
                    ErrorDialog.showException(
                        null,
                        "ERRO AO LIMPAR DADOS DE SESSÕES ANTERIORES",
                        ex
                    );
                }
            };
            
            ProgressDialog1 progressDialog1 = new ProgressDialog1(
                null,
                "LIMPANDO DADOS DE SESSÕES ANTERIORES...",
                runnable,
                false
            );
            
            cacheCleaner.addListener(progressDialog1);
            
            progressDialog1.setVisible(true);
            
            cacheCleaner.removeListener(progressDialog1);
            
        }
        
    }

    
    private void clearCurrentSessionCache() {
        
        Frame _this = this;
        
        CacheCleaner cacheCleaner = new CacheCleaner();
        
        Runnable runnable = () -> {
            try {
                cacheCleaner.cleanCurrentSessionCache();
            } catch (Exception ex) {
                ErrorDialog.showException(
                    _this,
                    "ERRO AO LIMPAR O CACHE DA SESSÃO ATUAL",
                    ex
                );
            }            
        };
        
        ProgressDialog1 progressDialog1 = new ProgressDialog1(
            this,
            "REMOVENDO ARQUIVOS TEMPORÁRIOS...",
            runnable,
            false
        );
        
        cacheCleaner.addListener(progressDialog1);
        
        progressDialog1.setVisible(true);
        
        cacheCleaner.removeListener(progressDialog1);
        
    }

    
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {}
    @Override
    public void dragOver(DropTargetDragEvent dtde) {}
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}
    @Override
    public void dragExit(DropTargetEvent dte) {}

    @Override
    public void drop(DropTargetDropEvent dtde) {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            Transferable tr = dtde.getTransferable();             
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            List<File> list = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
            List<File> filesAndFolders = new ArrayList<>();
            for (Object object : list) {
                filesAndFolders.add((File)object);
            }
            if (!filesAndFolders.isEmpty()) {
                addFilesAndFolders(filesAndFolders);
            }
            dtde.dropComplete(true);
        } catch (Exception e) {
            dtde.rejectDrop();
        }
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpmManageFiles = new javax.swing.JPopupMenu();
        pmiOpenFile = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        pmiRenameFile = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        pmiDeleteFile = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        pmiExtractFile = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        pmiMoveFileTo = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JPopupMenu.Separator();
        pmiPasteFiles = new javax.swing.JMenuItem();
        jToolBar1 = new javax.swing.JToolBar();
        jbAddFiles = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        jbExtractFiles = new javax.swing.JButton();
        jbExtractAllFiles = new javax.swing.JButton();
        jSeparator17 = new javax.swing.JToolBar.Separator();
        jbDeleteFiles = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        jbMoveFileTo = new javax.swing.JButton();
        jSeparator20 = new javax.swing.JToolBar.Separator();
        jbRenameFile = new javax.swing.JButton();
        jSeparator21 = new javax.swing.JToolBar.Separator();
        jbPasteFiles = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        jbCreateNewFolder = new javax.swing.JButton();
        jpStatusBar = new javax.swing.JPanel();
        jlMessage2 = new javax.swing.JLabel();
        jlMessage1 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtpComment = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtbFiles = new javax.swing.JTable();
        jToolBar2 = new javax.swing.JToolBar();
        jbUpFolder = new javax.swing.JButton();
        jcbFolders = new javax.swing.JComboBox<>();
        jMenuBar1 = new javax.swing.JMenuBar();
        jmFolder = new javax.swing.JMenu();
        jmiNewFile = new javax.swing.JMenuItem();
        jSeparator22 = new javax.swing.JPopupMenu.Separator();
        jmiOpenFile = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jmiChangePassword = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jmiFileDetails = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jmiCloseFile = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        jmiExit = new javax.swing.JMenuItem();
        jmiCommand = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        jMenuItem7 = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        jMenuItem9 = new javax.swing.JMenuItem();
        jSeparator24 = new javax.swing.JPopupMenu.Separator();
        jMenuItem10 = new javax.swing.JMenuItem();
        jSeparator25 = new javax.swing.JPopupMenu.Separator();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jmiManual = new javax.swing.JMenuItem();
        jSeparator23 = new javax.swing.JPopupMenu.Separator();
        jmiAbout = new javax.swing.JMenuItem();

        pmiOpenFile.setText("Abrir");
        pmiOpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmiOpenFileActionPerformed(evt);
            }
        });
        jpmManageFiles.add(pmiOpenFile);
        jpmManageFiles.add(jSeparator9);

        pmiRenameFile.setText("Renomear");
        pmiRenameFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmiRenameFileActionPerformed(evt);
            }
        });
        jpmManageFiles.add(pmiRenameFile);
        jpmManageFiles.add(jSeparator8);

        pmiDeleteFile.setText("Excluir");
        pmiDeleteFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmiDeleteFileActionPerformed(evt);
            }
        });
        jpmManageFiles.add(pmiDeleteFile);
        jpmManageFiles.add(jSeparator14);

        pmiExtractFile.setText("Extrair");
        pmiExtractFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmiExtractFileActionPerformed(evt);
            }
        });
        jpmManageFiles.add(pmiExtractFile);
        jpmManageFiles.add(jSeparator16);

        pmiMoveFileTo.setText("Mover Para...");
        pmiMoveFileTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmiMoveFileToActionPerformed(evt);
            }
        });
        jpmManageFiles.add(pmiMoveFileTo);
        jpmManageFiles.add(jSeparator19);

        pmiPasteFiles.setText("Colar");
        pmiPasteFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pmiPasteFilesActionPerformed(evt);
            }
        });
        jpmManageFiles.add(pmiPasteFiles);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            public void windowStateChanged(java.awt.event.WindowEvent evt) {
                formWindowStateChanged(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jToolBar1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToolBar1.setFloatable(false);

        jbAddFiles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/icons/32x32/file_add.png"))); // NOI18N
        jbAddFiles.setText("Adicionar");
        jbAddFiles.setToolTipText("Adicionar arquivos");
        jbAddFiles.setFocusable(false);
        jbAddFiles.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbAddFiles.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbAddFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddFilesActionPerformed(evt);
            }
        });
        jToolBar1.add(jbAddFiles);
        jToolBar1.add(jSeparator5);

        jbExtractFiles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/icons/32x32/file_extract.png"))); // NOI18N
        jbExtractFiles.setText("Extrair");
        jbExtractFiles.setToolTipText("Extrair os arquivos selecionados");
        jbExtractFiles.setFocusable(false);
        jbExtractFiles.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbExtractFiles.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbExtractFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbExtractFilesActionPerformed(evt);
            }
        });
        jToolBar1.add(jbExtractFiles);

        jbExtractAllFiles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/icons/32x32/extract_all.png"))); // NOI18N
        jbExtractAllFiles.setText("Extrair Tudo");
        jbExtractAllFiles.setToolTipText("Extrair todos os arquivos");
        jbExtractAllFiles.setFocusable(false);
        jbExtractAllFiles.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbExtractAllFiles.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbExtractAllFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbExtractAllFilesActionPerformed(evt);
            }
        });
        jToolBar1.add(jbExtractAllFiles);
        jToolBar1.add(jSeparator17);

        jbDeleteFiles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/icons/32x32/file_delete.png"))); // NOI18N
        jbDeleteFiles.setText("Excluir");
        jbDeleteFiles.setToolTipText("Excluir os arquivos selecionados");
        jbDeleteFiles.setFocusable(false);
        jbDeleteFiles.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbDeleteFiles.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbDeleteFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDeleteFilesActionPerformed(evt);
            }
        });
        jToolBar1.add(jbDeleteFiles);
        jToolBar1.add(jSeparator7);

        jbMoveFileTo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/icons/32x32/file_move.png"))); // NOI18N
        jbMoveFileTo.setText("Mover Para");
        jbMoveFileTo.setToolTipText("Mover os arquivos selecionados para outra pasta");
        jbMoveFileTo.setFocusable(false);
        jbMoveFileTo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbMoveFileTo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbMoveFileTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbMoveFileToActionPerformed(evt);
            }
        });
        jToolBar1.add(jbMoveFileTo);
        jToolBar1.add(jSeparator20);

        jbRenameFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/icons/32x32/file_rename.png"))); // NOI18N
        jbRenameFile.setText("Renomear");
        jbRenameFile.setToolTipText("Renomear o arquivo selecionado");
        jbRenameFile.setFocusable(false);
        jbRenameFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbRenameFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbRenameFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbRenameFileActionPerformed(evt);
            }
        });
        jToolBar1.add(jbRenameFile);
        jToolBar1.add(jSeparator21);

        jbPasteFiles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/icons/32x32/edit_paste.png"))); // NOI18N
        jbPasteFiles.setText("Colar");
        jbPasteFiles.setToolTipText("Inserir os arquivos da área de transferência");
        jbPasteFiles.setFocusable(false);
        jbPasteFiles.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbPasteFiles.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbPasteFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbPasteFilesActionPerformed(evt);
            }
        });
        jToolBar1.add(jbPasteFiles);
        jToolBar1.add(jSeparator12);

        jbCreateNewFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/icons/32x32/folder_add.png"))); // NOI18N
        jbCreateNewFolder.setText("Nova Pasta");
        jbCreateNewFolder.setToolTipText("Criar uma nova pasta");
        jbCreateNewFolder.setFocusable(false);
        jbCreateNewFolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbCreateNewFolder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbCreateNewFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCreateNewFolderActionPerformed(evt);
            }
        });
        jToolBar1.add(jbCreateNewFolder);

        jpStatusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jlMessage2.setForeground(new java.awt.Color(102, 102, 102));
        jlMessage2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jlMessage2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/gifs/loading.gif"))); // NOI18N
        jlMessage2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        jlMessage1.setText("[message1]");
        jlMessage1.setToolTipText("");

        javax.swing.GroupLayout jpStatusBarLayout = new javax.swing.GroupLayout(jpStatusBar);
        jpStatusBar.setLayout(jpStatusBarLayout);
        jpStatusBarLayout.setHorizontalGroup(
            jpStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpStatusBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlMessage1, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jlMessage2)
                .addContainerGap())
        );
        jpStatusBarLayout.setVerticalGroup(
            jpStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jlMessage2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jlMessage1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setDividerLocation(789);
        jSplitPane1.setDividerSize(3);

        jtpComment.setToolTipText("Duplo clique para editar");
        jtpComment.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jtpCommentFocusLost(evt);
            }
        });
        jtpComment.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jtpCommentMouseClicked(evt);
            }
        });
        jtpComment.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jtpCommentKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jtpCommentKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(jtpComment);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(jPanel3);

        jtbFiles.setComponentPopupMenu(jpmManageFiles);
        jtbFiles.setFillsViewportHeight(true);
        jtbFiles.setRowHeight(32);
        jtbFiles.setShowGrid(false);
        jtbFiles.getTableHeader().setReorderingAllowed(false);
        jtbFiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jtbFilesMouseClicked(evt);
            }
        });
        jtbFiles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jtbFilesKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(jtbFiles);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jToolBar2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToolBar2.setFloatable(false);

        jbUpFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bravo/gui/icons/16x16/up.png"))); // NOI18N
        jbUpFolder.setToolTipText("Voltar ao diretório anterior");
        jbUpFolder.setFocusable(false);
        jbUpFolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbUpFolder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbUpFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbUpFolderActionPerformed(evt);
            }
        });
        jToolBar2.add(jbUpFolder);

        jcbFolders.setToolTipText("Hierarquia de diretórios");
        jcbFolders.setRenderer(new bravo.gui.dialogs.DefaultListCellRenderer());
        jcbFolders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbFoldersActionPerformed(evt);
            }
        });
        jToolBar2.add(jcbFolders);

        jMenuBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jMenuBar1.setMinimumSize(new java.awt.Dimension(186, 28));
        jMenuBar1.setPreferredSize(new java.awt.Dimension(186, 32));

        jmFolder.setText("Arquivo");

        jmiNewFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jmiNewFile.setText("Criar Novo");
        jmiNewFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiNewFileActionPerformed(evt);
            }
        });
        jmFolder.add(jmiNewFile);
        jmFolder.add(jSeparator22);

        jmiOpenFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jmiOpenFile.setText("Abrir");
        jmiOpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiOpenFileActionPerformed(evt);
            }
        });
        jmFolder.add(jmiOpenFile);
        jmFolder.add(jSeparator3);

        jmiChangePassword.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jmiChangePassword.setText("Trocar Senha               ");
        jmiChangePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiChangePasswordActionPerformed(evt);
            }
        });
        jmFolder.add(jmiChangePassword);
        jmFolder.add(jSeparator1);

        jmiFileDetails.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jmiFileDetails.setText("Detalhes");
        jmiFileDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiFileDetailsActionPerformed(evt);
            }
        });
        jmFolder.add(jmiFileDetails);
        jmFolder.add(jSeparator6);

        jmiCloseFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jmiCloseFile.setText("Fechar");
        jmiCloseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiCloseFileActionPerformed(evt);
            }
        });
        jmFolder.add(jmiCloseFile);
        jmFolder.add(jSeparator11);

        jmiExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        jmiExit.setText("Sair");
        jmiExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiExitActionPerformed(evt);
            }
        });
        jmFolder.add(jmiExit);

        jMenuBar1.add(jmFolder);

        jmiCommand.setText("Itens");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem2.setText("Adicionar");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jmiCommand.add(jMenuItem2);
        jmiCommand.add(jSeparator4);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem3.setText("Extrair");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jmiCommand.add(jMenuItem3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem4.setText("Extrair Tudo               ");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jmiCommand.add(jMenuItem4);
        jmiCommand.add(jSeparator10);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        jMenuItem5.setText("Excluir");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jmiCommand.add(jMenuItem5);
        jmiCommand.add(jSeparator13);

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem6.setText("Mover Para");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jmiCommand.add(jMenuItem6);
        jmiCommand.add(jSeparator15);

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem7.setText("Renomear");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jmiCommand.add(jMenuItem7);
        jmiCommand.add(jSeparator18);

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem9.setText("Colar");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jmiCommand.add(jMenuItem9);
        jmiCommand.add(jSeparator24);

        jMenuItem10.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem10.setText("Nova Pasta");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jmiCommand.add(jMenuItem10);
        jmiCommand.add(jSeparator25);

        jMenuItem11.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItem11.setText("Abrir");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jmiCommand.add(jMenuItem11);

        jMenuBar1.add(jmiCommand);

        jMenu4.setText("Ajuda");

        jmiManual.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jmiManual.setText("Manual               ");
        jMenu4.add(jmiManual);
        jMenu4.add(jSeparator23);

        jmiAbout.setText("Sobre");
        jmiAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmiAboutActionPerformed(evt);
            }
        });
        jMenu4.add(jmiAbout);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jpStatusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jSplitPane1)
                        .addGap(1, 1, 1))
                    .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jSplitPane1)
                .addGap(0, 0, 0)
                .addComponent(jpStatusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbAddFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddFilesActionPerformed
        addFilesAndFolders();
    }//GEN-LAST:event_jbAddFilesActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Config.putInt("divider_location_1", jSplitPane1.getDividerLocation()); 
        if (isFileOpened()) {
            closeFile();
        }
        clearCurrentSessionCache();
    }//GEN-LAST:event_formWindowClosing

    private void jmiNewFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiNewFileActionPerformed
        createNewFile();
    }//GEN-LAST:event_jmiNewFileActionPerformed

    private void jmiOpenFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiOpenFileActionPerformed
        openFile();
    }//GEN-LAST:event_jmiOpenFileActionPerformed

    private void jmiChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiChangePasswordActionPerformed
        changeFilePassword();
    }//GEN-LAST:event_jmiChangePasswordActionPerformed

    private void jmiCloseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiCloseFileActionPerformed
        closeFile();
    }//GEN-LAST:event_jmiCloseFileActionPerformed

    private void jmiExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiExitActionPerformed
        clearCurrentSessionCache();
        System.exit(0);
    }//GEN-LAST:event_jmiExitActionPerformed

    private void jmiAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiAboutActionPerformed
        new AboutDialog(this).setVisible(true);
    }//GEN-LAST:event_jmiAboutActionPerformed

    private void jtpCommentKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtpCommentKeyReleased
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_CONTROL) {
            ctrlLocked = false;
        }
    }//GEN-LAST:event_jtpCommentKeyReleased

    private void jtpCommentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtpCommentKeyPressed
        if (isFileOpened()) {
            if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_CONTROL) {
                ctrlLocked = true;
            }
            if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                if (!ctrlLocked) {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    jtpComment.setEnabled(false);
                    jtpComment.setBackground(Color.WHITE);
                    try {
                        bravoFile.setComment(jtpComment.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                } else {
                    jtpComment.setText(jtpComment.getText() + "\n");
                }
            }
        }
    }//GEN-LAST:event_jtpCommentKeyPressed

    private void jtpCommentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jtpCommentMouseClicked
        if (bravoFile != null) {
            if (evt.getClickCount() == 2) {
                jtpComment.setEnabled(true);
                jtpComment.requestFocus();
            }
        }
    }//GEN-LAST:event_jtpCommentMouseClicked

    private void jtpCommentFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtpCommentFocusLost
        if (isFileOpened()) {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            jtpComment.setEnabled(false);
            jtpComment.setBackground(Color.WHITE);
            try {
                bravoFile.setComment(jtpComment.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_jtpCommentFocusLost

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        new Thread(new Runnable() {
            @Override
            public void run() {
                jSplitPane1.setDividerLocation(Config.getInt("divider_location_1", 250));
                clearPreviousSessionsCache();
            }
        }).start();        
    }//GEN-LAST:event_formWindowOpened

    private void jtbFilesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jtbFilesMouseClicked
        if (evt.getClickCount() == 2) {
            openFileOrFolder();
        }
    }//GEN-LAST:event_jtbFilesMouseClicked

    private void jcbFoldersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbFoldersActionPerformed
        setRootFoolder();
    }//GEN-LAST:event_jcbFoldersActionPerformed

    private void jbUpFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbUpFolderActionPerformed
        upFoldersHierarchy();
    }//GEN-LAST:event_jbUpFolderActionPerformed

    private void jbExtractFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbExtractFilesActionPerformed
        extractFilesAndFolders();
    }//GEN-LAST:event_jbExtractFilesActionPerformed

    private void jbCreateNewFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCreateNewFolderActionPerformed
        createNewEmptyFolder();
    }//GEN-LAST:event_jbCreateNewFolderActionPerformed

    private void jbDeleteFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDeleteFilesActionPerformed
        removeFilesAndFolders();
    }//GEN-LAST:event_jbDeleteFilesActionPerformed

    private void jbExtractAllFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbExtractAllFilesActionPerformed
        extractAllFilesAndFolders();
    }//GEN-LAST:event_jbExtractAllFilesActionPerformed

    private void pmiRenameFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmiRenameFileActionPerformed
        renameFileOrFolder();
    }//GEN-LAST:event_pmiRenameFileActionPerformed

    private void pmiOpenFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmiOpenFileActionPerformed
        if (jtbFiles.getSelectedRow() != 0) {
            if (encryptedFiles.get(jtbFiles.getSelectedRow()).isIsFolder()) {
                setRootFolder(encryptedFiles.get(jtbFiles.getSelectedRow()).getName());
            } else {
                extractAndOpenFiles();
            }
        } else {
            upFoldersHierarchy();
        }
    }//GEN-LAST:event_pmiOpenFileActionPerformed

    private void pmiDeleteFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmiDeleteFileActionPerformed
        removeFilesAndFolders();
    }//GEN-LAST:event_pmiDeleteFileActionPerformed

    private void pmiExtractFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmiExtractFileActionPerformed
        extractFilesAndFolders();
    }//GEN-LAST:event_pmiExtractFileActionPerformed

    private void pmiMoveFileToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmiMoveFileToActionPerformed
        moveFilesAndFoldersTo();
    }//GEN-LAST:event_pmiMoveFileToActionPerformed

    private void jbPasteFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbPasteFilesActionPerformed
        pasteFilesAndFoldersFromClipboard();
    }//GEN-LAST:event_jbPasteFilesActionPerformed

    private void jtbFilesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtbFilesKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER -> {
                if (encryptedFiles.get(jtbFiles.getSelectedRow()).isIsFolder()) {
                    setRootFolder(encryptedFiles.get(jtbFiles.getSelectedRow()).getName());
                } else {
                    extractAndOpenFiles();
                }
            }
        }
    }//GEN-LAST:event_jtbFilesKeyPressed

    private void jbRenameFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbRenameFileActionPerformed
        renameFileOrFolder();
    }//GEN-LAST:event_jbRenameFileActionPerformed

    private void jbMoveFileToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbMoveFileToActionPerformed
        moveFilesAndFoldersTo();
    }//GEN-LAST:event_jbMoveFileToActionPerformed

    private void jmiFileDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmiFileDetailsActionPerformed
        new DetailsDialog(this, bravoFile).setVisible(true);
    }//GEN-LAST:event_jmiFileDetailsActionPerformed

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        if (evt.getOldState() == 0) {
            jSplitPane1.setDividerLocation(Config.getInt("divider_location_1", 250));
            jSplitPane1.updateUI();
        }
    }//GEN-LAST:event_formWindowStateChanged

    private void pmiPasteFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pmiPasteFilesActionPerformed
        pasteFilesAndFoldersFromClipboard();
    }//GEN-LAST:event_pmiPasteFilesActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        addFilesAndFolders();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        extractFilesAndFolders();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        extractAllFilesAndFolders();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        removeFilesAndFolders();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        moveFilesAndFoldersTo();
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        renameFileOrFolder();
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        pasteFilesAndFoldersFromClipboard();
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        createNewEmptyFolder();
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        openFileOrFolder();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JToolBar.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JPopupMenu.Separator jSeparator19;
    private javax.swing.JToolBar.Separator jSeparator20;
    private javax.swing.JToolBar.Separator jSeparator21;
    private javax.swing.JPopupMenu.Separator jSeparator22;
    private javax.swing.JPopupMenu.Separator jSeparator23;
    private javax.swing.JPopupMenu.Separator jSeparator24;
    private javax.swing.JPopupMenu.Separator jSeparator25;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JButton jbAddFiles;
    private javax.swing.JButton jbCreateNewFolder;
    private javax.swing.JButton jbDeleteFiles;
    private javax.swing.JButton jbExtractAllFiles;
    private javax.swing.JButton jbExtractFiles;
    private javax.swing.JButton jbMoveFileTo;
    private javax.swing.JButton jbPasteFiles;
    private javax.swing.JButton jbRenameFile;
    private javax.swing.JButton jbUpFolder;
    private javax.swing.JComboBox<String> jcbFolders;
    private javax.swing.JLabel jlMessage1;
    private javax.swing.JLabel jlMessage2;
    private javax.swing.JMenu jmFolder;
    private javax.swing.JMenuItem jmiAbout;
    private javax.swing.JMenuItem jmiChangePassword;
    private javax.swing.JMenuItem jmiCloseFile;
    private javax.swing.JMenu jmiCommand;
    private javax.swing.JMenuItem jmiExit;
    private javax.swing.JMenuItem jmiFileDetails;
    private javax.swing.JMenuItem jmiManual;
    private javax.swing.JMenuItem jmiNewFile;
    private javax.swing.JMenuItem jmiOpenFile;
    private javax.swing.JPanel jpStatusBar;
    private javax.swing.JPopupMenu jpmManageFiles;
    private javax.swing.JTable jtbFiles;
    private javax.swing.JTextPane jtpComment;
    private javax.swing.JMenuItem pmiDeleteFile;
    private javax.swing.JMenuItem pmiExtractFile;
    private javax.swing.JMenuItem pmiMoveFileTo;
    private javax.swing.JMenuItem pmiOpenFile;
    private javax.swing.JMenuItem pmiPasteFiles;
    private javax.swing.JMenuItem pmiRenameFile;
    // End of variables declaration//GEN-END:variables

}
