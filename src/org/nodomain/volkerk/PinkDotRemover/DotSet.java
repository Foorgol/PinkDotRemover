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

import java.util.ArrayList;

/**
 * A container for dot locations for a specific camera type and a specific resolution
 */
public class DotSet {
    
    /**
     * The camera type this DotSet is valid for
     */
    String camType;
    
    /**
     * A string lke "1280x720" indicating the DNG resolution for this dot set
     */
    String res;
    
    /**
     * An individual name for this dot set, user defined
     * Should be unique
     */
    String setName;
    
    /**
     * A list for all the "grid-like" dot information
     */
    ArrayList<int[]> gridInfo;
    
    /**
     * ActiveArea offset x
     * Necessary to map grid-coordinates (picked from screen) to file coordinates
     */
    int activeAreaOffsetX;
    
    /**
     * ActiveArea offset y
     * Necessary to map grid-coordinates (picked from screen) to file coordinates
     */
    int activeAreaOffsetY;
    
    /**
     * A list for all the empirically determined dot locations
     */
    ArrayList<int[]> empiricDots;
    
    /**
     * Constructor. Stores cam data and resolution info and initializes (empty) dot lists
     * 
     * @param _camType string containing the camera type / name
     * @param _setName string containing the name of the dot set
     * @param _res string like "1280x720" indicating the resolution
     * @param aaX active area offset x
     * @param aaY active area offset y
     */
    public DotSet(String _camType, String _setName, String _res, int aaX, int aaY)
    {
        camType = _camType;
        res = _res;
        setName = _setName;
        activeAreaOffsetX = aaX;
        activeAreaOffsetY = aaY;
        
        gridInfo = new ArrayList<int[]>();
        empiricDots = new ArrayList<int[]>();
    }
    
    
    /**
     * Adds the coordinate of a single, specific dot to the dot set
     * 
     * @param x the 0-based x-coordinate of the dot
     * @param y the 0-based y-coordinate of the dot
     */
    public void addCoordinates(int x, int y)
    {
        empiricDots.add(new int[]{x, y});
    }
    
    /**
     * Add a "grid block" of dots to the dot set
     * 
     * @param x0 the x-coordinate of the upper left dot in the grid block
     * @param y0 the y-coordinate of the upper left dot in the grid block
     * @param x1 the x-coordinate of the lower right dot in the grid block
     * @param y1 the y-coordinate of the lower right dot in the grid block
     * @param stepX the x-offset between to grid dots
     * @param stepY the y-offset between to grid dots
     */
    public void addCoordinates(int x0, int y0, int x1, int y1, int stepX, int stepY)
    {
        gridInfo.add(new int[]{x0, y0, x1, y1, stepX, stepY});
    }
    
    /**
     * Provides a list of all dot locations covered by this dot set. Expands
     * grid dots to single coordinates.
     * 
     * @return an array of x,y-pairs
     */
    public int[][] getAllCoordinates()
    {
        // start with a copy of the empiric dots
        ArrayList<int[]> result = (ArrayList<int[]>) empiricDots.clone();
        
        // Add a x,y-locations specified by grid parameters
        for (int[] gi : gridInfo)
        {
            for (int y = gi[1]; y <= gi[3]; y += gi[5])
            {
                for (int x = gi[0]; x <= gi[2]; x += gi[4])
                {
                    result.add(new int[]{x + activeAreaOffsetX, y + activeAreaOffsetY});
                }
            }
        }
        
        // return all dot locations as an array of x,y-pairs
        int[][] dummyArray = new int[1][1];
        return result.toArray(dummyArray);
    }
    
    public String getCamType()
    {
        return camType;
    }
    
    public String getRes()
    {
        return res;
    }
    
    public String getSetName()
    {
        return setName;
    }
    
    public boolean isSet(String cam, String set)
    {
        return ((camType.equals(cam)) && (setName.equals(set)));
    }
        
    public boolean isSet(String cam, int w, int h)
    {
        String resQuery = w + "x" + h;
        return ((camType.equals(cam)) && (res.equals(resQuery)));
    }
    
    public String getCombinedName()
    {
        return camType + "_" + setName + "_" + res;
    }
}
