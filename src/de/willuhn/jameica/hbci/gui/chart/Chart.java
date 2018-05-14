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
}


/*********************************************************************
 * $Log: Chart.java,v $
 * Revision 1.6  2010/11/24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 * Revision 1.5  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 **********************************************************************/