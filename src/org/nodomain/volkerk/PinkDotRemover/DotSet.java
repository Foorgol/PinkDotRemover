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
     * The RAW image width associated with this dot set
     */
    int width;
    
    /**
     * The RAW image height associated with this dot set
     */
    int height;
    
    /**
     * A list for all the "grid-like" dot information
     */
    ArrayList<int[]> gridInfo;

    /**
     * Constructor. Stores cam data and initializes a default dot set (w=0, h=0)
     * 
     * @param _camType string containing the camera type / name
     */
    public DotSet(String _camType)
    {
        this(_camType, 0, 0);
    }
    
    /**
     * Constructor. Stores cam data and resolution info and initializes (empty) dot lists
     * 
     * @param _camType string containing the camera type / name
     * @param _w is the RAW width (ignore ActiveArea) of the image for this dot set (zero for default dot set)
     * @param _h is the RAW height (ignore ActiveArea) of the image for this dot set (zero for default dot set)
     */
    public DotSet(String _camType, int _w, int _h)
    {
        camType = _camType;
        height = _h;
        width = _w;
        
        gridInfo = new ArrayList<int[]>();
    }
        
    /**
     * Add a "grid block" of dots to the dot set
     * 
     * @param x0 the x-coordinate of the upper left dot in the grid block
     * @param dy0 the offset of the first dot row, relative to image center in RAW coordinates (ignore ActiveArea)
     * @param dy1 the offset of the last dot row, relative to image center in RAW coordinates (ignore ActiveArea)
     * @param stepX the x-offset between to grid dots
     * @param stepY the y-offset between to grid dots
     */
    public void addCoordinates(int x0, int dy0, int dy1, int stepX, int stepY)
    {
        gridInfo.add(new int[]{x0, dy0, dy1, stepX, stepY});
    }
    
    /**
     * Provides a list of all dot locations covered by this dot set. Expands
     * grid dots to single coordinates.
     * 
     * @param w the image width in RAW coordinates (ignore ActiveArea) for generic dot sets (w=0, h=0)
     * @param h the image height in RAW coordinates (ignore ActiveArea) for generic dot sets (w=0, h=0)
     * 
     * @return an array of x,y-pairs in RAW coordinates
     */
    public int[][] getAllCoordinates(int w, int h)
    {
        ArrayList<int[]> result = new ArrayList<int[]>();
        
        // if we are a specific dot set, use "our" coordinates
        // and ignore the parameters
        if ((width != 0) && (height != 0))
        {
            w = width;
            h = height;
        }
        
        // calculate the image x- and y-center; round up if necessary
        int cx;
        if ((w % 2) != 0) cx = (w + 1) / 2;
        else cx = w / 2;

        int cy;
        if ((h % 2) != 0) cy = (h + 1) / 2;
        else cy = h / 2;

        // Add a x,y-locations specified by grid parameters
        for (int[] gi : gridInfo)
        {
            int x0 = gi[0];
            int dy0 = gi[1];
            int dy1 = gi[2];
            int stepX = gi[3];
            int stepY = gi[4];
            
            for (int y = cy + dy0; y <= cy + dy1; y += stepY)
            {
                for (int x = (cx + x0) % stepX ; x <= w; x += stepX)
                {
                    result.add(new int[]{x, y});
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
        return width + "x" + height;
    }
    
    public boolean isSet(String cam, int w, int h)
    {
        return ((camType.equals(cam)) && (width == w) && (height == h));
    }
    
    public String getCombinedName()
    {
        return camType + "_" + getRes();
    }
}
