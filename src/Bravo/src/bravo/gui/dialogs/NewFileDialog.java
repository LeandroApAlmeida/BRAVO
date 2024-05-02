package bravo.gui.dialogs;

import bravo.environment.Config;
import bravo.file.Argon2Params;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JOptionPane;

public class NewFileDialog extends javax.swing.JDialog {
    
    
    private final String ITERATIONS_KEY = "iterations";
    
    private final String MEMORY_KEY = "memory";
    
    private final String PARALLELISM_KEY = "parallelism";
    
    private final boolean confirmPassword;
    
    private boolean canceled;

    
    public NewFileDialog(java.awt.Frame parent, String title, File file, boolean confirmPassword) {
        
        super(parent, true);
        
        this.confirmPassword = confirmPassword;
        this.canceled = true;
        
        initComponents();
        
        setTitle(title);
        
        jtfPath.setText(file.getAbsolutePath());
        jtfPath.setToolTipText(jtfPath.getText());
        
        showPasswords();
        
        setLocationRelativeTo(parent);
        
        if (!this.confirmPassword) {
            jLabel3.setVisible(false);
            jpfPassword2.setVisible(false);
            jLabel2.setText("Senha:");
            jcbShowPassword.setText("Mostrar Senha");
        }
        
        loadArgon2Params();
        
    }
    
    
    private void loadArgon2Params() {
        jspIterations.setValue(Config.getInt(ITERATIONS_KEY, 1));
        jspMemory.setValue(Config.getInt(MEMORY_KEY, 1));
        jspParallelism.setValue(Config.getInt(PARALLELISM_KEY, 1));
    }
    
    
    private void saveArgon2Params() {
        Config.putInt(ITERATIONS_KEY, (int) jspIterations.getValue());
        Config.putInt(MEMORY_KEY, (int) jspMemory.getValue());
        Config.putInt(PARALLELISM_KEY, (int) jspParallelism.getValue());
    }
    

    private void confirm() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        try {
            
            if (confirmPassword) {
                
                if (jpfPassword1.getPassword().length == 0) {                
                    jpfPassword1.requestFocus();
                    throw new Exception("Campo Senha (1) não pode estar vazio.");
                }
                
                if (jpfPassword2.getPassword().length == 0) {                
                    jpfPassword2.requestFocus();
                    throw new Exception("Campo Senha (2) não pode estar vazio.");
                }
                
                String password1 = new String(jpfPassword1.getPassword());
                String password2 = new String(jpfPassword2.getPassword());
                
                if (!password1.equals(password2)) {
                    throw new Exception("Campos Senha (1) e Senha (2) contém valores\ndiferentes.");
                }
                
            } else {
                
                if (jpfPassword1.getPassword().length == 0) {                
                    jpfPassword1.requestFocus();
                    throw new Exception("Campo Senha não pode estar vazio.");
                }
                
            }
            
            saveArgon2Params();
            canceled = false;
            setVisible(false);
            
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
    
    
    private void showPasswords() {
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        if (jcbShowPassword.isSelected()) {
            jpfPassword1.setEchoChar('\u0000');
            jpfPassword2.setEchoChar('\u0000');
        } else {
            jpfPassword1.setEchoChar('\u25cf');
            jpfPassword2.setEchoChar('\u25cf');
        }
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
    }

    
    public char[] getPassword() {
        return jpfPassword1.getPassword();
    }
    
    
    public Argon2Params getParams() {
        return new Argon2Params(
            (int) jspIterations.getValue(),
            (int) jspMemory.getValue(),
            (int) jspParallelism.getValue()
        );
    }
    
    
    @Override
    public void dispose() {
        jpfPassword1.setText(null);
        jpfPassword2.setText(null);
        super.dispose();
    }

    
    public boolean isCanceled() {
        return canceled;
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jtfPath = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jpfPassword1 = new javax.swing.JPasswordField();
        jpfPassword2 = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        jbConfirm = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jspIterations = new javax.swing.JSpinner();
        jspMemory = new javax.swing.JSpinner();
        jspParallelism = new javax.swing.JSpinner();
        jcbShowPassword = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Arquivo"));

        jtfPath.setEditable(false);
        jtfPath.setFocusable(false);

        jLabel1.setText("Caminho:");

        jLabel2.setText("Senha (1):");

        jpfPassword1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jpfPassword1KeyPressed(evt);
            }
        });

        jpfPassword2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jpfPassword2KeyPressed(evt);
            }
        });

        jLabel3.setText("Senha (2):");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(32, 32, 32)
                        .addComponent(jpfPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(jLabel1)))
                        .addGap(31, 31, 31)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jpfPassword1, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                            .addComponent(jtfPath))))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtfPath, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jpfPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jpfPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jbConfirm.setText("Confirmar");
        jbConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbConfirmActionPerformed(evt);
            }
        });

        jbCancel.setText("Cancelar");
        jbCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCancelActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Custo"));

        jLabel4.setText("Iterações:");

        jLabel5.setText("Memória:");

        jLabel6.setText("Paralelismo:");

        jspIterations.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));

        jspMemory.setModel(new javax.swing.SpinnerNumberModel(1024, 1024, null, 1));

        jspParallelism.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jspIterations, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jspMemory, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jspParallelism, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jspIterations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jspMemory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jspParallelism, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jcbShowPassword.setText("Mostrar Senhas");
        jcbShowPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbShowPasswordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jcbShowPassword)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jbConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbShowPassword)
                    .addComponent(jbCancel)
                    .addComponent(jbConfirm))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jpfPassword1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jpfPassword1KeyPressed
        if (!confirmPassword) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                confirm();
            }
        }
    }//GEN-LAST:event_jpfPassword1KeyPressed

    private void jpfPassword2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jpfPassword2KeyPressed
        if (confirmPassword) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                confirm();
            }
        }
    }//GEN-LAST:event_jpfPassword2KeyPressed

    private void jbConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbConfirmActionPerformed
        confirm();
    }//GEN-LAST:event_jbConfirmActionPerformed

    private void jbCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jbCancelActionPerformed

    private void jcbShowPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbShowPasswordActionPerformed
        showPasswords();
    }//GEN-LAST:event_jcbShowPasswordActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbConfirm;
    private javax.swing.JCheckBox jcbShowPassword;
    private javax.swing.JPasswordField jpfPassword1;
    private javax.swing.JPasswordField jpfPassword2;
    private javax.swing.JSpinner jspIterations;
    private javax.swing.JSpinner jspMemory;
    private javax.swing.JSpinner jspParallelism;
    private javax.swing.JTextField jtfPath;
    // End of variables declaration//GEN-END:variables
}
