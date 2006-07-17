/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataSaldoVerlauf.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/07/17 15:50:49 $
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

import org.eclipse.swt.graphics.Color;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;

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
    DBIterator list = Settings.getDBService().createList(Umsatz.class);
    list.addFilter("konto_id = " + this.konto.getID());

    if (this.days > 0)
    {
      long d = days * 24l * 60l * 60l * 1000l;
      list.addFilter("TONUMBER(valuta) > " + (System.currentTimeMillis() - d));
    }
    list.setOrder(" ORDER BY TONUMBER(valuta) ASC");
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
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getColor()
   */
  public Color getColor() throws RemoteException
  {
    return de.willuhn.jameica.gui.util.Color.LINK.getSWTColor();
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

}


/*********************************************************************
 * $Log: ChartDataSaldoVerlauf.java,v $
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