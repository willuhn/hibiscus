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

import de.willuhn.jameica.gui.Part;

/**
 * Basis-Interface fuer ein Chart.
 * @param <T> der Typ der Chartdaten.
 */
public interface Chart<T extends ChartData> extends Part
{
  /**
   * Speichert den Titel des Charts.
   * @param title Titel.
   */
  public void setTitle(String title);
  
  /**
   * Liefert den Titel des Charts.
   * @return Titel.
   */
  public String getTitle();
  
  /**
   * Fuegt dem Chart eine Datenreihe hinzu,
   * @param data
   */
  public void addData(T data);
  
  /**
   * Entfernt eine Datenreihe aus dem Chart.
   * @param data
   */
  public void removeData(T data);
  
  /**
   * Entfernt alle Datenreihen.
   */
  public void removeAllData();

  /**
   * Zeichnet das Chart neu.
   * Ist eigentlich nur noetig, wenn sich die Daten tatsaechlich geaendert haben.
   * @throws RemoteException
   */
  public void redraw() throws RemoteException;
  
  /**
   * Liefert das eigentliche SWT-Chart-Objekt.
   * @return das eigentliche SWT-Chart-Objekt.
   */
  public org.swtchart.Chart getChart();
  
  /**
   * Fuegt ein Feature hinzu.
   * @param feature das Feature.
   */
  public void addFeature(ChartFeature feature);

  /**
   * Entfernt das Feature.
   * @param feature das zu entfernende Feature.
   */
  public void removeFeature(ChartFeature feature);

}
