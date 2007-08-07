/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/08/07 23:54:15 $
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
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Umsatz;


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
 * Revision 1.1  2007/08/07 23:54:15  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 **********************************************************************/
