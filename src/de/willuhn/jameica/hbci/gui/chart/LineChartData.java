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


/**
 * Interface, welches die zu zeichnenden Datenreihen fuer ein Liniendiagramm enthaelt.
 */
public interface LineChartData extends ChartData
{
  /**
   * Legt fest, ob die Punkte gerade oder zu einer geschwungenen Linie verbunden werden sollen.
   * @return true, wenn die Punkte zu einer geschwungenen Linie verbunden werden sollen.
   * @throws RemoteException
   */
  public boolean getCurve() throws RemoteException;
  
  /**
   * Liefert die Linienbreite in Pixel.
   * @return die Linienbreite in Pixel.
   * @throws RemoteException
   */
  public int getLineWidth() throws RemoteException;
  
  /**
   * Liefert ein Array mit den Farbwerten (0-255) fuer Ror, Gruen und Blau.
   * @return Array mit den Farbwerten oder null, wenn eine zufaellige Farbe gewaehlt werden soll.
   * @throws RemoteException
   */
  public int[] getColor() throws RemoteException;
  
  /**
   * Legt fest, ob die Flaeche unter der Linie gefüllt ist oder nicht.
   * @return true falls gefuellt.
   * @throws RemoteException
   */
  public boolean isFilled() throws RemoteException;
}
