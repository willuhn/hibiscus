/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/TurnusNew.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/11/15 00:38:30 $
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

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.TurnusControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Turnus bearbeiten.
 */
public class TurnusNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Zahlungsturnus bearbeiten"));
		
		final TurnusControl control = new TurnusControl(this);
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));

		try {
			group.addLabelPair(i18n.tr("Zeiteinheit"),control.getZeiteinheit());
			group.addLabelPair(i18n.tr("Zahlung aller"),control.getIntervall());
			group.addLabelPair(i18n.tr("Zahlung am"), control.getTagWoechentlich());
			group.addLabelPair("", control.getTagMonatlich());
			
			group.addSeparator();
			group.addLabelPair("", control.getComment());
			
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading turnus",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen des Zahlungsturnus."));
		}

		// und noch die Abschicken-Knoepfe
		if (!control.getTurnus().isInitial())
		{
			ButtonArea buttonArea = new ButtonArea(getParent(),1);
			buttonArea.addButton(i18n.tr("Speichern"), new Action()
			{
				public void handleAction(Object context) throws ApplicationException
				{
					control.handleStore();
				}
			},null,true);
		}


  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: TurnusNew.java,v $
 * Revision 1.2  2004/11/15 00:38:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 **********************************************************************/