/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/UmsatzListe.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/04/04 18:30:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UmsatzControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class UmsatzListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Umsätze des Kontos"));
		
		final UmsatzControl control = new UmsatzControl(this);

		try {
			
			Table list = control.getUmsatzListe();
			list.paint(getParent());
			
			ButtonArea buttons = new ButtonArea(getParent(),3);
			buttons.addCustomButton(i18n.tr("Umsätze abrufen"), new MouseAdapter() {
        public void mouseUp(MouseEvent e) {
        	control.handleGetUmsaetze();
        }
      });
			buttons.addCustomButton(i18n.tr("alle Umsätze löschen"), new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					control.handleDeleteUmsaetze();
				}
			});
			buttons.addCancelButton(control);
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while loading umsatz list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden der Umsätze"));
		}

  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: UmsatzListe.java,v $
 * Revision 1.3  2004/04/04 18:30:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/