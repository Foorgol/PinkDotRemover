/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nodomain.volkerk.PinkDotRemover;

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
        // we need exactly one argument: the image file name
        if (args.length != 1)
        {
            printHelp();
            return;
        }
        
        // try to convert the image
        PinkDotRemover pdr;
        String dstPath;
        try
        {
            pdr = new PinkDotRemover(args[0]);

            if (!(pdr.doRemovalInMemory()))
            {
                System.err.println("Pink dot removal failed, no data written...");
                return;
            }
            
            dstPath = pdr.writeResultToFile();
        }
        catch (Exception e)
        {
            System.err.println("Something went terribly wrong: " + e.getMessage());
            return;
        }
        
        System.err.println("Good news: pink dots removed and clean image written to " + dstPath);
        
    }
    
    protected static void printHelp()
    {
        System.err.println("Usage: java -jar PinkDotRemover.jar <file.dng>");
    }
}
