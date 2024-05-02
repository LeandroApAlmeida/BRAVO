package bravo.gui.dialogs;

import bravo.file.FileOperation;
import bravo.file.ProcessListener;

public class ProgressDialog2 extends javax.swing.JDialog implements ProcessListener {
    
    
    private boolean abort = false;
    
    private final Thread thread;
    
    
    public ProgressDialog2(java.awt.Frame parent, String title, Runnable runnable,
    boolean enableCancel) {
        
        super(parent, true);
        
        this.thread = new Thread(runnable);
        
        initComponents();
        
        setLocationRelativeTo(parent);
        
        setTitle(title);
        
        jbCancel.setEnabled(enableCancel);
        
        jtfFile.setText("");
        
        jpbTotal.setMaximum(100);
        jpbTotal.setMinimum(0);
        jpbTotal.setValue(0);
        
    }
    
    
    private void cancel()  {
        abort = true;
        jbCancel.setEnabled(false);
    }
    
    
    @Override
    public void updateFile(String file, FileOperation operation) {
        
        String mode = "";
        
        switch (operation) {
            case ADD -> mode = "Adicionando";
            case REMOVE -> mode = "Removendo";
            case EXTRACT -> mode = "Extraindo";
            case ENCRYPT -> mode = "Encriptando";
            case WIPE -> mode = "Apagando";
        }
        
        jtfFile.setText(mode + " " + file);
        
    }

    
    @Override
    public void updateTotalPercentage(int percentage) {
        jpbTotal.setValue(percentage);
    }

    
    @Override
    public void updateFilePercentage(int percentage) {        
    }

    
    @Override
    public void done() {
        setVisible(false);
    }

    
    @Override
    public boolean abort() {
        return abort;    
    }
    
    
    @Override
    public void abortBlocked(boolean status) {
        if (status == true) {
            jbCancel.setEnabled(false);
        } else {
            jbCancel.setEnabled(true);
        }
    }

    
    @Override
    public void setVisible(boolean b) {
        if (b) thread.start();
        super.setVisible(b);
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jpbTotal = new javax.swing.JProgressBar();
        jtfFile = new javax.swing.JTextField();
        jbCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("  Progresso  "));

        jpbTotal.setStringPainted(true);

        jtfFile.setEditable(false);
        jtfFile.setText("[file]");
        jtfFile.setBorder(null);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jpbTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
                    .addComponent(jtfFile))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addComponent(jtfFile, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpbTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

        jbCancel.setText("Cancelar");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(242, 242, 242)
                        .addComponent(jbCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jbCancel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        cancel();
    }//GEN-LAST:event_jbCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbCancel;
    private javax.swing.JProgressBar jpbTotal;
    private javax.swing.JTextField jtfFile;
    // End of variables declaration//GEN-END:variables

}
