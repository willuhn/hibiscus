/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/chart/Chart.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/12/20 00:03:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.chart;

import de.willuhn.jameica.gui.Part;

/**
 * Basis-Interface fuer ein Chart.
 */
public interface Chart extends Part
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
  public void addData(ChartData data);
}


/*********************************************************************
 * $Log: Chart.java,v $
 * Revision 1.2  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 * Revision 1.1  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 **********************************************************************/