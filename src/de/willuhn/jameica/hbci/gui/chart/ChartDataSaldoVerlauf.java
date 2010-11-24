/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataSaldoVerlauf.java,v $
 * $Revision: 1.16 $
 * $Date: 2010/11/24 16:27:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.hbci.util.SaldoFinder;

/**
 * Implementierung eines Datensatzes fuer die Darstellung des Saldenverlaufs.
 */
public class ChartDataSaldoVerlauf extends AbstractChartDataSaldo
{
  private Konto konto      = null;
  private Date start       = null;
  private List<Saldo> data = null;
  
  /**
   * ct.
   * @param k das Konto, fuer das das Diagramm gemalt werden soll.
   * @param start Start-Datum.
   */
  public ChartDataSaldoVerlauf(Konto k, Date start)
  {
    this.konto = k;
    this.start = start;
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

    list.addFilter("valuta >= ?", new Object[]{new java.sql.Date(start.getTime())});
    
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

    SaldoFinder finder = new SaldoFinder(list);
    this.data = new ArrayList<Saldo>();
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(start);
    Date end = HBCIProperties.endOfDay(new Date());
    
    while (!start.after(end))
    {
      Saldo s = new Saldo(start,finder.get(start));
      this.data.add(s);
      
      // Und weiter zum naechsten Tag
      cal.add(Calendar.DAY_OF_MONTH,1);
      start = cal.getTime();
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


/*********************************************************************
 * $Log: ChartDataSaldoVerlauf.java,v $
 * Revision 1.16  2010/11/24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 * Revision 1.15  2010-09-01 15:28:57  willuhn
 * @B Der letzte Tag wurde nicht beruecksichtigt - siehe Mail von Felix vom 01.09.
 *
 * Revision 1.14  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 **********************************************************************/