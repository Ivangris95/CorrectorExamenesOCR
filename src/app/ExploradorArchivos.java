/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package app;

import javax.swing.JFileChooser;

/**
 *
 * @author Ivan Gris
 */

public class ExploradorArchivos extends javax.swing.JPanel {

    
    public ExploradorArchivos() {
        initComponents();
        
        
    }
    
    public JFileChooser getFileChooser() {
        return jFileChooser2;
    }

   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser2 = new javax.swing.JFileChooser();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jFileChooser2.setCurrentDirectory(new java.io.File("C:\\"));
            add(jFileChooser2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 700, 320));
        }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser jFileChooser2;
    // End of variables declaration//GEN-END:variables

}
