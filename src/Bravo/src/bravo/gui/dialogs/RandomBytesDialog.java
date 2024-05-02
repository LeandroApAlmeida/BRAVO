package bravo.gui.dialogs;

import dialogs.JOptionPaneEx;
import java.util.LinkedList;

public class RandomBytesDialog extends javax.swing.JDialog {
    
    
    private final int DIVISION = 16;
    
    private final int MIN_BYTES = 256;
    
    private final LinkedList<Byte> randomBytes;
    
    private final int sizeX;
    
    private final int sizeY;

    private int coordX;
    
    private int coordY;

    
    public RandomBytesDialog(java.awt.Frame parent) {
        
        super(parent, true);
        
        randomBytes = new LinkedList<>();
        
        initComponents();
        
        setLocationRelativeTo(parent);
        
        sizeX = jpRandom.getWidth() / DIVISION;
        sizeY = jpRandom.getHeight() / DIVISION;
        
        jpbMinimum.setMinimum(0);
        jpbMinimum.setMaximum(MIN_BYTES);
        jpbMinimum.setValue(0);
        
    }

    
    private void setPanelCoordinate(int x, int y) {

        int newXCoord = (x / sizeX <= 15 ? x / sizeX : 15);
        
        int newYCoord = (y / sizeY <= 15 ? y / sizeY : 15);
        
        boolean changed = (coordX != newXCoord) || (coordY != newYCoord);
        
        if (changed) {
            
            coordX = newXCoord;
            coordY = newYCoord;
            
            int unsignedByte = ((coordY * DIVISION) + coordX);
            
            byte twosComplementByte = (byte) unsignedByte;
            
            randomBytes.add(twosComplementByte);
            
            if (randomBytes.size() <= MIN_BYTES) {
            
                jpbMinimum.setValue(jpbMinimum.getValue() + 1);
            
            }
            
            jlNumOfBytes.setText(String.valueOf(randomBytes.size()) + " bytes");
            
            jlMessage.setText(
                "[X = " + String.valueOf(coordX) + ", " +
                "Y = " + String.valueOf(coordY) + "]"
            );
            
        }
        
    }
    
    
    private void createNewFile() {
        if (randomBytes.size() >= MIN_BYTES) {
            setVisible(false);
        } else {
            JOptionPaneEx.showMessageDialog(
                this,
                "Continue movimentando o mouse até completar a barra de progresso.",
                "Atenção",
                JOptionPaneEx.WARNING_MESSAGE
            );
        }
    }
    
    
    public byte[] getRandomBytesGenerated() {
        byte[] random = new byte[randomBytes.size()];
        System.arraycopy(random, 0, random, 0, randomBytes.size());
        return random;
    }
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpRandom = new javax.swing.JPanel();
        jlMessage = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jbCreateNewFile = new javax.swing.JButton();
        jpbMinimum = new javax.swing.JProgressBar();
        jlNumOfBytes = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("CRIAR UM NOVO ARQUIVO");

        jpRandom.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jpRandom.setMaximumSize(new java.awt.Dimension(480, 480));
        jpRandom.setMinimumSize(new java.awt.Dimension(394, 394));
        jpRandom.setPreferredSize(new java.awt.Dimension(394, 394));
        jpRandom.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jpRandomMouseMoved(evt);
            }
        });

        jlMessage.setForeground(new java.awt.Color(153, 153, 153));
        jlMessage.setText("[]");

        jLabel1.setForeground(new java.awt.Color(153, 153, 153));
        jLabel1.setText("Movimente o mouse nesta área, de forma aleatória, no minímo até completar a barra de progresso. Feito isso,");

        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        jLabel2.setText("clique no botão \"Criar Arquivo\" para que o novo arquivo seja gerado.");

        javax.swing.GroupLayout jpRandomLayout = new javax.swing.GroupLayout(jpRandom);
        jpRandom.setLayout(jpRandomLayout);
        jpRandomLayout.setHorizontalGroup(
            jpRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpRandomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jlMessage)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addContainerGap(54, Short.MAX_VALUE))
        );
        jpRandomLayout.setVerticalGroup(
            jpRandomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpRandomLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 443, Short.MAX_VALUE)
                .addComponent(jlMessage)
                .addContainerGap())
        );

        jbCreateNewFile.setText("Criar Arquivo");
        jbCreateNewFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbCreateNewFileActionPerformed(evt);
            }
        });

        jlNumOfBytes.setText("0 bytes");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jpbMinimum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jlNumOfBytes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jbCreateNewFile, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jpRandom, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE))
                .addGap(5, 5, 5))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jpRandom, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(jpbMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlNumOfBytes)
                    .addComponent(jbCreateNewFile))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jpRandomMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jpRandomMouseMoved
        setPanelCoordinate(evt.getX(), evt.getY());
    }//GEN-LAST:event_jpRandomMouseMoved

    private void jbCreateNewFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbCreateNewFileActionPerformed
        createNewFile();
    }//GEN-LAST:event_jbCreateNewFileActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton jbCreateNewFile;
    private javax.swing.JLabel jlMessage;
    private javax.swing.JLabel jlNumOfBytes;
    private javax.swing.JPanel jpRandom;
    private javax.swing.JProgressBar jpbMinimum;
    // End of variables declaration//GEN-END:variables
}
