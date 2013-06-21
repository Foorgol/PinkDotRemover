/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nodomain.volkerk.PinkDotRemover;

import java.io.File;
import javax.swing.JFileChooser;
import java.util.*;
import javax.swing.JOptionPane;

/**
 *
 * @author volker
 */
public class MainFrame extends javax.swing.JFrame {

    protected final JFileChooser fChooser = new JFileChooser();
    ArrayList<File> fList;
    protected RemoverWorker remWorker;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        fList = new ArrayList<File>();
        remWorker = null;
        
        updateList();
        updateButtons();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        radioGrpMethod = new javax.swing.ButtonGroup();
        btnClear = new javax.swing.JButton();
        btnConvert = new javax.swing.JButton();
        btnQuit = new javax.swing.JButton();
        progBar = new javax.swing.JProgressBar();
        btnAddFile = new javax.swing.JButton();
        btnAddDir = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        radioBadPix = new javax.swing.JRadioButton();
        radioInterpolate = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        teFiles = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(602, 400));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        btnClear.setText("Clear List");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 60, 5);
        getContentPane().add(btnClear, gridBagConstraints);

        btnConvert.setText("Convert Files");
        btnConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvertActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        getContentPane().add(btnConvert, gridBagConstraints);

        btnQuit.setText("Quit");
        btnQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(btnQuit, gridBagConstraints);

        progBar.setName(""); // NOI18N
        progBar.setString("");
        progBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(13, 5, 5, 5);
        getContentPane().add(progBar, gridBagConstraints);

        btnAddFile.setText("Add Files");
        btnAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(btnAddFile, gridBagConstraints);

        btnAddDir.setText("Add Directory");
        btnAddDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 5, 8, 5);
        getContentPane().add(btnAddDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(filler1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(filler2, gridBagConstraints);

        radioGrpMethod.add(radioBadPix);
        radioBadPix.setText("Set Dead Pixel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(radioBadPix, gridBagConstraints);

        radioGrpMethod.add(radioInterpolate);
        radioInterpolate.setSelected(true);
        radioInterpolate.setText("Interpolate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(radioInterpolate, gridBagConstraints);

        teFiles.setEditable(false);
        teFiles.setColumns(20);
        teFiles.setRows(5);
        teFiles.setFocusable(false);
        jScrollPane1.setViewportView(teFiles);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btnQuitActionPerformed

    private void btnAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFileActionPerformed
        doAddFilesViaDlg();
    }//GEN-LAST:event_btnAddFileActionPerformed

    private void btnAddDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDirActionPerformed
        doAddDirViaDlg();
    }//GEN-LAST:event_btnAddDirActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        doClearList();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvertActionPerformed
        doConversion();
    }//GEN-LAST:event_btnConvertActionPerformed

    protected void doClearList()
    {
        fList.clear();
        updateList();
        updateButtons();
    }
    
    protected void doAddFilesViaDlg()
    {
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setMultiSelectionEnabled(true);
        int result = fChooser.showOpenDialog(this);
        
        if (result != JFileChooser.APPROVE_OPTION) return;
        
        for (File f : fChooser.getSelectedFiles())
        {
            if (fList.contains(f)) continue;
            fList.add(f);
        }
        
        updateList();
        updateButtons();
    }
    
    protected void doAddDirViaDlg()
    {
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fChooser.setMultiSelectionEnabled(false);
        int result = fChooser.showOpenDialog(this);
        
        if (result != JFileChooser.APPROVE_OPTION) return;
        
        File selDir = fChooser.getSelectedFile();
        
        for (File f : selDir.listFiles())
        {
            if (fList.contains(f)) continue;
            if (!(f.isFile())) continue;
            fList.add(f);
        }
        
        updateList();
        updateButtons();
    }
    
    protected void doConversion()
    {
        progBar.setMaximum(fList.size());
        remWorker = new RemoverWorker(this, fList);
        remWorker.execute();
    }
    
    protected void updateList()
    {
        String txt = "";
        for (File f : fList) txt += f.toString() + System.lineSeparator();
        System.err.println(txt);
        
        teFiles.setText(txt);
        teFiles.setCaretPosition(0);
    }
    
    protected void updateButtons()
    {
        // set a basic state (everything disabled) as long as the conversion is running
        boolean basicState = true;
        if ((remWorker != null) && (!(remWorker.isDone()))) basicState = false;
        btnConvert.setEnabled(basicState);
        btnAddDir.setEnabled(basicState);
        btnAddFile.setEnabled(basicState);
        btnClear.setEnabled(basicState);
        radioBadPix.setEnabled(basicState);
        radioInterpolate.setEnabled(basicState);
        if (!basicState) return;
        
        // individual per-button decisions
        if (fList.size() == 0)
        {
            btnConvert.setEnabled(false);
            btnClear.setEnabled(false);
        }
        else
        {
            btnConvert.setEnabled(true);
            btnClear.setEnabled(true);
        }
        
    }
    
    public void globalUpdateHook(ArrayList<File> newList, int filesProcessed)
    {
        fList = newList;
        updateButtons();
        updateList();
        
        if (filesProcessed >= 0)
        {
            progBar.setValue(filesProcessed);
            progBar.setString(filesProcessed + " of " + progBar.getMaximum() + " files processed");
        }
        else
        {
            progBar.setValue(0);
            progBar.setString("");
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDir;
    private javax.swing.JButton btnAddFile;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnConvert;
    private javax.swing.JButton btnQuit;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JProgressBar progBar;
    private javax.swing.JRadioButton radioBadPix;
    private javax.swing.ButtonGroup radioGrpMethod;
    private javax.swing.JRadioButton radioInterpolate;
    private javax.swing.JTextArea teFiles;
    // End of variables declaration//GEN-END:variables
}
