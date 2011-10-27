/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataSaldoSumme.java,v $
 * $Revision: 1.4 $
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
import java.util.List;

import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Datensatzes fuer die Darstellung des addierten Saldenverlaufs.
 */
public class ChartDataSaldoSumme extends AbstractChartDataSaldo
{
  private List<Value> data = null;
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
   */
  public List getData() throws RemoteException
  {
    return this.data;
  }
  
  /**
   * Fuegt weitere Daten hinzu.
   * @param data weitere Daten.
   */
  public void add(List<Value> data)
  {
    // Per Definition ist die Anzahl der Elemente in data und this.data immer gleich

    if (this.data == null)
    {
      // BUGZILLA 1044: Wir duerfen nicht die Saldo-Objekte von draussen
      // verwenden, weil wir sonst auf Referenzen arbeiten, die nicht uns gehoeren
      this.data = new ArrayList<Value>(data.size());
      for (int i=0;i<data.size();++i)
      {
        Value saldo = data.get(i);
        Value sum = new Value(saldo.getDate(),saldo.getValue());
        this.data.add(sum);
      }
    }
    else
    {
      for (int i=0;i<data.size();++i)
      {
        Value saldo = data.get(i);
        Value sum = this.data.get(i);
        sum.setValue(sum.getValue() + saldo.getValue());
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
   */
  public String getLabel() throws RemoteException
  {
    return i18n.tr("Summe");
  }
}


/*********************************************************************
 * $Log: ChartDataSaldoSumme.java,v $
 * Revision 1.4  2011/10/27 17:09:29  willuhn
 * @C Saldo-Bean in neue separate (und generischere) Klasse "Value" ausgelagert.
 * @N Saldo-Finder erweitert, damit der jetzt auch mit Value-Objekten arbeiten kann
 *
 * Revision 1.3  2011-05-16 08:46:46  willuhn
 * @N BUGZILLA 1044
 *
 * Revision 1.2  2011-05-16 08:44:08  willuhn
 * @B BUGZILLA 1044
 *
 * Revision 1.1  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 **********************************************************************/