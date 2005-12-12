/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataSaldoVerlauf.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/12/12 18:53:00 $
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
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Implementierung eines Datensatzes fuer die Darstellung des Saldenverlaufs.
 */
public class ChartDataSaldoVerlauf implements ChartData
{

  private Konto konto         = null;
  private Formatter formatter = null;
  
  /**
   * ct.
   * @param k das Konto, fuer das das Diagramm gemalt werden soll.
   */
  public ChartDataSaldoVerlauf(Konto k)
  {
    this.konto = k;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
   */
  public GenericIterator getData() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(Umsatz.class);
    list.addFilter("konto_id = " + this.konto.getID());
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

}


/*********************************************************************
 * $Log: ChartDataSaldoVerlauf.java,v $
 * Revision 1.2  2005/12/12 18:53:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/