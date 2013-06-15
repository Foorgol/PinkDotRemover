/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nodomain.volkerk.PinkDotRemover;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nodomain.volkerk.SimpleTIFFlib.ImageFileDirectory;
import org.nodomain.volkerk.SimpleTIFFlib.TIFFhandler;
import org.nodomain.volkerk.SimpleTIFFlib.TIFFhandler;

/**
 *
 * @author volker
 */
public class PinkDotRemover {
    
    /**
     * the file to modify
     */
    protected String srcFileName;
    
    /**
     * the handler for the input file
     */
    TIFFhandler srcDng;
    
    /**
     * a handler for the output file -- will be initialized as the input file
     */
    TIFFhandler dstDng;
    
    /**
     * Constructor. Checks for a valid file name and tries to open the file
     * 
     * @param fName the name / path of the DNG file
     */
    public PinkDotRemover(String fName)
    {
        File src = new File(fName);
        
        if (!(src.exists()))
        {
            throw new IllegalArgumentException("File " + fName + " does not exist!");
        }
        
        try
        {
            srcDng = new TIFFhandler(fName);
            dstDng = new TIFFhandler(fName);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Baaaaad file: " + e.getMessage());
        }
        
        srcFileName = fName;
        
   }
    
    public boolean doRemovalInMemory(boolean useEmpiricPattern)
    {
        ImageFileDirectory ifdSrc = srcDng.getFirstIFDwithCFA();
        ImageFileDirectory ifdDst = dstDng.getFirstIFDwithCFA();
        int w = (int) ifdSrc.imgWidth();
        int h = (int) ifdSrc.imgLen();
        
        ifdSrc.dumpInfo();
        
        ArrayList<int[]> dotList = new ArrayList<>();
        
        dotList = getEmpiricDotPattern(w, h);
        if (dotList == null)
        {
            System.err.println("No empiric dot pattern for images " + w + "x" + h + " available!");
            return false;
        }
        
        int[][] dotPattern = getDotPattern(w, h);
        if (dotPattern == null)
        {
            System.err.println("No dot pattern for images " + w + "x" + h + " available!");
            return false;
        }

        for (int[] dots : dotPattern)
        {
            int minX = dots[0];
            int minY = dots[1];
            int maxX = dots[2];
            int maxY = dots[3];
            int stepX = dots[4];
            int stepY = dots[5];

            for (int y = minY; y <= maxY; y += stepY)
            {
                for (int x = minX; x <= maxX; x += stepX)
                {
                    dotList.add(new int[] {x, y});
                }
            }
        }
        
        double fac1 = 1.0;
        double fac2 = 0;
        
        for (int[] dot : dotList)
        {
            interpolPixel(ifdSrc, ifdDst, dot[0], dot[1], fac1);
            //interpolPixel(ifdSrc, ifdDst, x-2, y, fac2);
            //interpolPixel(ifdSrc, ifdDst, x+2, y, fac2);
            //interpolPixel(ifdSrc, ifdDst, x, y-2, fac2);
            //interpolPixel(ifdSrc, ifdDst, x, y+2, fac2);
            //interpolPixel(ifdSrc, ifdDst, x-2, y-2, fac2);
            //interpolPixel(ifdSrc, ifdDst, x+2, y+2, fac2);
            //interpolPixel(ifdSrc, ifdDst, x+2, y-2, fac2);
            //interpolPixel(ifdSrc, ifdDst, x-2, y+2, fac2);
        }
        
        return true;
    }
    
    protected void interpolPixel(ImageFileDirectory ifdSrc, ImageFileDirectory ifdDst, int x, int y, double weight)
    {
        if ((x < 2) || (x > (ifdSrc.imgWidth() - 3)) || (y < 2) || (y > (ifdSrc.imgLen() - 3))) return;
        
        double fac1 = 0.0;
        double fac2 = 0.0;
        double fac3 = 0.25;

        // calc a new pixel value from the neighbors of the dot;
        // no range checking here; I assume there's always a neighbor...
        double newVal = 0;
        
        // direct neighbors
        newVal += fac1 * ifdSrc.CFA_getPixel(x - 2, y);
        newVal += fac1 * ifdSrc.CFA_getPixel(x + 2, y);
        newVal += fac1 * ifdSrc.CFA_getPixel(x, y + 2);
        newVal += fac1 * ifdSrc.CFA_getPixel(x, y - 2);

        newVal += fac3 * ifdSrc.CFA_getPixel(x - 2, y - 2);
        newVal += fac3 * ifdSrc.CFA_getPixel(x + 2, y - 2);
        newVal += fac3 * ifdSrc.CFA_getPixel(x - 2, y + 2);
        newVal += fac3 * ifdSrc.CFA_getPixel(x + 2, y + 2);

        // indirect neighbors
        //newVal += fac2 * ifdSrc.CFA_getPixel(x - 4, y - 4);
        //newVal += fac2 * ifdSrc.CFA_getPixel(x + 4, y + 4);
        //newVal += fac2 * ifdSrc.CFA_getPixel(x - 4, y + 4);
        //newVal += fac2 * ifdSrc.CFA_getPixel(x + 4, y - 4);
        
        newVal = (1.0 - weight) * ((double) ifdSrc.CFA_getPixel(x, y)) + weight * newVal;

        ifdDst.CFA_setPixel(x, y, (int) newVal);
        
    }
    
    public String writeResultToFile()
    {
        Path srcPath = Paths.get(srcFileName);
        
        String fName = srcPath.getFileName().toString();
        String pName = srcPath.getParent().normalize().toString();
        
        Path dstPath = Paths.get(pName, "_" + fName);
                
        dstDng.saveAs(dstPath);
        dstDng.getFirstIFDwithCFA().CFA_primitiveDemosaic("/tmp/pdr.png");
        //dstDng.getFirstIFDwithCFA().CFA_raw2png("/tmp/rawTest_after.png", false);
        
        return dstPath.toString();
    }
    
    protected int[][] getDotPattern(int w, int h)
    {
        int[][] result = null;
        
        if ((w == 1280) && (h == 720))
        {
            result = new int[][] {
                {511, 213, 767, 263, 8, 10},
                {507, 219, 763, 269, 8, 10},
                {504, 234, 760, 304, 8, 10},
                {504, 334, 760, 404, 8, 10},
                {508, 338, 764, 408, 8, 10},
                {511, 413, 767, 463, 8, 10},
                {507, 419, 763, 469, 8, 10},
                {504, 434, 760, 504, 8, 10},
                {508, 238, 764, 308, 8, 10},
                {312, 274, 496, 284, 8, 10},
                {316, 278, 500, 288, 8, 10},
                {768, 274, 952, 284, 8, 10},
                {768, 284, 956, 288, 8, 10},
                {319, 313, 359, 323, 8, 10},
                {315, 319, 955, 329, 8, 10},
                {511, 333, 767, 363, 8, 10},
                {507, 339, 763, 369, 8, 10},
                
                {312, 354, 496, 364, 8, 10},
                {316, 358, 500, 368, 8, 10},
                {312, 394, 496, 404, 8, 10},
                {316, 398, 500, 408, 8, 10},
                {312, 434, 496, 444, 8, 10},
                {316, 438, 500, 448, 8, 10},
                {319, 433, 503, 443, 8, 10},
                {315, 439, 499, 449, 8, 10},
                
                {312+456, 354, 496+456, 364, 8, 10},
                {316+456, 358, 500+456, 368, 8, 10},
                {312+456, 394, 496+456, 404, 8, 10},
                {316+456, 398, 500+456, 408, 8, 10},
                {312+456, 434, 496+456, 444, 8, 10},
                {316+456, 438, 500+456, 448, 8, 10},
                {319+456, 433, 503+456, 443, 8, 10},
                {315+456, 439, 499+456, 449, 8, 10},
                {316+456, 278, 500+456, 288, 8, 10},
                
                {508, 438, 764, 508, 8, 10},
                {367, 313, 959, 323, 8, 10},
                {775, 353, 959, 363, 8, 10},
                {771, 359, 955, 369, 8, 10},
                
                {327, 273, 903, 283, 8, 10},
                {315, 279, 939, 289, 8, 10},
                
                {511, 293, 767, 303, 8, 10},
                {507, 299, 763, 309, 8, 10},
                
                {319, 393, 959, 403, 8, 10},
                {315, 399, 955, 409, 8, 10},
                
                {511, 493, 767, 503, 8, 10},
                {507, 499, 763, 509, 8, 10},
                
                {319, 353, 503, 363, 8, 10},
                {315, 359, 499, 369, 8, 10},
                
                {512, 214, 760, 224, 8, 10},
                {516, 218, 764, 228, 8, 10},
                
                {312, 314, 952, 324, 8, 10},
                {308, 318, 948, 328, 8, 10},
                
                {496, 414, 744, 424, 8, 10},
                {516, 418, 764, 428, 8, 10}
            };
        
        }
            
        return result;
    }
    
    protected ArrayList<int[]> getEmpiricDotPattern(int w, int h)
    {
        ArrayList<int[]> result = null;
        
        if ((w == 1280) && (h == 720))
        {
            
            Charset charset = Charset.forName("US-ASCII");
            
            try
            {
                InputStream in = this.getClass().getResourceAsStream("res/pixCoord_threshold2068.txt");
                InputStreamReader ir = new InputStreamReader(in);
                
                BufferedReader b  = new BufferedReader(ir);
                                
                result = new ArrayList<>();
                String line;
                while ((line = b.readLine()) != null)
                {
                    int x = Integer.parseInt(line.split(",")[0].trim());
                    int y = Integer.parseInt(line.split(",")[1].trim());
                    result.add(new int[] {x, y});
                }
            }
            catch (Exception e)
            {
                System.err.println("Something went terribly wrong while reading empiric dot pattern data: " + e.getMessage());
            }
        }
            
        return result;
    }
    
}
