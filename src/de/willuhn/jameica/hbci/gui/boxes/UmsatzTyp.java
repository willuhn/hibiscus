/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/UmsatzTyp.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/06/29 23:10:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypChart;

/**
 * Implementierung einer Box, die die Umsatzverteilung anzeigt.
 */
public class UmsatzTyp extends AbstractBox
{

  /**
   * ct.
   */
  public UmsatzTyp()
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + "Umsatz-Analyse";
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 5;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    UmsatzTypChart chart = new UmsatzTypChart();
    chart.paint(parent);
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }


}


/*********************************************************************
 * $Log: UmsatzTyp.java,v $
 * Revision 1.5  2006/06/29 23:10:33  willuhn
 * @R Box-System aus Hibiscus in Jameica-Source verschoben
 * @C keine eigene Startseite mehr, jetzt alles ueber Jameica-Boxsystem geregelt
 *
 * Revision 1.4  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 * Revision 1.3  2006/03/20 00:35:53  willuhn
 * @N new box "Konten-Übersicht"
 *
 * Revision 1.2  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.1  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 **********************************************************************/