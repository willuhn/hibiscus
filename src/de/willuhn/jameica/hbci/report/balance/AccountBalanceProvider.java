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
import java.util.List;

import de.willuhn.jameica.hbci.gui.chart.AbstractChartDataSaldo;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Value;
import de.willuhn.jameica.hbci.HasName;

/**
 * Interface fuer einen AccountBalance-Provider.
 * Der Provider liefert Salden fuer ein Konto, was bei einem Fonds/Depot anders funktioniert als bei einem Girokonto. 
 */
public interface AccountBalanceProvider extends HasName
{

  /**
   * Gibt an, ob der Provider fuer ein konkretes Konto Daten liefern kann.
   * @param konto Zu ueberpruefendes Konto
   * @return
   */
  public boolean supports(Konto konto);

  /**
   * Gibt die taeglichen Salden fuer ein Konto als Liste von Werten zurueck.
   * @param konto Konto fuer den Saldenabruf
   * @param start Startdatum der Salden
   * @param end Enddatum der Salden
   * @return die taeglichen Salden fuer ein Konto als Liste von Werten
   */
  public List<Value> getBalanceData(Konto konto, Date start, Date end);
  
  /**
   * Gibt die taeglichen Salden fuer ein Konto als Chart-Datenreihe zurueck.
   * @param konto Konto fuer den Saldenabruf
   * @param start Startdatum der Salden
   * @param end Enddatum der Salden
   * @return die taeglichen Salden fuer ein Konto als Chart-Datenreihe
   */
  public AbstractChartDataSaldo getBalanceChartData(Konto konto, Date start, Date end);

  /**
   * Liefert einen Namen f�r Anzeige und Sortierung
   * @return einen Namen f�r Anzeige und Sortierung
   */
  @Override
  public String getName();
}


