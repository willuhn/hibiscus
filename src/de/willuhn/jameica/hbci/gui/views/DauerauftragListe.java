/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/DauerauftragListe.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/10/08 13:37:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.DauerauftragControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Zeigt eine Liste mit den vorhandenen Dauerauftraegen an.
 */
public class DauerauftragListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Daueraufträge"));
		
		DauerauftragControl control = new DauerauftragControl(this);
		
		try {

			control.getDauerauftragListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),2);
			buttons.addCustomButton(i18n.tr("Existierende Daueraufträge abrufen"), new Listener()
      {
        public void handleEvent(Event event)
        {
          // TODO Auto-generated method stub

        }
      });
			buttons.addCreateButton(i18n.tr("neuer Dauerauftrag"),control);

		}
		catch (Exception e)
		{
			Logger.error("error while loading dauerauftrag list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Daueraufträge."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: DauerauftragListe.java,v $
 * Revision 1.6  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.4  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.2  2004/07/13 23:26:14  willuhn
 * @N Views fuer Dauerauftrag
 *
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/