/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/DauerauftragUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/09/02 22:10:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Turnus;


/**
 * Hilfsklasse fuer Dauerauftraege.
 */
public class DauerauftragUtil
{
  // Hilfsmapping, um die Tages-Konstanten aus java.util.Calendar in
  // integer von 1 (montag) - 7 (sonntag) umrechnen zu koennen
  private final static int[] DAYMAP = new int[]
    {
      Calendar.MONDAY,
      Calendar.TUESDAY,
      Calendar.WEDNESDAY,
      Calendar.THURSDAY,
      Calendar.FRIDAY,
      Calendar.SATURDAY,
      Calendar.SUNDAY
    };
  

  /**
   * Berechnet das naechste Ausfuehrungsdatum fuer einen Turnus.
   * @param ersteZahlung Datum der ersten Zahlung.
   * @param letzteZahlung Datum der letzten Zahlung.
   * @param turnus Turnus.
   * @param valuta Stichtag, zu dem die Berechnung erfolgen soll.
   * Ist kein Datum angegeben, wird das aktuelle verwendet.
   * @return das ermittelte Datum oder <code>null</code>, wenn keines mehr existiert.
   * @throws RemoteException
   */
  public static Date getNaechsteZahlung(Date ersteZahlung, Date letzteZahlung, Turnus turnus, Date valuta) throws RemoteException
  {
    // Keine erste Zahlung angegeben und kein Turnus. Nichts ermittelbar
    if (ersteZahlung == null || turnus == null)
      return null;
    
    if (valuta == null)
      valuta = new Date();
    
    // Das Datum der ersten Zahlung liegt in der Zukunft oder ist heute. Dann brauchen
    // wir gar nicht rechnen, sondern koennen gleich das nehmen.
    if (ersteZahlung.after(valuta) || ersteZahlung.equals(valuta))
      return ersteZahlung;

    // Auftrag bereits abgelaufen, da sich das Valuta-Datum hinter
    // der letzten Ausfuehrung befindet
    if (letzteZahlung != null && letzteZahlung.before(valuta))
      return null;

    // OK, wenn wir hier angekommen sind, muessen wir rechnen ;)
    Calendar cal = Calendar.getInstance();
    cal.setTime(ersteZahlung);
    cal.setFirstDayOfWeek(Calendar.MONDAY);
    
    int ze  = turnus.getZeiteinheit();
    int tag = turnus.getTag();
    int iv  = turnus.getIntervall();

    Date test = null;
    
    // eigentlich gehoert hier ein "while true" hin, ich will aber eine
    // Abbruchbedingung, damit das Teil keine 1000 Jahre in die Zukunft
    // rechnet ;)
    for (int i=0;i<1000;++i)
    {
      // Woechentlich
      if (ze == Turnus.ZEITEINHEIT_WOECHENTLICH)
      {
        // Wochentag festlegen
        int calTag = DAYMAP[tag-1]; // "-1" weil das Array bei 0 anfaengt
        cal.set(Calendar.DAY_OF_WEEK,calTag);

        test = cal.getTime();
        if (test != null && (test.after(valuta)) || test.equals(valuta))
          return test; // Datum gefunden

        // Ne, dann Anzahl der Wochen drauf rechnen
        cal.add(Calendar.WEEK_OF_YEAR,iv);
      }
      // Monatlich
      else
      {
        // Tag im Monat festlegen
        if (tag == HBCIProperties.HBCI_LAST_OF_MONTH)
          cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        else
          cal.set(Calendar.DAY_OF_MONTH,tag);

        test = cal.getTime();
        if (test != null && (test.after(valuta)) || test.equals(valuta))
          return test; // Datum gefunden

        // Ne, dann Anzahl der Monate drauf rechnen
        cal.add(Calendar.MONTH,iv);
      }
    }
    return null; // kein Datum ermittelbar
  }
}


/**********************************************************************
 * $Log: DauerauftragUtil.java,v $
 * Revision 1.1  2008/09/02 22:10:26  willuhn
 * @B BUGZILLA 617 - Berechnungsfunktion grundlegend ueberarbeitet.
 *
 **********************************************************************/
