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
import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Datensatzes fuer die Darstellung des Saldo-Durchschnitts.
 */
public class ChartDataSaldoTrend extends AbstractChartDataSaldo
{
  private List<Value> data = null;
  
  /**
   * Fuegt weitere Daten hinzu.
   * @param data weitere Daten.
   */
  public void add(List<Value> data)
  {
    if (data == null)
    {
      Logger.warn("skipping data line, contains no data");
      return;
    }
    if (this.data == null)
    {
      this.data = new ArrayList<Value>();

      for (int i=0;i<data.size();++i)
      {
        this.data.add(createAverage(data,i));
      }
    }
    else
    {
      for (int i=0;i<data.size();++i)
      {
        // Weitere Durchschnitte hinzufuegen
        Value s = this.data.get(i);
        s.setValue(s.getValue() + createAverage(data,i).getValue());
      }
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
   * @see de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoVerlauf#getLabel()
   */
  public String getLabel() throws RemoteException
  {
    return i18n.tr("Monatsdurchschnitt");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoVerlauf#getCurve()
   */
  public boolean getCurve()
  {
    return true;
  }

  /**
   * Liefert einen Saldo, dessen Saldo dem Durchschnitt der x Werte links
   * und rechts daneben entspricht.
   * @param list Liste der Umsaetze.
   * @param pos Position.
   * @return der Durchschnitt.
   */
  private Value createAverage(List<Value> list, int pos)
  {
    Value item = new Value(list.get(pos).getDate(),0.0d);

    int found = 0;
    Date first = null;
    for (int i=-30;i<=0;++i)
    {
      try
      {
        Value current = list.get(pos + i);
        found++;
        
        if (first == null)
          first = current.getDate();
        
        item.setValue(item.getValue() + current.getValue());
      }
      catch (Exception e)
      {
        // Ignore
      }
    }
    
    // Durchschnittswert bilden
    item.setValue(item.getValue() / found);
    return item;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo#isFilled()
   */
  public boolean isFilled() throws RemoteException
  {
    return false;
  }

}
