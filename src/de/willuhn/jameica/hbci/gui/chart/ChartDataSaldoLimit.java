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

import de.willuhn.jameica.hbci.forecast.ForecastCreator;
import de.willuhn.jameica.hbci.forecast.SaldoLimit;
import de.willuhn.jameica.hbci.forecast.SaldoLimit.Type;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.util.ColorGenerator;

/**
 * Implementierung eines Datensatzes für die Darstellung des Limits.
 */
public class ChartDataSaldoLimit extends AbstractChartDataSaldo
{
  private SaldoLimit.Type type = null;
  private List<Value> data = new ArrayList<>();
  
  /**
   * ct.
   * @param k das Konto.
   * @param start das Start-Datum.
   * @param type die Art des Limits.
   */
  public ChartDataSaldoLimit(Konto k, Date start, SaldoLimit.Type type)
  {
    this.type = type;
    
    final SaldoLimit l = ForecastCreator.getLimit(k,type);
    if (l != null && l.isEnabled())
    {
      if (start != null)
        this.data.add(new Value(start,l.getValue()));
      
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE,l.getDays());
      this.data.add(new Value(cal.getTime(),l.getValue()));
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
   */
  public List getData() throws RemoteException
  {
    return this.data;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
   */
  public String getLabel() throws RemoteException
  {
    return this.type.getDescription();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo#getColor()
   */
  @Override
  public int[] getColor() throws RemoteException
  {
    return ColorGenerator.create(ColorGenerator.PALETTE_PASTEL + (this.type == Type.UPPER ? 3 : 0));
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo#getLineStyle()
   */
  @Override
  public LineStyle getLineStyle() throws RemoteException
  {
    return LineStyle.DASH;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo#getLineWidth()
   */
  @Override
  public int getLineWidth() throws RemoteException
  {
    return 2;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo#isLegendEnabled()
   */
  @Override
  public boolean isLegendEnabled() throws RemoteException
  {
    return !this.data.isEmpty();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo#isFilled()
   */
  public boolean isFilled() throws RemoteException
  {
    return false;
  }
}
