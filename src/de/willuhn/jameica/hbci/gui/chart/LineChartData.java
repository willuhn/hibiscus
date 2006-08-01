/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/LineChartData.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/08/01 21:29:12 $
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
}


/*********************************************************************
 * $Log: LineChartData.java,v $
 * Revision 1.2  2006/08/01 21:29:12  willuhn
 * @N Geaenderte LineCharts
 *
 * Revision 1.1  2006/07/17 15:50:49  willuhn
 * @N Sparquote
 *
 **********************************************************************/