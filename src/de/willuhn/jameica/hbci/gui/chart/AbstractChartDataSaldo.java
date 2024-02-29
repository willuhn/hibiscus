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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Basis-Implementierung eines Datensatzes fuer die Darstellung des Saldenverlaufs.
 */
public abstract class AbstractChartDataSaldo implements LineChartData
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private int[] color = null;
  private boolean legend = true;

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getDataAttribute()
   */
  public String getDataAttribute() throws RemoteException
  {
    return "value";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelAttribute()
   */
  public String getLabelAttribute() throws RemoteException
  {
    return "date";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getCurve()
   */
  public boolean getCurve()
  {
    return false;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getLineWidth()
   */
  @Override
  public int getLineWidth() throws RemoteException
  {
    return 1;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getLineStyle()
   */
  @Override
  public LineStyle getLineStyle() throws RemoteException
  {
    return null;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#isLegendEnabled()
   */
  @Override
  public boolean isLegendEnabled() throws RemoteException
  {
    return this.legend;
  }
  
  /**
   * Legt fest, ob die Legende angezeigt werden soll.
   * @param legend true, wenn die Legende angezeigt werden soll.
   */
  public void setLegendEnabled(boolean legend)
  {
    this.legend = legend;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getColor()
   */
  public int[] getColor() throws RemoteException
  {
    return this.color;
  }
  
  /**
   * Speichert die zu verwendende Farbe.
   * @param color die zu verwendende Farbe.
   * @throws RemoteException
   */
  public void setColor(int[] color) throws RemoteException
  {
    this.color = color;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#isFilled()
   */
  public boolean isFilled() throws RemoteException
  {
    return true;
  }
}
