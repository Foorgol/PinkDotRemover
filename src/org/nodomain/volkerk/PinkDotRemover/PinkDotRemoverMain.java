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

/**
 * The main class for the application. Parses the command line, instanciates
 * the remover class and triggers the removal
 */
public class PinkDotRemoverMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // retrieve all files denoted by command line arguments
        ArrayList<File> fList = collectFiles(args);
        
        // we need at least one file
        if (fList.size() == 0)
        {
            printHelp();
            return;
        }
        
        // loop over all files and convert them one by one
        int cnt = 1;
        for (File f : fList)
        {
            System.err.print("Processing file " + cnt + " / " + fList.size() + ": " + f.toString() + " ... ");
            
            PinkDotRemover pdr;
            String dstPath;
            try
            {
                pdr = new PinkDotRemover(f.toString());

                if (!(pdr.doRemovalInMemory()))
                {
                    System.err.println("Pink dot removal failed, no data written, program stopped");
                    return;
                }

                dstPath = pdr.writeResultToFile();
            }
            catch (Exception e)
            {
                System.err.println("failed!");
                System.err.println("Something went terribly wrong: " + e.getMessage());
                return;
            }
            
            System.err.println("done" + System.lineSeparator());
            cnt++;
        }
        
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
        ArrayList<File> fileList = new ArrayList<>();
        
        for (String s : args)
        {
            File f = new File(s);
            
            if (!(f.exists())) continue;
            
            if (f.isDirectory())
            {
                for (File subDirFile : f.listFiles())
                {
                    if (subDirFile.isFile()) fileList.add(subDirFile);
                }
            }
            
            if (f.isFile()) fileList.add(f);
        }
        
        // clean-up: allow only files ending in "dng" or "DNG"
        ArrayList<File> result = new ArrayList<>();
        for (File f : fileList)
        {
            String fname = f.getName();
            if (fname.endsWith("dng") || (fname.endsWith("DNG"))) result.add(f);
        }
        
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
}
