/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.util;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.jameica.util.DateUtil;

/**
 * Hilfsklasse zum Finden eines Saldos zum angegebenen Zeitpunkt aus
 * einer vorgegebenen Liste von Umsaetzen/Salden.
 */
public class SaldoFinder
{
  private TreeMap<Date,Double> map = new TreeMap<Date,Double>();
  private double anfangssaldo = 0.0d;
  
  /**
   * ct.
   * @param umsaetze Liste der Umsaetze, in denen gesucht werden soll.
   * @param anfangssaldo der initiale Saldo, den das Konto vorher hatte.
   * Darf "0.00" sein, wenn er nicht bekannt ist.
   * @throws RemoteException
   */
  public SaldoFinder(GenericIterator umsaetze, double anfangssaldo) throws RemoteException
  {
    this.anfangssaldo = anfangssaldo;
    
    // Wir fuellen die Map
    while (umsaetze.hasNext())
    {
      Umsatz u = (Umsatz) umsaetze.next();
      // Vormerkbuchungen werden nicht beruecksichtigt, weil sie keinen Saldo haben
      if (u.hasFlag(Umsatz.FLAG_NOTBOOKED))
        continue;
      this.map.put(u.getDatum(),u.getSaldo());
    }
  }
  
  /**
   * ct.
   * @param values Liste der Salden, in denen gesucht werden soll.
   * @param anfangssaldo der initiale Saldo, den das Konto vorher hatte.
   * Darf "0.00" sein, wenn er nicht bekannt ist.
   * @throws RemoteException
   */
  public SaldoFinder(Collection<Value> values, double anfangssaldo) throws RemoteException
  {
    this.anfangssaldo = anfangssaldo;
    
    for (Value v:values)
    {
      this.map.put(v.getDate(),v.getValue());
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
      return anfangssaldo;
    Date key = DateUtil.startOfDay(date);
    
    // Checken, ob wir fuer genau diesen Tag einen Saldo haben
    Double d = this.map.get(key);
    if (d != null)
      return d;
    
    // Haben wir einen Saldo zu einem frueheren Zeitpunkt?
    Date lower = this.map.lowerKey(date); // JAVA 1.6
    if (lower != null)
      return this.map.get(lower);
    
    // Ne, wir haben auch keinen frueheren Saldo. Also war
    // er zu diesem Zeitpunkt noch 0 bzw der Anfangssaldo.
    return anfangssaldo;
  }
}



/**********************************************************************
 * $Log: SaldoFinder.java,v $
 * Revision 1.7  2012/04/05 21:44:18  willuhn
 * @B BUGZILLA 1219
 *
 * Revision 1.6  2011/10/27 17:09:29  willuhn
 * @C Saldo-Bean in neue separate (und generischere) Klasse "Value" ausgelagert.
 * @N Saldo-Finder erweitert, damit der jetzt auch mit Value-Objekten arbeiten kann
 *
 * Revision 1.5  2011-05-02 14:43:41  willuhn
 * @B BUGZILLA 1036
 *
 * Revision 1.4  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.3  2010-09-01 15:33:54  willuhn
 * @B Vormerkbuchungen in Saldo-Verlauf ignorieren, weil sie keinen Saldo haben
 *
 * Revision 1.2  2010-08-13 10:49:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 **********************************************************************/