/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataUmsatzTyp.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/04/03 21:39:07 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Datensatzes fuer die Darstellung der Umsatz-Verteilung.
 */
public class ChartDataUmsatzTyp implements ChartData
{
  private I18N i18n = null;
  private boolean einnahmen = true;
  private int days = -1;
  
  /**
   * @param einnahmen legt fest, ob es sich um Einnahmen oder Ausgaben handelt.
   * ct.
   */
  public ChartDataUmsatzTyp(boolean einnahmen)
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.einnahmen = einnahmen;
  }

  /**
   * @param einnahmen legt fest, ob es sich um Einnahmen oder Ausgaben handelt.
   * @param days Anzahl der Tage.
   * ct.
   */
  public ChartDataUmsatzTyp(boolean einnahmen, int days)
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.einnahmen = einnahmen;
    this.days = days;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
   */
  public GenericIterator getData() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(UmsatzTyp.class);
    list.addFilter("iseinnahme = " + (einnahmen ? "1" : "0"));
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
   */
  public String getLabel() throws RemoteException
  {
    return i18n.tr("Umsatz-Verteilung");
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getDataAttribute()
   */
  public String getDataAttribute() throws RemoteException
  {
    if (days > 0)
      return "umsatz:" + days; 
    return "umsatz";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelAttribute()
   */
  public String getLabelAttribute() throws RemoteException
  {
    return "name";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelFormatter()
   */
  public Formatter getLabelFormatter() throws RemoteException
  {
    return null;
  }

}


/*********************************************************************
 * $Log: ChartDataUmsatzTyp.java,v $
 * Revision 1.3  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 * Revision 1.2  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.1  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 * Revision 1.2  2005/12/12 18:53:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/