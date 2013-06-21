/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nodomain.volkerk.PinkDotRemover;

import java.io.File;
import javax.swing.SwingWorker;
import java.util.*;
import javax.swing.JOptionPane;

/**
 *
 * @author volker
 */
public class RemoverWorker extends SwingWorker<ArrayList<File>, Integer>
{
    protected ArrayList<File> fList;
    protected MainFrame parent;
    
    public RemoverWorker(MainFrame _parent, ArrayList<File> _fList)
    {
        fList = _fList;
        parent = _parent;
    }
    
    @Override
    protected ArrayList<File> doInBackground()
    {
        int processedFiles = 0;
        
        int i=0;
        while (i < fList.size())
        {
            File f = fList.get(i);
            
            // try to convert the file
            PinkDotRemover pdr;
            try
            {
                pdr = new PinkDotRemover(f.toString());
                if (!(pdr.doRemovalInMemory()))
                {
                    i++;
                }
                else
                {
                    pdr.writeResultToFile();
                    fList.remove(i);
                }
            }
            catch (Exception e)
            {
                i++;
            }
            
            processedFiles++;
            publish(processedFiles);
        }
        
        return fList;
    }
    
    @Override
    protected void process(List<Integer> count)
    {
        for (int i : count) parent.globalUpdateHook(fList, i);
    }
    
    @Override
    protected void done()
    {
        parent.globalUpdateHook(fList, -1);
        if (fList.size() != 0)
        {
            JOptionPane.showMessageDialog(null, "One or more files could not be converted", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
