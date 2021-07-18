/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.BaseDauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;

/**
 * Hilfsklasse zum Berechnen von Zahlungsstroemen bei Dauerauftraegen.
 */
public class DauerauftragUtil
{
  /**
   * Prueft, ob der Dauerauftrag im genannten Zeitraum ausgefuehrt wird oder wurde.
   * @param t der Dauerauftrag.
   * @param from Start-Datum.
   * @param to End-Datum.
   * @return die Termine, zu denen der Auftrag im angegebenen Zeitraum ausgefuehrt wird.
   * @throws RemoteException
   */
  public static List<Date> getTermine(BaseDauerauftrag t, Date from, Date to) throws RemoteException
  {
    Date de       = t.getErsteZahlung();
    Date dl       = t.getLetzteZahlung();
    Turnus turnus = t.getTurnus();

    if (from == null)
      from = new Date();

    List<Date> result = new ArrayList<Date>();

    // Auftrag faengt erst spaeter an
    if (de != null && to != null && de.after(to))
      return result;

    // Auftrag ist schon abgelaufen
    if (dl != null && dl.before(from))
      return result;

    // Wir machen maximal 100 Iterationen. Das hab ich jetzt willkuerlich
    // festgelegt. Wenn das Zeitfenster 1 Monat ist, koennen es ohnehin nur
    // maximal 5 Termine sein. Fuer den Fall, dass das Zeitfenster aber mal
    // groesser ist, machen wir ein paar mehr Iterationen
    Date start = from;
    if (de.after(start)) // Der Auftrag faengt erst mitten im Zeitraum an
      start = de;

    for (int i=0;i<100;++i)
    {
      if (to != null && start.after(to))
        break; // Wir sind raus

      // Als Valuta nehmen wir den ersten des Monats
      Date d = TurnusHelper.getNaechsteZahlung(de,dl,turnus,start);

      // Wir haben keine weiteren Termine mehr gefunden oder sind aus dem Zeitfenster raus
      if (d == null || (to != null && d.after(to)))
        break;

      // Tag uebernehmen
      result.add(d);

      // Noch einen Tag weiterruecken
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
      cal.add(Calendar.DAY_OF_MONTH,1);
      d = cal.getTime();

      // Und wir machen beim naechsten Tag weiter
      start = d;

    }
    return result;
  }
}
