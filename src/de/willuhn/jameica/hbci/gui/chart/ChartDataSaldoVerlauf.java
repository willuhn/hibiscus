/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.jameica.hbci.util.SaldoFinder;
import de.willuhn.jameica.util.DateUtil;

/**
 * Implementierung eines Datensatzes fuer die Darstellung des Saldenverlaufs.
 */
public class ChartDataSaldoVerlauf extends AbstractChartDataSaldo
{
  private Konto konto      = null;
  private Date start       = null;
  private Date end         = null;
  private List<Value> data = null;
  
  /**
   * ct.
   * @param k das Konto, fuer das das Diagramm gemalt werden soll.
   * @param start Start-Datum.
   * @param end Ende-Datum.
   */
  public ChartDataSaldoVerlauf(Konto k, Date start, Date end)
  {
    this.konto = k;
    this.start = DateUtil.startOfDay(start == null ? new Date() : start);
    this.end = DateUtil.endOfDay(end == null ? new Date() : end);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
   */
  public List getData() throws RemoteException
  {
    if (this.data != null)
      return this.data;
    
    // Wir holen uns erstmal alle Umsaetze im Zeitraum
    DBIterator list = UmsatzUtil.getUmsaetze();
    if (this.konto != null)
      list.addFilter("konto_id = " + this.konto.getID());

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
    if (this.konto != null)
      startSaldo = this.konto.getNumUmsaetze() > 0 ? KontoUtil.getAnfangsSaldo(this.konto,this.start) : this.konto.getSaldo();
    
    SaldoFinder finder = new SaldoFinder(list,startSaldo);
    this.data = new ArrayList<Value>();
    
    Date localStart = this.start;
    final Calendar cal = Calendar.getInstance();
    cal.setTime(this.start);
    
    while (!localStart.after(this.end))
    {
      Value s = new Value(localStart,finder.get(localStart));
      this.data.add(s);
      
      // Und weiter zum naechsten Tag
      cal.add(Calendar.DAY_OF_MONTH,1);
      localStart = cal.getTime();
    }
    return this.data;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
   */
  public String getLabel() throws RemoteException
  {
    if (this.konto != null)
      return this.konto.getBezeichnung();
    return i18n.tr("Alle Konten");
  }
}
