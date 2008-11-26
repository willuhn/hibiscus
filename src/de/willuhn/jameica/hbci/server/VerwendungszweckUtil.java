/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/VerwendungszweckUtil.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/11/26 00:39:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Verwendungszweck;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse zum Laden und Speichern der erweiterten Verwendungszwecke.
 */
public class VerwendungszweckUtil
{
  /**
   * Liefert eine Liste der gefundenen Verwendungszwecke fuer diesen Transfer.
   * @param t der Transfer.
   * @return Liste der gefundenen Verwendungszwecke.
   * Sortiert nach ID oder ein leeres Array, wenn keine erweiterten Verwendungszwecke vorliegen.
   * Die Funktion liefert NIE NULL sondern hoechstens ein leeres Array.
   * @throws RemoteException
   */
  public static String[] get(Transfer t) throws RemoteException
  {
    if (!(t instanceof DBObject))
      return new String[0];
    
    DBObject g = (DBObject) t;
    if (g.isNewObject())
      return new String[0];
    
    DBIterator list = Settings.getDBService().createList(Verwendungszweck.class);
    list.addFilter("typ = " + t.getTransferTyp());
    list.addFilter("auftrag_id = " + g.getID());
    list.setOrder("order by id");
    ArrayList l = new ArrayList();
    while (list.hasNext())
    {
      Verwendungszweck z = (Verwendungszweck) list.next();
      l.add(z.getText());
    }
    return (String[]) l.toArray(new String[l.size()]);
  }
  
  /**
   * Speichert die erweiterten Verwendungszwecke fuer einen Auftrag in der Datenbank.
   * @param t der Transfer.
   * @param list Liste der Verwendungszwecke.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static void store(Transfer t, String[] list) throws RemoteException, ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    if (t == null)
      throw new ApplicationException(i18n.tr("Kein Auftrag angegeben"));

    if (!(t instanceof DBObject))
      throw new ApplicationException(i18n.tr("Auftrag unterstützt keine erweiterten Verwendungszwecke"));
    
    DBObject g = (DBObject) t;
    if (g.isNewObject())
      throw new ApplicationException(i18n.tr("Bitte speichern Sie zuerst den Auftrag"));

    try
    {
      g.transactionBegin();

      // Wir loeschen die existierenden erst komplett weg
      delete(t);
      
      // und schreiben sie dann komplett neu
      for (int i=0;i<list.length;++i)
      {
        // leere Zeilen ueberspringen
        if (list[i] == null)
          continue;
        String text = list[i].trim();
        if (text.length() == 0)
          continue;
        
        Verwendungszweck zweck = (Verwendungszweck) Settings.getDBService().createObject(Verwendungszweck.class,null);
        zweck.setTransfer(t);
        zweck.setText(text);
        zweck.store();
      }
      
      // Transaktion committen
      g.transactionCommit();
    }
    catch (RemoteException re)
    {
      try
      {
        g.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw re;
    }
    catch (ApplicationException ae)
    {
      try
      {
        g.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw ae;
    }
  }
  
  /**
   * Loescht die erweiterten Verwendungszwecke fuer diesen Auftrag in der Datenbank.
   * @param t der Transfer.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static void delete(Transfer t) throws RemoteException, ApplicationException
  {
    if (t == null)
      return;

    if (!(t instanceof DBObject))
      return;
    
    DBObject g = (DBObject) t;
    if (g.isNewObject())
      return;

    try
    {
      g.transactionBegin();

      DBIterator it = Settings.getDBService().createList(Verwendungszweck.class);
      it.addFilter("typ = " + t.getTransferTyp());
      it.addFilter("auftrag_id = " + g.getID());
      while (it.hasNext())
      {
        Verwendungszweck z = (Verwendungszweck) it.next();
        z.delete();
      }
      // Transaktion committen
      g.transactionCommit();
    }
    catch (RemoteException re)
    {
      try
      {
        g.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw re;
    }
    catch (ApplicationException ae)
    {
      try
      {
        g.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw ae;
    }
  }

}


/*********************************************************************
 * $Log: VerwendungszweckUtil.java,v $
 * Revision 1.4  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.3  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.2  2008/02/22 00:52:35  willuhn
 * @N Erste Dialoge fuer erweiterte Verwendungszwecke (noch auskommentiert)
 *
 * Revision 1.1  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 **********************************************************************/