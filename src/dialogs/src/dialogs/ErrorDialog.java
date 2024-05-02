package dialogs;

import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ErrorDialog extends javax.swing.JDialog {

    
    private ErrorDialog(Frame parent) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
    }

    
    public static void showException(Frame parent, String msg, Throwable ex) {
        ErrorDialog errorDlg = new ErrorDialog(parent);
        errorDlg.jlTituloMensagem.setText(msg);
        errorDlg.jtaDetalhes.setText(ex.getMessage());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        ex.printStackTrace(printStream);
        errorDlg.jtaStack.setText(new String(outputStream.toByteArray()));
        errorDlg.setVisible(true);       
    } 
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtaDetalhes = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        jlTituloMensagem = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtaStack = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Relatório de Erro do Sistema");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("DETALHES:");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/dialogs/icon5.png"))); // NOI18N
        jButton1.setText("Fechar");
        jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton1.setIconTextGap(16);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jtaDetalhes.setEditable(false);
        jtaDetalhes.setBackground(new java.awt.Color(240, 240, 240));
        jtaDetalhes.setColumns(20);
        jtaDetalhes.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jtaDetalhes.setLineWrap(true);
        jtaDetalhes.setRows(5);
        jtaDetalhes.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jtaDetalhes);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/dialogs/icon1.png"))); // NOI18N

        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jlTituloMensagem.setEditable(false);
        jlTituloMensagem.setBackground(new java.awt.Color(240, 240, 240));
        jlTituloMensagem.setColumns(2);
        jlTituloMensagem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jlTituloMensagem.setForeground(new java.awt.Color(255, 0, 0));
        jlTituloMensagem.setLineWrap(true);
        jlTituloMensagem.setRows(1);
        jlTituloMensagem.setWrapStyleWord(true);
        jlTituloMensagem.setBorder(null);
        jScrollPane2.setViewportView(jlTituloMensagem);

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        jtaStack.setEditable(false);
        jtaStack.setBackground(new java.awt.Color(240, 240, 240));
        jtaStack.setColumns(20);
        jtaStack.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jtaStack.setRows(5);
        jScrollPane3.setViewportView(jtaStack);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addComponent(jScrollPane2)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        setSize(new java.awt.Dimension(779, 588));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea jlTituloMensagem;
    private javax.swing.JTextArea jtaDetalhes;
    private javax.swing.JTextArea jtaStack;
    // End of variables declaration//GEN-END:variables
}
