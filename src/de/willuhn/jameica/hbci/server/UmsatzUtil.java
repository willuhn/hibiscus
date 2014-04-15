/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzUtil.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/08/05 11:21:59 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Hilfsklasse zum Verarbeiten von Umsaetzen.
 * 
 */
public class UmsatzUtil
{
  /**
   * Liefert alle Umsaetze in chronologischer Reihenfolge (alte zuerst) sortiert nach Datum, ID.
   * Weitere Filter-Kriterien wie Zeitraum und Konto muessen noch hinzugefuegt werden.
   * Die Funktion sortiert lediglich vereinheitlicht.
   * @return sortierte Liste der Umsaetze.
   * @throws RemoteException
   */
  public static DBIterator getUmsaetze() throws RemoteException
  {
    return getUmsaetze(false);
  }

  /**
   * Liefert alle Umsaetze in umgekehrt chronologischer Reihenfolge (neue zuerst) sortiert nach Datum, ID.
   * Weitere Filter-Kriterien wie Zeitraum und Konto muessen noch hinzugefuegt werden.
   * Die Funktion sortiert lediglich vereinheitlicht.
   * @return sortierte Liste der Umsaetze.
   * @throws RemoteException
   */
  public static DBIterator getUmsaetzeBackwards() throws RemoteException
  {
    return getUmsaetze(true);
  }
  
  /**
   * Liefert alle Umsaetze in ugekehrt chronologischer Reihenfolge (neue zuerst), in denen
   * der genannte Suchbegriff auftaucht.
   * @param query Suchbegriff.
   * @return Liste der gefundenen Umsaetze.
   * @throws RemoteException
   * @throws ApplicationException wird geworfen, wenn kein Suchbegriff angegeben ist.
   */
  public static DBIterator find(String query) throws RemoteException, ApplicationException
  {
    if (query == null || query.length() == 0)
    {
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      throw new ApplicationException(i18n.tr("Bitte geben Sie einen Suchbegriff an"));
    }

    String text = "%" + query.toLowerCase() + "%";
    DBIterator list = getUmsaetzeBackwards();
    list.addFilter("LOWER(CONCAT(COALESCE(zweck,''),COALESCE(zweck2,''),COALESCE(zweck3,''))) LIKE ? OR " +
                   "LOWER(empfaenger_name) LIKE ? OR " +
                   "empfaenger_konto LIKE ? OR " +
                   "empfaenger_blz LIKE ? OR " +
                   "LOWER(primanota) LIKE ? OR " +
                   "LOWER(art) LIKE ? OR " +
                   "LOWER(customerref) LIKE ? OR " +
                   "LOWER(kommentar) LIKE ?",
                   text,text,text,text,text,text,text,text);
    return list;
  }

  /**
   * Liefert alle Umsaetze, jedoch mit vereinheitlichter Vorsortierung.
   * @param backwards chronologisch (alte zuerst) = true.
   * umgekehrt chronologisch (neue zuerst) = false.
   * @return sortierte Liste der Umsaetze.
   * @throws RemoteException
   */
  private static DBIterator getUmsaetze(boolean backwards) throws RemoteException
  {
    String s = backwards ? "DESC" : "ASC";
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    DBIterator list = service.createList(Umsatz.class);
    list.setOrder("ORDER BY " + service.getSQLTimestamp("datum") + " " + s + ", id " + s);
    return list;
  }
}


/**********************************************************************
 * $Log: UmsatzUtil.java,v $
 * Revision 1.3  2011/08/05 11:21:59  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 * Revision 1.2  2010-09-10 11:57:24  willuhn
 * @C Allgemeine Suche nach Umsaetzen anhand Suchbegriff in UmsatzUtil verschoben - kann dort besser wiederverwendet werden
 *
 * Revision 1.1  2007/08/07 23:54:15  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 **********************************************************************/
