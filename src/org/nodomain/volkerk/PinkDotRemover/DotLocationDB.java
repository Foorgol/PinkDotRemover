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
    ArrayList<DotSet> dsLib;
    
    /**
     * Constructor. Reads non-recursively all dot set definition files (ending with .txt) from a directory
     * and initializes the database with this data
     * 
     * @param dotDataPath String with the name/path of the directory to read from
     */
    public DotLocationDB(String dotDataPath)
    {
        // initialize the list of all dot sets
        dsLib = new ArrayList<DotSet>();
        
        // the provided path must exist and point to a directory
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
    
    /**
     * Takes an array of File-instances and tries to read dot definitions from them.
     * Valid dot sets are stored in the library for later use.
     * 
     * @param files is an array of File-instances with are parse as dot set definitions
     */
    protected void parseDotDataFiles(File[] files)
    {
        // loop over all files in the provided array
        for (File f : files)
        {
            // the file handle must be valid and point to an existing, regular file
            if ((f == null) || (!(f.exists())) || (!(f.isFile()))) continue;
            
            // dot set definitions must end with .txt
            if (!(f.toString().endsWith(DOT_FILE_EXT))) continue;
            
            dbg("Found valid file ", f);
            
            // try to read all text lines in the file for subsequent processing
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
            
            // a temporary dot set for the dot set that's currently being read
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
                    // store the current dot set to the lib
                    if (newDots != null)
                    {
                        dbg("Storing dot set ", newDots.getCombinedName());
                        dsLib.add(newDots);
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
                
                // if its not a control line, it must be a coordinates line
                // which we can only handle if we have a dot set
                if (newDots == null) continue;
                
                // Split up CSV data and make sure the number of "columns" fits
                String[] val = line.split(",");
                if (val.length != 5) continue;
                
                // prepare csv-values for parsing as integers
                String x0 = val[0].trim();
                String dy0 = val[1].trim();
                String dy1 = val[2].trim();
                String stepX = val[3].trim();
                String stepY = val[4].trim();
                
                // append the dot definition to the current dot set
                // parsing the Strings as ints might throw exceptions which
                // we gracefully ignore....
                newDots.addCoordinates(Integer.parseInt(x0), Integer.parseInt(dy0),
                        Integer.parseInt(dy1),
                        Integer.parseInt(stepX), Integer.parseInt(stepY));
                
            }
            
            logPop("Done");
            
            // store a possibly open dot set
            if (newDots != null)
            {
                dbg("Storing dot set ", newDots.getCombinedName());
                dsLib.add(newDots);
            }
                    
        }
        
    }

    /**
     * Takes a control line from a dot set definition file, parses the line and
     * creates/initializes a new dot set from the parameters
     * 
     * @param controlLine is the controle line from the dot set definition file
     * 
     * @return the newline created dot set instance or null in case of errors
     */
    protected DotSet initDotSetFromText(String controlLine)
    {
        // No further error checking here.
        // We assume to be called by parseDotDataFiles which provides a valid
        // controlLine
        
        // split the control line
        String[] val = controlLine.split(DELIM);
        
        // need three values: token (new / ref), cam name, resolution
        if (val.length < 3)
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
        
        // get the resolution and check if its valid
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
        
        boolean roundDown = false;
        if (val.length > 3) {
            roundDown = val[3].trim().equals("round_down");
        }
        
        // initialize a new dot set with the parameters
        DotSet newDotSet = new DotSet(cam, res[0], res[1], roundDown);
        
        return newDotSet;
    }
    
    /**
     * Converts a string with comma separated integers and converts it into an int array
     * 
     * NOTE: "empty" values are not permitted and will throw an exception!
     * Example: 1,2,,4 is invalid!
     * 
     * @param csv the string with the integers
     * 
     * @return an int array
     */
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
    
    /**
     * Retrieves a list of all distinct camera models in the database
     * 
     * @return a String array with the model names
     */
    public String[] getAllModels()
    {
        ArrayList<String> allCams = new ArrayList<String>();
        
        for (DotSet ds : dsLib)
        {
            String mod = ds.getCamType();
            if (!(allCams.contains(mod))) allCams.add(mod);
        }
        
        String[] dummy = new String[1];  // only necessary to convert the ArrayList to String[]
        
        return allCams.toArray(dummy);
    }
    
    /**
     * Dumps a list of all stored resolutions and camera models to stderr
     */
    public void dumpInfo()
    {
        String nl = System.lineSeparator();
        
        String out = "------------ Dot Lib Info ------------" + nl + nl;
        
        for (DotSet d : dsLib)
        {
            out += "  " + d.getCombinedName() + nl;
        }
        
        out += nl + "List of all cam types: ";
        for (String cam : getAllModels()) out += cam + ", ";
        out += nl;
        
        System.err.println(out);
    }
    
    /**
     * Returns a list of all pink dot coordinates for a given camera and image resolution.
     * 
     * If a dot set is explicitly defined for the requested resolution, the specific dot set
     * is used for calculate all dots. Otherwise, the dot locations are interpolated from the
     * default dot set for the cam.
     * 
     * @param model is the camera model for which the dot locations shall be retrieved
     * @param w is the width of the RAW image in pixels (outer dimensions; ignore ActiveArea etc.)
     * @param h is the height of the RAW image in pixels (outer dimensions; ignore ActiveArea etc.)
     * 
     * @return an array of all [x,y] dot locations or null in case of errors
     */
    public int[][] getAllDots(String model, int w, int h)
    {
        // try to get a specific dot set for this resolution
        preLog(LVL_DEBUG, "Trying to find specific dot set for ", model, " and ", w, "x", h);
        DotSet ds = null;
        for (DotSet tmp : dsLib)
        {
            if (tmp.isSet(model, w, h))
            {
                ds = tmp;
                resultLog(LOG_OK);
            }
        }
        
        // no match. Try to get a generic dot set for this model
        if (ds == null)
        {
            resultLog(LOG_FAIL);
            preLog(LVL_DEBUG, "Trying to find generic dot set for ", model);
            for (DotSet tmp : dsLib)
            {
                if (tmp.isSet(model, 0, 0)) ds = tmp;
            }
            
            // still no match. So we don't have any
            // usable dot data for this cam
            if (ds == null)
            {
                resultLog(LOG_FAIL);
                failed("Unable to find dot set for ", model, " and ", w, "x", h);
                return null;
            }
            
            resultLog(LOG_OK);
        }
        
        return ds.getAllCoordinates(w, h);
    }
}
