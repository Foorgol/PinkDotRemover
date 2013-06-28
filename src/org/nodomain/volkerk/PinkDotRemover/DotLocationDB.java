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

/**
 * Reads, stores and maintains dot locations from various files for various
 * camera types and various resolutions
 */
public class DotLocationDB extends LoggingClass {
    
    protected static final String COMMENT_TOKEN = "#";
    protected static final String DELIM = "::";
    protected static final String NEW_SET_TOKEN = "N";
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
                        logPush("Init new dot set with '" + line + "'");
                        newDots = initDotSetFromText(line);
                        logPop("Done");
                        if (newDots == null)
                        {
                            failed("Could not handle control line '", line, "' in file ", f);
                            continue;
                        }
                    }
                    
                    // control line parsing done, continue with next line
                    continue;
                }
                
                // if its not a control line, it must be a coordinate line
                // which we can only handle if we have a dot set
                if (newDots == null) continue;
                
                // Split up CSV data
                String[] val = line.split(",");
                if (val.length != 5) continue;
                
                String x0 = val[0].trim();
                String dy0 = val[1].trim();
                String dy1 = val[2].trim();
                String stepX = val[3].trim();
                String stepY = val[4].trim();
                
                newDots.addCoordinates(Integer.parseInt(x0), Integer.parseInt(dy0),
                        Integer.parseInt(dy1),
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
        
    }

    protected DotSet initDotSetFromText(String controlLine)
    {
        // No further error checking here.
        // We assume to be called by parseDotDataFiles which provides a valid
        // controlLine
        
        // split the control line
        String[] val = controlLine.split(DELIM);
        
        // need three values: token (new / ref), cam name, resolution
        if (val.length != 3)
        {
            failed("Control line '", controlLine, "' is invalid");
            return null;
        }
        
        // check if we can identify the camera model
        String cam = val[1].trim();
        if (cam.length() == 0)
        {
            failed("The control line does not contain the camera's name");
            return null;
        }
        
        // get the resolution
        String csvRes = val[2].trim();
        int[] res;
        try
        {
            res = intArrayFromCSV(csvRes);
        }
        catch (Exception e)
        {
            failed("Could not parse image resolutionin control line '", controlLine, "'");
            return null;
        }
        if (res.length != 2)
        {
            failed("Could not parse image resolutionin control line '", controlLine, "'");
            return null;
        }
        
        // build the resolution string (e.g., "1280x720") and
        // initialize a new dot set
        DotSet newDotSet = new DotSet(cam, res[0], res[1]);
        
        return newDotSet;
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
    
    public int[][] getAllDots(String model, int w, int h)
    {
        // try to get a specific dot set for this resolution
        preLog(LVL_DEBUG, "Trying to find specific dot set for ", model, " and ", w, "x", h);
        DotSet d = null;
        for (DotSet tmp : ds)
        {
            if (tmp.isSet(model, w, h))
            {
                d = tmp;
                resultLog(LOG_OK);
            }
        }
        
        // no match. Try to get a generic dot set for this model
        if (d == null)
        {
            resultLog(LOG_FAIL);
            preLog(LVL_DEBUG, "Trying to find generic dot set for ", model);
            for (DotSet tmp : ds)
            {
                if (tmp.isSet(model, 0, 0)) d = tmp;
            }
            
            // still no match. So we don't have any
            // usable dot data for this cam
            if (d == null)
            {
                resultLog(LOG_FAIL);
                failed("Unable to find dot set for ", model, " and ", w, "x", h);
                return null;
            }
            
            resultLog(LOG_OK);
        }
        
        return d.getAllCoordinates(w, h);
    }
}
