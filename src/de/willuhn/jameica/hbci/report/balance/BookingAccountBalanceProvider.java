/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 * Class author: Fabian Aiteanu
 **********************************************************************/

package de.willuhn.jameica.hbci.report.balance;

import java.util.Date;

import de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo;
import de.willuhn.jameica.hbci.gui.chart.ChartDataSaldoVerlauf;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * AccountBalance-Provider fuer normale Konten mit Umsatz-Buchungen.
 */
public class BookingAccountBalanceProvider implements AccountBalanceProvider
{

  /**
   * @see de.willuhn.jameica.hbci.report.balance.AccountBalanceProvider#supports(de.willuhn.jameica.hbci.rmi.Konto)
   */
  @Override
  public boolean supports(Konto konto) {
    // Dies ist der Standard-Provider fuer Konten in Hibiscus und er unterstuetzt jedes Konto per Definition.
    return true;
  }

  /**
   * @see de.willuhn.jameica.hbci.report.balance.AccountBalanceProvider#getBalanceChartData(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date, java.util.Date)
   */
  @Override
  public AbstractChartDataSaldo getBalanceChartData(Konto konto, Date start, Date end) {
    return new ChartDataSaldoVerlauf(konto, start, end);
  }

  /**
   * @see de.willuhn.jameica.hbci.report.balance.AccountBalanceProvider#getName()
   */
  @Override
  public String getName()
  {
    return "BookingAccountBalanceProvider";
  }
}


