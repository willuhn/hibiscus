/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/DauerauftragListe.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/07/13 23:26:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.DauerauftragControl;
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

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Daueraufträge"));
		
		DauerauftragControl control = new DauerauftragControl(this);
		
		try {

			control.getDauerauftragListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),2);
			buttons.addCustomButton(i18n.tr("Existierende Daueraufträge abrufen"),new MouseAdapter()
      {
        public void mouseUp(MouseEvent e)
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
 * Revision 1.2  2004/07/13 23:26:14  willuhn
 * @N Views fuer Dauerauftrag
 *
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/