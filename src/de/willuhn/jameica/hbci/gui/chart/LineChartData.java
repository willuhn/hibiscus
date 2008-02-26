/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/LineChartData.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/02/26 01:12:30 $
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
   * Liefert ein Array mit den Farbwerten (0-255) fuer Ror, Gruen und Blau.
   * @return Array mit den Farbwerten oder null, wenn eine zufaellige Farbe gewaehlt werden soll.
   * @throws RemoteException
   */
  public int[] getColor() throws RemoteException;
}


/*********************************************************************
 * $Log: LineChartData.java,v $
 * Revision 1.4  2008/02/26 01:12:30  willuhn
 * @R nicht mehr benoetigte Funktion entfernt
 *
 * Revision 1.3  2008/02/26 01:01:16  willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp. die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet werden muss
 *
 * Revision 1.2  2006/08/01 21:29:12  willuhn
 * @N Geaenderte LineCharts
 *
 * Revision 1.1  2006/07/17 15:50:49  willuhn
 * @N Sparquote
 *
 **********************************************************************/