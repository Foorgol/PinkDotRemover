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
import java.nio.file.Path;
import java.nio.file.Paths;
import org.nodomain.volkerk.LoggingLib.LoggingClass;
import org.nodomain.volkerk.SimpleTIFFlib.Generic_CFA_PixBuf;
import org.nodomain.volkerk.SimpleTIFFlib.ImageFileDirectory;
import org.nodomain.volkerk.SimpleTIFFlib.RawFileFrame;
import org.nodomain.volkerk.SimpleTIFFlib.RawImageSequenceHandler;
import org.nodomain.volkerk.SimpleTIFFlib.TIFFhandler;

/**
 * Removes the AF dots in Magic Lantern's raw video frames for the Canon 650D
 */
public class PinkDotRemover extends LoggingClass {
    
    /**
     * the DNG file to modify
     */
    protected String srcFileName;
    
    /**
     * the image handler for the input file
     */
    protected TIFFhandler srcDng;
    
    /**
     * A handle for the dot location database
     */
    DotLocationDB db;
    
    /**
     * The cam type to use
     */
    String camType;
    
    /**
     * an image handler for the output file -- will be initialized from the input file
     */
    protected TIFFhandler dstDng;
    
    /**
     * A handler for a RAW file with image sequences
     */
    protected RawImageSequenceHandler srcRaw;
    
    protected static final String DEFAULT_CAM_TYPE = "650D";
    
    /**
     * Constructor. Checks for a valid file name and tries to open the file
     * 
     * @param fName the name / path of the DNG or RAW file
     * @param db is the database with dot locations for all cams and resolutions
     * @param camType is the name of the camera type
     */
    public PinkDotRemover(String fName, DotLocationDB _db, String _camType)
    {
        preLog(LVL_DEBUG, "Trying to instanciate File for ", fName);
        File src = new File(fName);
        
        if (!(src.exists()))
        {
            resultLog(LOG_FAIL + ": got null pointer");
            throw new IllegalArgumentException("File " + fName + " does not exist!");
        }
        resultLog(LOG_OK);
        
        srcFileName = fName;
        
        db = _db;
        
        camType = DEFAULT_CAM_TYPE;
        if ((_camType != null) && (_camType.length() != 0)) camType = _camType;

        // instanciate the right file handler, depending on the file name
        if (srcFileName.toLowerCase().endsWith("dng"))
        {
            logPush("Instanciating TIFF handlers for ", fName);
            initFromDNG();
            logPop("Done");
        }
        else if (srcFileName.toLowerCase().endsWith("raw"))
        {
            logPush("Instanciating RAW handler for ", fName);
            initFromRAW();
            logPop("Done");            
        }
        else
        {
            resultLog(LOG_FAIL + ": not a valid RAW or DNG file");
            throw new IllegalArgumentException("File " + fName + " seems not to be a valid RAW or DNG file!");
        }
        
    }
    
    protected void initFromDNG()
    {
        srcRaw = null;
        
        try
        {
            
            logPush("Instanciating source TIFF handler with string arg");
            srcDng = new TIFFhandler(srcFileName);
            logPop("Done");
            
            logPush("Instanciating destination TIFF handler with string arg");
            dstDng = new TIFFhandler(srcFileName);
            logPop("Done");
        }
        catch (Exception e)
        {
            failed(e.getMessage());
            throw new IllegalArgumentException("Baaaaad file: " + e.getMessage());
        }
        
    }
    
    protected void initFromRAW()
    {
        srcDng = null;
        dstDng = null;
        
        logPush("Instanciating RAW file handler with string arg");
        srcRaw = new RawImageSequenceHandler(srcFileName);
        srcRaw.dumpInfo();
        logPop("Done");
    }
    
    /**
     * Removes the pink dots from the target file
     * 
     * @param doInterpolation if true, the interpolation algorithm is used; otherwise, the pixel is simply marked as "bad pixel"
     * @return true if the dots could be removed, false in case of errors
     */
    public boolean doRemoval(boolean doInterpolation)
    {
        int w;
        int h;
        
        // prepare access to the image data
        ImageFileDirectory ifdSrc = null;
        ImageFileDirectory ifdDst = null;
        
        if (srcRaw != null)
        {
            // okay, we're reading from a RAW file
            
            logPush("Retrieving CFA image dimensions from RAW file");
            w = srcRaw.getWidth();
            h = srcRaw.getHeight();
            logPop("Done");
        }
        else
        {
            // we're reading from a DNG file
            
            // we assume that the TIFF file contains exactly one RAW image...
            logPush("Retrieving CFA IFDs and image dimensions from DNG file");

            ifdSrc = srcDng.getFirstIFDwithCFA();
            if (ifdSrc == null) dbg("Got null for srcDng");

            ifdDst = dstDng.getFirstIFDwithCFA();
            if (ifdDst == null) dbg("Got null for dstDng");

            w = (int) ifdSrc.imgWidth();
            h = (int) ifdSrc.imgHeight();
            
            ifdSrc.dumpInfo();

            logPop("Done");
        }
        
        // Let's see if we have the dot pattern for this type of image
        int[][] dotList = db.getAllDots(camType, w, h);
        if (dotList == null)
        {
            failed("No dot pattern for image size " + w + "x" + h + " and cam type ", camType, " available!");
            return false;
        }
        dbg("Retrieved dot list for image!");
        
        if (srcRaw != null)
        {
            logPush("Starting dot removal in RAW file");
            for (int n=0; n < 1; n++)
            //for (int n=0; n < srcRaw.getFrameCount(); n++)
            {
                // get the n-th frame and remove the dots
                logPush("Retrieving frame ", n, " of ", srcRaw.getFrameCount() - 1, " from RAW file");
                RawFileFrame srcFr = srcRaw.getFrame(n);
                RawFileFrame dstFr = srcFr.getCopy();
                srcFr.dumpInfo();
                logPop("Done");
                logPush("Removing dots in frame");
                if (doInterpolation) interpolPixel(srcFr, dstFr, dotList);
                else markBadPixels(srcFr, dstFr, dotList);
                logPop("Done");
                                
                // write the n-th frame back to disk
                logPush("Writing frame ", n, " back to disk");
                srcRaw.writeFrameToFile(dstFr, n);
                logPop("Done");
            }
            logPop("Done");
        }
        else
        {
            logPush("Starting dot removal in DNG file");
            if (doInterpolation) interpolPixel(ifdSrc, ifdDst, dotList);
            else markBadPixels(ifdSrc, ifdDst, dotList);
            logPop("Done");
            
            // write the frame back to disk
            logPush("Writing image back to disk");
            writeResultsToTargetDNG();
            logPop("Done");
        }
        
        dbg("Conversion in memory completed!");
        
        return true;
    }
    
    
    /**
     * Replaces a pixel intensity with an interpolation of the "X"-like neighboring pixels
     * Pixels closer than 2 pixel to the image border can't be interpolated and remain unmodified.
     * 
     * @param srcBuf ImageFileHandler for the distorted source image data (read)
     * @param dstBuf ImageFileHandler for the improved image data (write)
     * @param dotList a list of x,y-coordinates of the dots to fix
     */
    protected void interpolPixel(Generic_CFA_PixBuf srcBuf, Generic_CFA_PixBuf dstBuf, int[][] dotList)
    {
        int w = (int) srcBuf.imgWidth();
        int h = (int) srcBuf.imgHeight();
        
        for (int[] dot : dotList)
        {
            int x = dot[0];
            int y = dot[1];
            
            // don't fix pixel on image borders
            if ((x < 2) || (x > (w - 3)) || (y < 2) || (y > (h - 4))) continue;
            
            // determine intensity gradients in all four directions
            int g1 = srcBuf.CFA_getPixel(x, y - 2) - srcBuf.CFA_getPixel(x, y + 2); // top-down
            int g2 = srcBuf.CFA_getPixel(x - 2, y) - srcBuf.CFA_getPixel(x + 2, y); // left-right
            int g3 = srcBuf.CFA_getPixel(x - 2, y - 2) - srcBuf.CFA_getPixel(x + 2, y + 2); // top-left, down-right
            int g4 = srcBuf.CFA_getPixel(x + 2, y - 2) - srcBuf.CFA_getPixel(x - 2, y + 2); // top-right, down-left
            
            // find the minimum gradient
            g1 = Math.abs(g1);
            g2 = Math.abs(g2);
            g3 = Math.abs(g3);
            g4 = Math.abs(g4);
            
            int minG = Math.min(g1, g2);
            minG = Math.min(minG, g3);
            minG = Math.min(minG, g4);
            
            // use the minimum gradient for interpolation
            double newVal;
            if (minG == g1)
            {
                newVal = (srcBuf.CFA_getPixel(x, y - 2) + srcBuf.CFA_getPixel(x, y + 2)) * 0.5;
            }
            else if (minG == g2)
            {
                newVal = (srcBuf.CFA_getPixel(x-2, y) + srcBuf.CFA_getPixel(x+2, y)) * 0.5;
            }
            else if (minG == g3)
            {
                newVal = (srcBuf.CFA_getPixel(x-2, y-2) + srcBuf.CFA_getPixel(x+2, y+2)) * 0.5;
            }
            else
            {
                newVal = (srcBuf.CFA_getPixel(x+2, y-2) + srcBuf.CFA_getPixel(x-2, y+2)) * 0.5;
            }


            dstBuf.CFA_setPixel(x, y, (int) newVal);
        }
        
    }
    
    /**
     * Replaces a pixel intensity with a 0 to indicate a bad pixel
     * Leaves the actual interpolation to the RAW processor later on
     * 
     * @param srcBuf ImageFileHandler for the distorted source image data (read)
     * @param dstBuf ImageFileHandler for the improved image data (write)
     * @param dotList a list of x,y-coordinates of the dots to fix
     */
    protected void markBadPixels(Generic_CFA_PixBuf srcBuf, Generic_CFA_PixBuf dstBuf, int[][] dotList)
    {
        int w = (int) srcBuf.imgWidth();
        int h = (int) srcBuf.imgHeight();
        
        for (int[] dot : dotList)
        {
            //just set pixels to 0 that are in the image
            if ((dot[0] < 0) || (dot[0] >= w) || (dot[1] < 0) || (dot[1] >= h)) continue;
            dstBuf.CFA_setPixel(dot[0], dot[1], 0);
        }
    
    }
    
    /**
     * Writes the contents of the destination image to a DNG file. The filename
     * is constructed from the original filename plus a leading underscore.
     * Existing files will be overwritten.
     * 
     * @return the name and (possibly) path of the destination file
     */
    protected String writeResultsToTargetDNG()
    {
        preLog(LVL_DEBUG, "Trying to instanciate Path for ", srcFileName);
        Path srcPath = Paths.get(srcFileName);
        if (srcPath == null) resultLog((LOG_FAIL));
        else resultLog(LOG_OK);
        
        String fName = srcPath.getFileName().toString();
        dbg("fName = ", fName);
        String pName = "";
        if (srcPath.getParent() != null) pName = srcPath.getParent().normalize().toString();
        dbg("pName = ", pName);
        
        preLog(LVL_DEBUG, "Trying to instanciate Path for destination file");
        Path dstPath = Paths.get(pName, "_" + fName);
        if (dstPath == null) resultLog((LOG_FAIL));
        else resultLog(LOG_OK);
                
        logPush("Calling dstDng.saveAs() with Path parameter ", dstPath);
        writeResultToFile(dstPath);
        logPop("Done");
        
        dbg("File saved successfully");
        
        return dstPath.toString();
    }
    
    
    /**
     * Writes the contents of the destination image to a DNG file.
     * Existing files will be overwritten.
     * 
     * @param dstFileName the name of the file to write to
     */
    protected void writeResultToFile(String dstFileName)
    {
        dstDng.saveAs(dstFileName);
    }
    
    /**
     * Writes the contents of the destination image to a DNG file.
     * Existing files will be overwritten.
     * 
     * @param dstFilePath the path of the file to write to
     */
    protected void writeResultToFile(Path dstFilePath)
    {
        dstDng.saveAs(dstFilePath);
    }
}
