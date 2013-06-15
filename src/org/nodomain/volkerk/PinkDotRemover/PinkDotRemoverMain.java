/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nodomain.volkerk.PinkDotRemover;

/**
 *
 * @author volker
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

            if (!(pdr.doRemovalInMemory(true)))
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
