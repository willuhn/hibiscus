/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/UmsatzVerteilung.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/11/23 17:25:38 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypList;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypChart;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * View zur Anzeige der Umsatz-Analyse.
 */
public class UmsatzVerteilung extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    GUI.getView().setTitle(i18n.tr("Umsatz-Verteilung"));

    UmsatzTypChart chart = new UmsatzTypChart();
    chart.paint(getParent());
    
    ButtonArea buttons = new ButtonArea(getParent(),2);
    buttons.addButton(i18n.tr("Umsatz-Kategorien bearbeiten..."), new UmsatzTypList());
    buttons.addButton(i18n.tr("Zurück"),new Back(),null,true);
  }

}


/*********************************************************************
 * $Log: UmsatzVerteilung.java,v $
 * Revision 1.2  2006/11/23 17:25:38  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 * Revision 1.1  2006/08/05 22:00:51  willuhn
 * *** empty log message ***
 *
 **********************************************************************/