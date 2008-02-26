/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataSaldoVerlauf.java,v $
 * $Revision: 1.11 $
 * $Date: 2008/02/26 01:01:16 $
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.UmsatzUtil;

/**
 * Implementierung eines Datensatzes fuer die Darstellung des Saldenverlaufs.
 */
public class ChartDataSaldoVerlauf implements LineChartData
{

  private Konto konto         = null;
  private Formatter formatter = null;
  private int days            = -1;
  
  /**
   * ct.
   * @param k das Konto, fuer das das Diagramm gemalt werden soll.
   */
  public ChartDataSaldoVerlauf(Konto k)
  {
    this.konto = k;
  }

  /**
   * ct.
   * @param k das Konto, fuer das das Diagramm gemalt werden soll.
   * @param days Anzahl der Tage, die der Chart in die Vergangenheit reichen soll.
   */
  public ChartDataSaldoVerlauf(Konto k, int days)
  {
    this.konto = k;
    this.days = days;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
   */
  public GenericIterator getData() throws RemoteException
  {
    DBIterator list = UmsatzUtil.getUmsaetze();
    list.addFilter("konto_id = " + this.konto.getID());

    if (this.days > 0)
    {
      long d = days * 24l * 60l * 60l * 1000l;
      Date start = HBCIProperties.startOfDay(new Date(System.currentTimeMillis() - d));
      list.addFilter("valuta >= ?", new Object[]{new java.sql.Date(start.getTime())});
    }
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
   */
  public String getLabel() throws RemoteException
  {
    return this.konto.getBezeichnung();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getDataAttribute()
   */
  public String getDataAttribute() throws RemoteException
  {
    return "saldo";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelAttribute()
   */
  public String getLabelAttribute() throws RemoteException
  {
    return "datum";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelFormatter()
   */
  public Formatter getLabelFormatter() throws RemoteException
  {
    if (this.formatter != null)
      return this.formatter;
    
    return new Formatter() {
      private DateFormat df = new SimpleDateFormat("dd.MM.yy");

      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null)
          return "";
        if (!(o instanceof Date))
          return o.toString();
        return df.format((Date)o);
      }
    };
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getCurve()
   */
  public boolean getCurve()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getShowMarker()
   */
  public boolean getShowMarker()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getColor()
   */
  public int[] getColor() throws RemoteException
  {
    return null;
  }
}


/*********************************************************************
 * $Log: ChartDataSaldoVerlauf.java,v $
 * Revision 1.11  2008/02/26 01:01:16  willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp. die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet werden muss
 *
 * Revision 1.10  2007/08/07 23:54:16  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 * Revision 1.9  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.8  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.7  2006/08/25 10:13:43  willuhn
 * @B Fremdschluessel NICHT mittels PreparedStatement, da die sonst gequotet und von McKoi nicht gefunden werden. BUGZILLA 278
 *
 * Revision 1.6  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.5  2006/08/01 21:29:12  willuhn
 * @N Geaenderte LineCharts
 *
 * Revision 1.4  2006/07/17 15:50:49  willuhn
 * @N Sparquote
 *
 * Revision 1.3  2006/03/09 18:24:05  willuhn
 * @N Auswahl der Tage in Umsatz-Chart
 *
 * Revision 1.2  2005/12/12 18:53:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/