/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/BackupRestore.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/01/22 13:34:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.serialize.ObjectFactory;
import de.willuhn.datasource.serialize.Reader;
import de.willuhn.datasource.serialize.XmlReader;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 */
public class BackupRestore implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    final ClassLoader loader = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getClassLoader();

    Reader reader = null;
    try
    {
      InputStream is = new BufferedInputStream(new FileInputStream("/tmp/install/backup.xml"));
      reader = new XmlReader(is,new ObjectFactory() {
      
        public GenericObject create(String type, String id, Map values) throws Exception
        {
          AbstractDBObject object = (AbstractDBObject) Settings.getDBService().createObject(loader.loadClass(type),null);
          object.setID(id);
          Iterator i = values.keySet().iterator();
          while (i.hasNext())
          {
            String name = (String) i.next();
            object.setAttribute(name,values.get(name));
          }
          return object;
        }
      
      });
      GenericObject o = null;
      while ((o = reader.read()) != null)
      {
        ((AbstractDBObject)o).insert();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new ApplicationException(e.getMessage());
    }
    finally
    {
      if (reader != null)
      {
        try
        {
          reader.close();
          Logger.info("Backup fertig");
        }
        catch (Exception e) {/*useless*/}
      }
    }
  }
}


/*********************************************************************
 * $Log: BackupRestore.java,v $
 * Revision 1.1  2008/01/22 13:34:45  willuhn
 * @N Neuer XML-Import/-Export
 *
 **********************************************************************/