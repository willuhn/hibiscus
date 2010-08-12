/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/util/SaldoFinder.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/08/12 17:12:32 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.util;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.TreeMap;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Hilfsklasse zum Finden eines Saldos zum angegebenen Zeitpunkt aus
 * einer vorgegebenen Liste von Umsaetzen.
 */
public class SaldoFinder
{
  private TreeMap<Date,Double> map = new TreeMap<Date,Double>();
  
  /**
   * ct.
   * @param umsaetze Liste der Umsaetze, in denen gesucht werden soll.
   * @throws RemoteException
   */
  public SaldoFinder(GenericIterator umsaetze) throws RemoteException
  {
    // Wir fuellen die Map
    while (umsaetze.hasNext())
    {
      Umsatz u = (Umsatz) umsaetze.next();
      this.map.put(u.getValuta(),u.getSaldo());
    }
  }
  
  /**
   * Liefert den Saldo zum angegebenen Zeitpunkt.
   * @param date das Datum.
   * @return der Saldo zu diesem Zeitpunkt.
   */
  public Double get(Date date)
  {
    if (date == null)
      return 0.0d;
    Date key = HBCIProperties.startOfDay(date);
    
    // Checken, ob wir fuer genau diesen Tag einen Saldo haben
    Double d = this.map.get(key);
    if (d != null)
      return d;
    
    // Haben wir einen Saldo zu einem frueheren Zeitpunkt?
    Date lower = this.map.lowerKey(date);
    if (lower != null)
      return this.map.get(lower);
    
    // Ne, wir haben auch keinen frueheren Saldo. Also war
    // er zu diesem Zeitpunkt noch 0.
    return 0.0d;
  }
}



/**********************************************************************
 * $Log: SaldoFinder.java,v $
 * Revision 1.1  2010/08/12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 **********************************************************************/