/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataSaldoVerlauf.java,v $
 * $Revision: 1.13 $
 * $Date: 2010/08/11 16:06:04 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Datensatzes fuer die Darstellung des Saldenverlaufs.
 */
public class ChartDataSaldoVerlauf implements LineChartData
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Konto konto         = null;
  private Formatter formatter = null;
  private int days            = -1;
  
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
    if (this.konto != null)
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
    if (this.konto != null)
      return this.konto.getBezeichnung();
    return i18n.tr("Alle Konten");
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
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getColor()
   */
  public int[] getColor() throws RemoteException
  {
    return null;
  }
}


/*********************************************************************
 * $Log: ChartDataSaldoVerlauf.java,v $
 * Revision 1.13  2010/08/11 16:06:04  willuhn
 * @N BUGZILLA 783 - Saldo-Chart ueber alle Konten
 *
 * Revision 1.12  2008/02/26 01:12:30  willuhn
 * @R nicht mehr benoetigte Funktion entfernt
 **********************************************************************/