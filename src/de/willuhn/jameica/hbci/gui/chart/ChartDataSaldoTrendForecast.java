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

/**
 * Implementierung eines Datensatzes fuer die Darstellung des Saldo-Durchschnitts - jedoch als Prognose.
 */
public class ChartDataSaldoTrendForecast extends ChartDataSaldoTrend
{
  private ChartDataSaldoTrend base = null;
  
  /**
   * ct.
   * @param base die Basis.
   */
  public ChartDataSaldoTrendForecast(ChartDataSaldoTrend base)
  {
    this.base = base;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoTrend#getLabel()
   */
  @Override
  public String getLabel() throws RemoteException
  {
    return this.base.getLabel() + ": " + i18n.tr("Prognose");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo#getColor()
   */
  @Override
  public int[] getColor() throws RemoteException
  {
    return this.base.getColor();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo#getLineStyle()
   */
  @Override
  public LineStyle getLineStyle() throws RemoteException
  {
    return LineStyle.DOT;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo#isLegendEnabled()
   */
  @Override
  public boolean isLegendEnabled() throws RemoteException
  {
    return false;
  }
}
