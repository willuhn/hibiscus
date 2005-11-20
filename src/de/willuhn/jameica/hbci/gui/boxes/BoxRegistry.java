/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Attic/BoxRegistry.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/11/20 23:39:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.util.Collections;
import java.util.Vector;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Diese Klasse uebernimmt die Verwaltung der Boxen.
 */
public class BoxRegistry
{
  private static Box[] list = null;
  
  /**
   * Liefert eine Liste der verfuegbaren Boxen.
   * Die Liste enthaelt auch deaktivierte Boxen.
   * @return Liste der verfuegbaren Boxen.
   */
  public static synchronized Box[] getBoxes()
  {
    if (list != null)
      return list;
    
    ClassFinder finder = Application.getClassLoader().getClassFinder();

    Vector v = new Vector();
    Class[] boxes = new Class[0];

    try
    {
      boxes = finder.findImplementors(Box.class);
    }
    catch (ClassNotFoundException ce)
    {
      Logger.error("no boxes found",ce);
    }

    Box current = null;
    for (int i=0;i<boxes.length;++i)
    {
      try
      {
        current = (Box) boxes[i].newInstance();
        v.add(current);
      }
      catch (Exception e)
      {
        Logger.error("unable to load box " + boxes[i].getName() + ", skipping");
      }
    }
    Collections.sort(v);
    list = (Box[]) v.toArray(new Box[v.size()]);
    return list;
  }
  
  /**
   * Schiebt die Box einen Index nach unten.
   * @param box die zu verschiebende Box.
   */
  public static void up(Box box)
  {
    int index = box.getIndex();
      
    if (index <= 0)
      return; // Die Box ist schon ganz oben.
    
    // Wir nehmen die Box oben drueber und tauschen die Positionen
    int newIndex = index - 1;
    Box other = list[newIndex];
    
    other.setIndex(index);
    box.setIndex(newIndex);
    
    list[index]    = other;
    list[newIndex] = box;
  }
  
  /**
   * Schiebt die Box einen Index nach oben.
   * @param box die zu verschiebende Box.
   */
  public static void down(Box box)
  {
    int index = box.getIndex();
    
    if (index >= (list.length - 1))
      return; // Die Box ist schon ganz unten

    // Wir nehmen die Box unten drunter und tauschen die Positionen
    int newIndex = index + 1;
    Box other = list[newIndex];
    
    other.setIndex(index);
    box.setIndex(newIndex);
    
    list[index]    = other;
    list[newIndex] = box;
  }
}


/*********************************************************************
 * $Log: BoxRegistry.java,v $
 * Revision 1.1  2005/11/20 23:39:11  willuhn
 * @N box handling
 *
 **********************************************************************/