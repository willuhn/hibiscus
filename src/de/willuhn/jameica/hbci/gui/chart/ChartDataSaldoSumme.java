/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/ChartDataSaldoSumme.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/16 08:44:08 $
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

/**
 * Implementierung eines Datensatzes fuer die Darstellung des addierten Saldenverlaufs.
 */
public class ChartDataSaldoSumme extends AbstractChartDataSaldo
{
  private List<Saldo> data = null;
  
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
  public void add(List<Saldo> data)
  {
    if (this.data == null)
      this.data = new ArrayList<Saldo>(data.size());

    boolean first = this.data.size() == 0;
    
    // Per Definition ist die Anzahl der Elemente immer gleich
    for (int i=0;i<data.size();++i)
    {
      Saldo saldo = data.get(i);

      Saldo sum = null;
      if (first)
      {
        // BUGZILLA 1044: Wir duerfen nicht die Saldo-Objekte von draussen
        // verwenden, weil wir sonst auf Referenzen arbeiten, die nicht uns gehoeren
        sum = new Saldo(saldo.getDatum(),saldo.getSaldo());
        this.data.add(i,sum);
      }
      else
      {
        sum = this.data.get(i);
        sum.setSaldo(sum.getSaldo() + saldo.getSaldo());
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
 * Revision 1.2  2011/05/16 08:44:08  willuhn
 * @B BUGZILLA 1044
 *
 * Revision 1.1  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 **********************************************************************/