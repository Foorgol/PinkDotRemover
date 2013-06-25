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

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author volker
 */
public class LocationDatabaseInitWorker extends SwingWorker<DotLocationDB, Integer>
{
    /**
     * The path to the dot data files
     */
    String dbPath;
    
    /**
     * Handle to the main window for "communication" via function calls
     */
    MainFrame parent;
    
    /**
     * The final db instance
     */
    DotLocationDB db;
    
    /**
     * Constructor. Stores the path to the dot data files
     * 
     * @param _parent handle to the parent window's instance for "communication"
     * @param dbPath string containing the path name for the dot data files
     */
    public LocationDatabaseInitWorker(MainFrame _parent, String _dbPath)
    {
        dbPath = _dbPath;
        parent = _parent;
        db = null;
    }
    
    @Override
    public DotLocationDB doInBackground()
    {
        DotLocationDB result = null;
        db = null;
        
        try
        {
            result = new DotLocationDB(dbPath);
        }
        catch (Exception e)
        {
            return null;
        }
        
        db = result;
        return result;
    }
    
    @Override
    protected void process(List<Integer> dummy)
    {
        // no need for this function
    }
    
    @Override
    protected void done()
    {
        if (db == null)
        {
            JOptionPane.showMessageDialog(parent, "Error while reading the database! File conversion impossible!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        parent.dbInitDone();
    }
    
}
