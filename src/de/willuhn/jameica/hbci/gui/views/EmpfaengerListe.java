/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/EmpfaengerListe.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/03/03 22:26:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.EmpfaengerControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Empfaenger-Adressen an.
 */
public class EmpfaengerListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.setTitleText(i18n.tr("Vorhandene Empfängeradressen"));
		
		EmpfaengerControl control = new EmpfaengerControl(this);
		
		try {

			control.getEmpfaengerListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addCreateButton(i18n.tr("neuer Empfänger"),control);

		}
		catch (Exception e)
		{
			Application.getLog().error("error while loading empfaenger list",e);
			GUI.setActionText(i18n.tr("Fehler beim Lesen der Empfänger."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: EmpfaengerListe.java,v $
 * Revision 1.2  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/