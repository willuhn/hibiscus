/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/Termine.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/11/21 23:57:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.util.Date;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt einen Kalender mit Terminen zu offenen Auftraegen an.
 */
public class Termine extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private static Date currentDate = null;
  
  private de.willuhn.jameica.hbci.gui.parts.Termine termine = null;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		GUI.getView().setTitle(i18n.tr("Termine"));

		this.termine = new de.willuhn.jameica.hbci.gui.parts.Termine();
		this.termine.setCurrentDate(currentDate);
		this.termine.paint(getParent());

		ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Back(false));
    buttons.paint(getParent());
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
    currentDate = this.termine.getCurrentDate();
  }
}


/**********************************************************************
 * $Log: Termine.java,v $
 * Revision 1.2  2010/11/21 23:57:58  willuhn
 * @N Wir merken uns das letzte Datum und springen wieder zu dem zurueck, wenn wir z.Bsp. aus der Detail-Ansicht eines Auftrages zurueckkommen
 *
 * Revision 1.1  2010-11-19 18:37:20  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 **********************************************************************/