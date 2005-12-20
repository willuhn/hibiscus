/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/UmsatzTyp.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/12/20 00:03:27 $
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

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.chart.ChartDataUmsatzTyp;
import de.willuhn.jameica.hbci.gui.chart.PieChart;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer Box, die die Umsatzverteilung anzeigt.
 */
public class UmsatzTyp extends AbstractBox
{
  private I18N i18n = null;

  /**
   * ct.
   */
  public UmsatzTyp()
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Umsatz-Analyse";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 4;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    try
    {
      LabelGroup group = new LabelGroup(parent,i18n.tr("Umsatz-Analyse"));
      PieChart chart = new PieChart();
      chart.setTitle(i18n.tr("Umsatz-Analyse"));
      chart.addData(new ChartDataUmsatzTyp());
      chart.paint(group.getComposite());
    }
    catch (Exception e)
    {
      Logger.error("unable to create pie chart",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Erzeugen des Diagramms"));
    }
  }

}


/*********************************************************************
 * $Log: UmsatzTyp.java,v $
 * Revision 1.1  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 **********************************************************************/