package bravo.gui.dialogs;

import bravo.file.BravoFile;
import bravo.file.FileEntry;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;


public class FoldersTreeDialog extends javax.swing.JDialog {

    
    private String selectedFolder = null;
    private final BravoFile bravoFile;
    
    
    public FoldersTreeDialog(java.awt.Frame parent, String title, BravoFile bravoFile) {
        super(parent, true);
        this.bravoFile = bravoFile;
        initComponents();
        setTitle(title);
        setLocationRelativeTo(parent);
        loadFoldersTree();
    }
    
    
    private String extractFileName(String file) {
        return file.substring(file.lastIndexOf(BravoFile.FILE_SEPARATOR) + 1, file.length());
    }
    
    
    private void listSubfolders(FileEntry file, DefaultMutableTreeNode treeNode) {
        List<FileEntry> encryptedFiles = bravoFile.getSubfoldersFromFolder(file.getName());
        for (FileEntry encryptedFile : encryptedFiles) {
            DefaultMutableTreeNode treeNode2 = new DefaultMutableTreeNode(
            extractFileName(encryptedFile.getName()));
            treeNode.add(treeNode2);
            listSubfolders(encryptedFile, treeNode2);
        }
    }
    
    
    private void loadFoldersTree() {
        
        List<FileEntry> encryptedFiles = bravoFile.getSubfoldersFromFolder(BravoFile.FILE_SEPARATOR);
        
        DefaultMutableTreeNode treeNode1 = new DefaultMutableTreeNode(BravoFile.FILE_SEPARATOR);
        
        for (FileEntry encryptedFile : encryptedFiles) {
            DefaultMutableTreeNode treeNode2 = new DefaultMutableTreeNode(
            extractFileName(encryptedFile.getName()));
            treeNode1.add(treeNode2);
            listSubfolders(encryptedFile, treeNode2);
        }
        
        jtFolders.setCellRenderer(new TreeCellRenderer());
        jtFolders.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        
        if (jtFolders.getRowCount() > 1) {
            jtFolders.setSelectionRow(0);
        } else {
            jtFolders.setEnabled(false);
            jbConfirm.setEnabled(false);
        }
        
    }
    
    
    private void confirm() {
        
        Object[] objs = jtFolders.getLeadSelectionPath().getPath();
        
        if (objs.length > 1) {
            StringBuilder path = new StringBuilder();
            for (int i = 1; i < objs.length; i++) {
                path.append(BravoFile.FILE_SEPARATOR);
                path.append(objs[i].toString());           
            }
            selectedFolder = path.toString();
        } else {
            selectedFolder = objs[0].toString();
        }
        
        setVisible(false);
        
    }

    
    public String getSelectedFolder() {
        return selectedFolder;
    }
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtFolders = new javax.swing.JTree();
        jbCancel = new javax.swing.JButton();
        jbConfirm = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("  Pastas  "));

        jScrollPane1.setBorder(null);

        jtFolders.setBackground(jPanel1.getBackground());
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jtFolders.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jtFolders.setRowHeight(20);
        jScrollPane1.setViewportView(jtFolders);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
        );

        jbCancel.setText("Cancelar");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });

        jbConfirm.setText("Confirmar");
        jbConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbConfirmActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 382, Short.MAX_VALUE)
                        .addComponent(jbConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(8, 8, 8))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbCancel)
                    .addComponent(jbConfirm))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jbCancelActionPerformed

    private void jbConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbConfirmActionPerformed
        confirm();
    }//GEN-LAST:event_jbConfirmActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbConfirm;
    private javax.swing.JTree jtFolders;
    // End of variables declaration//GEN-END:variables

}
