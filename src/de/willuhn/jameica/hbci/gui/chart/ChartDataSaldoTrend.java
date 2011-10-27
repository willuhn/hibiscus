/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataSaldoTrend.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/10/27 17:09:29 $
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
    for (int i=-15;i<=15;++i)
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
}


/*********************************************************************
 * $Log: ChartDataSaldoTrend.java,v $
 * Revision 1.5  2011/10/27 17:09:29  willuhn
 * @C Saldo-Bean in neue separate (und generischere) Klasse "Value" ausgelagert.
 * @N Saldo-Finder erweitert, damit der jetzt auch mit Value-Objekten arbeiten kann
 *
 * Revision 1.4  2011-05-03 10:15:56  willuhn
 * @B NPE
 *
 * Revision 1.3  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.2  2010-08-11 16:06:04  willuhn
 * @N BUGZILLA 783 - Saldo-Chart ueber alle Konten
 *
 * Revision 1.1  2009/08/27 13:37:28  willuhn
 * @N Der grafische Saldo-Verlauf zeigt nun zusaetzlich eine Trendkurve an
 *
 **********************************************************************/