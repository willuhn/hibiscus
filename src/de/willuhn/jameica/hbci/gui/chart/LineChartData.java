/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/LineChartData.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/07/17 15:50:49 $
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

import org.eclipse.swt.graphics.Color;


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
   * Legt fest, ob auf der Linie fuer jeden Messwert noch ein kleines Kaestchen eingezeichnet wird.
   * @return true, wenn Kaestchen auf die Linie sollen.
   * @throws RemoteException
   */
  public boolean getShowMarker() throws RemoteException;

  /**
   * Liefert die zu verwendende Farbe.
   * @return die Farbe.
   * @throws RemoteException
   */
  public Color getColor() throws RemoteException;
  
}


/*********************************************************************
 * $Log: LineChartData.java,v $
 * Revision 1.1  2006/07/17 15:50:49  willuhn
 * @N Sparquote
 *
 **********************************************************************/