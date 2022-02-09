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
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Value;

/**
 * Implementierung eines Datensatzes fuer die Darstellung des Saldenverlaufs.
 */
public class ChartDataSaldoVerlauf extends AbstractChartDataSaldo
{
  private Konto konto      = null;
  private List<Value> data = null;
  
  /**
   * ct. 
   * @param konto das Konto, fuer das das Diagramm gemalt werden soll.
   * @param data
   */
  public ChartDataSaldoVerlauf(Konto konto, List<Value> data)
  {
    this.konto = konto;
    this.data = data;
  }

  @Override
  public List getData()
  {
    return this.data;
  }

  @Override
  public String getLabel() throws RemoteException
  {
    if (this.konto != null)
      return this.konto.getBezeichnung();
    return i18n.tr("Alle Konten");
  }
}
