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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.nodomain.volkerk.LoggingLib.LoggingClass;
import org.nodomain.volkerk.SimpleTIFFlib.ImageFileDirectory;
import org.nodomain.volkerk.SimpleTIFFlib.TIFFhandler;

/**
 * Reads, stores and maintains dot locations from various files for various
 * camera types and various resolutions
 */
public class DotLocationDB extends LoggingClass {
    
    protected static final String COMMENT_TOKEN = "#";
    protected static final String DELIM = "::";
    protected static final String NEW_SET_TOKEN = "N";
    protected static final String REF_SET_TOKEN = "R";
    protected static final String DOT_FILE_EXT = "txt";
    
    /**
     * All dot sets in this database
     */
    ArrayList<DotSet> ds;
    
    public DotLocationDB(String dotDataPath)
    {
        ds = new ArrayList<DotSet>();
        
        File pDotData = new File(dotDataPath);
        
        if ((pDotData == null) || (!(pDotData.exists())) || (!(pDotData.isDirectory())))
        {
            throw new IllegalArgumentException("Not a valid directory with dot data!");
        }
        
        dbg(dotDataPath, " is a valid path.");
        
        logPush("Start parsing all files in ", dotDataPath);
        parseDotDataFiles(pDotData.listFiles());
        logPop(("Done"));
        
    }
    
    protected void parseDotDataFiles(File[] files)
    {
        ArrayList<String> refLines = new ArrayList<String>();
        
        for (File f : files)
        {
            if ((f == null) || (!(f.exists())) || (!(f.isFile()))) continue;
            
            if (!(f.toString().endsWith(DOT_FILE_EXT))) continue;
            
            dbg("Found valid file ", f);
            
            List<String> allLines;
            
            try
            {
                allLines = Files.readAllLines(Paths.get(f.toString()), Charset.defaultCharset());
            }
            catch (Exception e)
            {
                failed("Could not read file ", f, " ; Reason: ", e.getMessage());
                continue;
            }
            
            DotSet newDots = null;
            
            logPush("Start parsing file ", f);
            
            for (String line : allLines)
            {
                // remove leading / trailing whitespaces
                line = line.trim();
                
                // skip empty lines
                if (line.length() == 0) continue;
                
                // skip comments
                if (line.startsWith(COMMENT_TOKEN)) continue;
                
                // is this a control line which starts a new dot set?
                if (line.contains(DELIM))
                {
                    // store the current dot set
                    if (newDots != null)
                    {
                        dbg("Storing dot set ", newDots.getCombinedName());
                        ds.add(newDots);
                        newDots = null;
                    }
                    
                    // start of a new dot set?
                    if (line.startsWith(NEW_SET_TOKEN))
                    {
                        logPush("Init new dot set from DNG as define in '" + line + "'");
                        newDots = initDotSetFromDNG(f.getParent(), line);
                        logPop("Done");
                        if (newDots == null)
                        {
                            failed("Could not handle control line '", line, "' in file ", f);
                            continue;
                        }
                    }
                    
                    // reference to an existing dot set?
                    if (line.startsWith(REF_SET_TOKEN))
                    {
                        // store for later. Resolve reference once all
                        // other lists and files have been processed
                        refLines.add(line);
                        continue;
                    }
                    
                    // control line parsing done, continue with next line
                    continue;
                }
                
                // if its not a control line, it must be a coordinate line
                // which we can only handle if we have a dot set
                if (newDots == null) continue;
                
                // Split up CSV data
                String[] val = line.split(",");
                if (val.length != 6) continue;
                
                String x0 = val[0].trim();
                String y0 = val[1].trim();
                String x1 = val[2].trim();
                String y1 = val[3].trim();
                String stepX = val[4].trim();
                String stepY = val[5].trim();
                
                newDots.addCoordinates(Integer.parseInt(x0), Integer.parseInt(y0),
                        Integer.parseInt(x1), Integer.parseInt(y1),
                        Integer.parseInt(stepX), Integer.parseInt(stepY));
                
            }
            
            logPop("Done");
            
            // store a possibly open dot set
            if (newDots != null)
            {
                dbg("Storing dot set ", newDots.getCombinedName());
                ds.add(newDots);
            }
                    
        }
        
        // resolve references, if any
        for (String line : refLines)
        {
            logPush("Trying to resolve reference: ", line);
            DotSet newDotSet = createDotSetFromReference(line);
            if (newDotSet == null)
            {
                failed("Could not resolve reference ", line);
            }
            else
            {
                ds.add(newDotSet);
            }
            logPop("Done");
            
        }
    }

    protected DotSet initDotSetFromDNG(String parentDir, String controlLine)
    {
        // No further error checking here.
        // We assume to be called by parseDotDataFiles which provides a valid
        // parentDir and an actual controlLine
        
        // split the control line
        String[] val = controlLine.split(DELIM);
        
        // need five values: token (new / ref), cam name, set name, dng file name, dng threshold
        if (val.length != 5)
        {
            failed("Control line '", controlLine, "' is invalid");
            return null;
        }
        
        // dng name is on position four
        String dngName = val[3].trim();
        
        // try to open the DNG with the TIFF lib
        TIFFhandler dng;
        try
        {
            dng = new TIFFhandler(Paths.get(parentDir, dngName));
        }
        catch (Exception e)
        {
            failed("Could not open DNG file ", dngName);
            return null;
        }
        
        // check if we have CFA data in the DNG
        ImageFileDirectory ifd = dng.getFirstIFDwithCFA();
        if (ifd == null)
        {
            failed(dngName, " does not contain valid CFA data!");
            return null;
        }
        
        // check if we can identify the camera model
        String cam = val[1].trim();
        if (cam.length() == 0)
        {
            failed("The control line does not contain the camera's name");
            return null;
        }
        
        // determine the threshold, which is in column 5
        int threshold = -1;
        try
        {
            threshold = Integer.parseInt(val[4].trim());
        }
        catch (Exception e)
        {
            failed("Invalid threshold in control line: ", val[4]);
            return null;
        }
        
        int bpp = ifd.bitsPerSample()[0];
        int maxPixVal = (1 << bpp) - 1;
        if ((threshold < 0) || (threshold > maxPixVal))
        {
            failed("Threshold ", threshold, " is out of range");
            return null;
        }
        
        // build the resolution string (e.g., "1280x720") and
        // initialize a new dot set
        int h = (int) ifd.imgLen();
        int w = (int) ifd.imgWidth();
        int aaX = (int) ifd.DNG_ActiveArea()[0];
        int aaY = (int) ifd.DNG_ActiveArea()[1];
        String res = w + "x" + h;
        DotSet newDotSet = new DotSet(cam, val[2].trim(), res, aaX, aaY);
        
        logPush("Start extracting dot locations from file");
        extractDotsFromCFAwithThreshold(newDotSet, ifd, threshold);
        logPop("Done");
        
        return newDotSet;
    }

    protected void extractDotsFromCFAwithThreshold(DotSet targetDs, ImageFileDirectory ifd, int threshold)
    {
        // No further error checking here.
        // We assume to be called by initDotSetFromDNG which provides a valid
        // ifd with CFA data and a valid threshold
        
        int h = (int) ifd.imgLen();
        int w = (int) ifd.imgWidth();
        // ifd.dumpInfo();
        
        // Loop over all pixel and apply threshold
        // Baaaaaad, bad hack: sometimes accessing the last pixel
        // throws an exception. Some we skip the last line...
        // ... until I found the bug
        int cnt=0;
        for (int y = 0; y < (h-1); y++)
        {
            for (int x = 0; x < w; x++)
            {
                if (ifd.CFA_getPixel(x, y) >= threshold)
                {
                    targetDs.addCoordinates(x, y);
                    cnt++;
                }
            }
        }
        dbg(cnt, " dots extracted");
    }
    
    protected DotSet createDotSetFromReference(String controlLine)
    {
        // split the control line
        String[] val = controlLine.split(DELIM);
        
        // need seven values: token (new / ref), new cam name, new set name, new resolution
        // source cam name, source set name, offset-pair
        if (val.length != 7)
        {
            failed("Control line '", controlLine, "' is invalid");
            return null;
        }
        
        // try to retrieve the source set
        String srcCam = val[4].trim();
        String srcSet = val[5].trim();
        DotSet src = getDotSetByModelAndName(srcCam, srcSet);
        if (src == null) return null;
        dbg("Found source dot set: ", src.getCombinedName());
        
        // create the target dot set
        String dstCam = val[1].trim();
        String dstSet = val[2].trim();
        int[] newRes = intArrayFromCSV(val[3]);
        String res = newRes[0] + "x" + newRes[1];
        DotSet dst = new DotSet(dstCam, dstSet, res, 0, 0);  // no aaX / aaY here, because src delivers the coodinates already with offset
        dbg("Created destination dot set: ", dst.getCombinedName());
        
        // get the offset
        int[] off = intArrayFromCSV(val[6]);
        
        // copy the data between the source and target
        // and apply the offset
        preLog(LVL_DEBUG, "Start copying dot locations");
        for (int[] coord : src.getAllCoordinates())
        {
            dst.addCoordinates(coord[0] + off[0], coord[1] + off[1]);
        }
        resultLog(LOG_OK);
        
        return dst;
        
    }
    
    protected int[] intArrayFromCSV(String csv)
    {
        // there are a lot of things that can go wrong here
        // e.g., invalid numbers, double commas, empty string, etc.
        //
        // For now, we silently ignore all this
        
        String[] val = csv.split(",");
        
        int[] result = new int[val.length];
        
        for (int i=0; i < val.length; i++) result[i] = Integer.parseInt(val[i].trim());
        
        return result;
    }
    
    public DotSet getDotSetByModelAndName(String modelName, String setName)
    {
        String name = modelName.trim() + "_" + setName.trim();
        
        for (DotSet d : ds)
        {
            if (d.isSet(modelName, setName)) return d;
        }
        
        return null;
    }
    
    public DotSet getDotSetByModelAndResolution(String modelName, int w, int h)
    {
        for (DotSet d : ds)
        {
            if (d.isSet(modelName, w, h)) return d;
        }
        
        return null;
    }
    
    public String[] getAllModels()
    {
        ArrayList<String> allCams = new ArrayList<String>();
        
        for (DotSet d : ds)
        {
            String mod = d.getCamType();
            if (!(allCams.contains(mod))) allCams.add(mod);
        }
        
        String[] dummy = new String[1];
        
        return allCams.toArray(dummy);
    }
    
    public void dumpInfo()
    {
        String nl = System.lineSeparator();
        
        String out = "------------ Dot Lib Info ------------" + nl + nl;
        
        for (DotSet d : ds)
        {
            out += "  " + d.getCombinedName() + nl;
        }
        
        out += nl + "List of all cam types: ";
        for (String cam : getAllModels()) out += cam + ", ";
        out += nl;
        
        System.err.println(out);
    }
}
