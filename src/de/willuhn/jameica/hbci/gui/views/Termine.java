/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/Termine.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/11/19 18:37:20 $
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
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt einen Kalender mit Terminen zu offenen Auftraegen an.
 */
public class Termine extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		GUI.getView().setTitle(i18n.tr("Termine"));

		de.willuhn.jameica.hbci.gui.parts.Termine termine = new de.willuhn.jameica.hbci.gui.parts.Termine();
		termine.paint(getParent());

		ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Back(false));
    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: Termine.java,v $
 * Revision 1.1  2010/11/19 18:37:20  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 **********************************************************************/