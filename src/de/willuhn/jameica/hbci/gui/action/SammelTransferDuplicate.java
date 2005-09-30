/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelTransferDuplicate.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Duplizierung eines Sammel-Auftrages ausgefuehrt werden kann.
 * Er erwartet ein Objekt vom Typ <code>SammelTransfer</code> als Context.
 */
public class SammelTransferDuplicate implements Action
{
  // BUGZILLA 115 http://www.willuhn.de/bugzilla/show_bug.cgi?id=115

	private I18N i18n = null;

  /**
   * ct.
   */
  public SammelTransferDuplicate()
  {
    super();
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Dupliziert den uebergebenen Sammel-Transfer und oeffnet sie in einem neuen Dialog.
	 * Erwartet ein Objekt vom Typ <code>SammelTransfer</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null)
			throw new ApplicationException(i18n.tr("Kein Sammel-Auftrag angegeben"));

		try {
			SammelTransfer l = (SammelTransfer) context;
			GUI.startView(GUI.getCurrentView().getClass(),l.duplicate());
      GUI.getStatusBar().setSuccessText(i18n.tr("Sammel-Auftrag dupliziert"));
		}
		catch (RemoteException e)
		{
			Logger.error("error while duplicating sammeltransfer",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Duplizieren des Sammel-Auftrages"));
		}
  }

}


/**********************************************************************
 * $Log: SammelTransferDuplicate.java,v $
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/