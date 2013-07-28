/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nodomain.volkerk.PinkDotRemover;

import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;
import org.nodomain.volkerk.SimpleTIFFlib.ImageFileDirectory;
import org.nodomain.volkerk.SimpleTIFFlib.TIFFhandler;

/**
 *
 * @author volker
 */
public class PinkDotRemoverTest extends TstBaseClass {
    
    @Test
    public void testConversion() throws IOException
    {
        String[] fList = new String[] {
            "1280x720_14bit_le_650D.dng",
            "1344x572_14bit_le_650D.dng",
            "1344x756_14bit_le_650D.dng",
            "1472x626_14bit_le_650D.dng",
            "1472x828_14bit_le_650D.dng",
            "1600x680_14bit_le_650D.dng",
            "1600x900_14bit_le_650D.dng",
            "1728x736_14bit_le_650D.dng",
            "1728x972_14bit_le_650D.dng",
            "1808x1190_14bit_le_650D.dng",
            "1808x727_14bit_le_650D.dng"
        };
        
        System.err.println(Paths.get(projRootDir(), "dotData").toString());
        DotLocationDB db = new DotLocationDB(Paths.get(projRootDir(), "dotData").toString());
        assertNotNull(db);
        
        suppressCleanup();
        for (String fname : fList)
        {
            String inFile = Paths.get(testInputDataDir(), fname).toString();
            String outFile = Paths.get(outDir(), fname).toString();
            String refFile = Paths.get(testInputDataDir(), "ref_" + fname).toString();
            
            System.err.println("Converting " + inFile);
            PinkDotRemover pdr = new PinkDotRemover(inFile, db, "650D");
            assertTrue(pdr.doRemoval(true));
            pdr.writeResultToFile(outFile);
            assertTrue(cmpFilesBinary(outFile, refFile));
        }
        cleanupOutDir();
    }
}