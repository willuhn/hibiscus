/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/EmpfaengerNeu.java,v $
 * $Revision: 1.8 $
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

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.EmpfaengerControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Empfaenger bearbeiten.
 */
public class EmpfaengerNeu extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Empfänger bearbeiten"));
		
		final EmpfaengerControl control = new EmpfaengerControl(this);
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));

		try {
			group.addLabelPair(i18n.tr("Kontonummer"),			    		control.getKontonummer());
			group.addLabelPair(i18n.tr("Bankleitzahl"),			    		control.getBlz());
			group.addLabelPair(i18n.tr("Name"),			    						control.getName());

			control.init();
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading konto",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Empfängerdaten."));
		}

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(getParent(),3);
		buttonArea.addCancelButton(control);
		buttonArea.addDeleteButton(control);
		buttonArea.addStoreButton(control);


  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: EmpfaengerNeu.java,v $
 * Revision 1.8  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.3  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/