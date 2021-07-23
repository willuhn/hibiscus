/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 * Class author: Fabian Aiteanu
 **********************************************************************/

package de.willuhn.jameica.hbci.report.balance;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoVerlauf;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.jameica.hbci.util.SaldoFinder;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;

/**
 * AccountBalance-Provider fuer normale Konten mit Umsatz-Buchungen.
 */
public class BookingAccountBalanceProvider implements AccountBalanceProvider
{

  @Override
  public boolean supports(Konto konto) {
    // Dies ist der Standard-Provider fuer Konten in Hibiscus und er unterstuetzt jedes Konto per Definition.
    return true;
  }

  @Override
  public AbstractChartDataSaldo getBalanceChartData(Konto konto, Date start, Date end) {
    List<Value> data = getBalanceData(konto, start, end);
    return new ChartDataSaldoVerlauf(konto, data);
  }

  @Override
  public List<Value> getBalanceData(Konto konto, Date start, Date end)
  {
    start = DateUtil.startOfDay(start == null ? new Date() : start);
    end = DateUtil.endOfDay(end == null ? new Date() : end);
    ArrayList<Value> data = new ArrayList<Value>();
    
    try
    {
      // Wir holen uns erstmal alle Umsaetze im Zeitraum
      DBIterator list = UmsatzUtil.getUmsaetze();
      if (konto != null)
        list.addFilter("konto_id = " + konto.getID());
  
      list.addFilter("datum >= ?", new Object[] { new java.sql.Date(start.getTime()) });
      list.addFilter("datum <= ?", new Object[] { new java.sql.Date(end.getTime()) });
      
      // Jetzt kommt die Homogenisierung ;)
      // Wir brauchen genau einen Messwert pro Tag. Das ist wichtig,
      // damit auch unterschiedliche Konten in einem Chart ueberlagernd
      // angezeigt werden koennen. Nehmen wir jetzt einfach die Umsaetze
      // aus der DB, haben wir womoeglich nicht fuer jeden Tag einen
      // Messwert, weil nicht genuegend Umsaetze vorhanden sind. Effekt:
      // In Konto A haben wir 10 Umsaetze, in Konto B aber 20. Wir haben
      // fuer Konto B also Messpunkte, zu denen in Konto A kein korrelierender
      // Wert existiert. Sowas kann man sauber nicht zeichnen. Daher iterieren
      // wir jetzt tageweise ueber die angegebene Zeitspanne. Fuer jeden Tag
      // schauen wir, ob wir einen Umsatz haben. Liegt keiner vor, nehmen
      // wir den letzten Umsatz, der vor diesem Tag liegt, da der dort
      // angegebene Saldo ja zum gesuchten Tag noch gilt.
  
      // BUGZILLA 1036
      double startSaldo = 0.0d;
      if (konto != null)
        startSaldo = konto.getNumUmsaetze() > 0 ? KontoUtil.getAnfangsSaldo(konto, start) : konto.getSaldo();
      
      SaldoFinder finder = new SaldoFinder(list,startSaldo);
      
      Date localStart = start;
      final Calendar cal = Calendar.getInstance();
      cal.setTime(start);
      
      while (!localStart.after(end))
      {
        Value s = new Value(localStart,finder.get(localStart));
        data.add(s);
        
        // Und weiter zum naechsten Tag
        cal.add(Calendar.DAY_OF_MONTH,1);
        localStart = cal.getTime();
      }
    } catch (RemoteException e)
    {
      Logger.error("Fehler beim der Ermittlung der Salden", e);
    }
    return data;
  }
  
  @Override
  public String getName()
  {
    return "BookingAccountBalanceProvider";
  }
}


