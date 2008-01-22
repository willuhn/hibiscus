/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/BackupCreate.java,v $
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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.serialize.Writer;
import de.willuhn.datasource.serialize.XmlWriter;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.server.DauerauftragImpl;
import de.willuhn.jameica.hbci.server.HibiscusAddressImpl;
import de.willuhn.jameica.hbci.server.KontoImpl;
import de.willuhn.jameica.hbci.server.LastschriftImpl;
import de.willuhn.jameica.hbci.server.NachrichtImpl;
import de.willuhn.jameica.hbci.server.ProtokollImpl;
import de.willuhn.jameica.hbci.server.SammelLastBuchungImpl;
import de.willuhn.jameica.hbci.server.SammelLastschriftImpl;
import de.willuhn.jameica.hbci.server.SammelUeberweisungBuchungImpl;
import de.willuhn.jameica.hbci.server.SammelUeberweisungImpl;
import de.willuhn.jameica.hbci.server.TurnusImpl;
import de.willuhn.jameica.hbci.server.UeberweisungImpl;
import de.willuhn.jameica.hbci.server.UmsatzImpl;
import de.willuhn.jameica.hbci.server.UmsatzTypImpl;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 */
public class BackupCreate implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Writer writer = null;
    try
    {
      writer = new XmlWriter(new BufferedOutputStream(new FileOutputStream("/tmp/install/backup.xml")));
      backup(TurnusImpl.class,writer);
      backup(UmsatzTypImpl.class,writer);


      backup(HibiscusAddressImpl.class,writer);

      backup(KontoImpl.class,writer);
      backup(NachrichtImpl.class,writer);
      backup(UmsatzImpl.class,writer);
      
      backup(DauerauftragImpl.class,writer);
      backup(LastschriftImpl.class,writer);
      backup(UeberweisungImpl.class,writer);

      backup(SammelLastschriftImpl.class,writer);
      backup(SammelLastBuchungImpl.class,writer);

      backup(SammelUeberweisungImpl.class,writer);
      backup(SammelUeberweisungBuchungImpl.class,writer);

      // Die Protokolle zum Schluss.
      backup(ProtokollImpl.class,writer);
    }
    catch (Exception e)
    {
      throw new ApplicationException(e.getMessage());
    }
    finally
    {
      if (writer != null)
      {
        try
        {
          writer.close();
          Logger.info("Backup fertig");
        }
        catch (Exception e) {/*useless*/}
      }
    }
  }
  
  private void backup(Class type, Writer writer) throws Exception
  {
    DBIterator list = Settings.getDBService().createList(type);
    while (list.hasNext())
      writer.write(list.next());
  }

}


/*********************************************************************
 * $Log: BackupCreate.java,v $
 * Revision 1.1  2008/01/22 13:34:45  willuhn
 * @N Neuer XML-Import/-Export
 *
 **********************************************************************/