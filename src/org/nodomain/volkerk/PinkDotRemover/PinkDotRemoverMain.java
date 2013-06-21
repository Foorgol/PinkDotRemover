/*
 * Copyright © 2013 Volker Knollmann
 * 
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file or visit
 * http://www.wtfpl.net/ for more details.
 * 
 * This program comes without any warranty. Use it at your own risk or
 * don't use it at all.
 */

package org.nodomain.volkerk.PinkDotRemover;

import java.io.File;
import java.util.*;
import org.nodomain.volkerk.LoggingLib.LoggingClass;

/**
 * The main class for the application. Parses the command line, instanciates
 * the remover class and triggers the removal
 */
public class PinkDotRemoverMain extends LoggingClass {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // set the log level
        logLvl = LVL_FAIL;  // bad hack
        
        dbg("Command line args: " + strCat(args));
        logPush("Trying to resolve dirs and valid files");
        
        // retrieve all files denoted by command line arguments
        ArrayList<File> fList = collectFiles(args);
        logPop("Done");
        
        // we need at least one file
        if (fList.size() == 0)
        {
            //dbg("No valid files found!");
            //printHelp();
            doGUI();
            return;
        }
        
        dbg("At least one valid file found for conversion.");
        
        // loop over all files and convert them one by one
        logPush("Looping over all found files for conversion");
        int cnt = 1;
        for (File f : fList)
        {
            logPush("Processing file ", cnt, " / ", fList.size(), ": ", f);
            
            PinkDotRemover pdr;
            String dstPath;
            try
            {
                logPush("Instanciating dot remover class for ", f);
                pdr = new PinkDotRemover(f.toString());
                logPop("Done");

                logPush("Starting dot removal for ", f);
                if (!(pdr.doRemovalInMemory(true)))
                {
                    failed("Pink dot removal failed, no data written, program stopped");
                    logPop("Aborted");
                    return;
                }
                logPop("Done");

                logPush("Start writing results");
                dstPath = pdr.writeResultToFile();
                logPop("Done");
            }
            catch (Exception e)
            {
                failed("Exception in main(): ", e.getMessage());
                logPop("Aborted");
                return;
            }
            
            logPop("Done");
            cnt++;
        }
        logPop("Done");
        dbg("main() end.");
        
    }
    
    /**
     * Takes a list of strings (e. g. command line args) and checks one by one
     * if they point to existing files or directories. In case of directories,
     * the file in this directory are considered as well (non-recursively).
     * 
     * The function will return a list of all existing files with extension "dng" or "DNG"
     * 
     * @param args the of strings to check
     * 
     * @return a list of File-objects for existing files ending in dng or DNG
     */
    protected static ArrayList<File> collectFiles(String[] args)
    {
        ArrayList<File> fileList = new ArrayList();
        
        logPush("Looping over command line arguments");
        for (String s : args)
        {
            preLog(LVL_DEBUG, "Trying to instanciate File for ", s);
            File f = new File(s);
            resultLog(LOG_OK);
            
            if (!(f.exists())) continue;
            dbg(s, " exists");
            
            if (f.isDirectory())
            {
                logPush(s, " is a directory, diving into it");
                for (File subDirFile : f.listFiles())
                {
                    dbg("Found file ", subDirFile.toString());
                    if (subDirFile.isFile()) fileList.add(subDirFile);
                }
                logPop("Done");
            }
            
            if (f.isFile())
            {
                dbg(s, " is a file");
                fileList.add(f);
            }
        }
        logPop("Done");
        dbg(fileList.size(), " files found");
        
        // clean-up: allow only files ending in "dng" or "DNG"
        ArrayList<File> result = new ArrayList();
        logPush("Checking if found files are valid");
        for (File f : fileList)
        {
            String fname = f.getName();
            if (fname.endsWith("dng") || (fname.endsWith("DNG")))
            {
                dbg(f, " is okay");
                result.add(f);
            }
            else dbg(f, " is NOT okay");
        }
        logPop("Done");
        
        return result;
    }
    
    /**
     * Prints a short help message to stderr
     */
    protected static void printHelp()
    {
        System.err.println();
        System.err.println("Usage:");
        System.err.println("java -jar PinkDotRemover.jar <file1.dng or dir1> <file2.dng or dir2> etc.");
        System.err.println();
    }
    
    protected static void doGUI()
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });

    }
}
