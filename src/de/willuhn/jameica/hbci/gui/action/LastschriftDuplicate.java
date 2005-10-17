/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/LastschriftDuplicate.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/10/17 22:00:44 $
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
import de.willuhn.jameica.hbci.gui.views.LastschriftNew;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Duplizierung einer Lastschrift ausgefuehrt werden kann.
 * Er erwartet ein Objekt vom Typ <code>Lastschrift</code> als Context.
 */
public class LastschriftDuplicate implements Action
{

	private I18N i18n = null;

  /**
   * ct.
   */
  public LastschriftDuplicate()
  {
    super();
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Dupliziert die uebergebene Lastschrift und oeffnet sie in einem neuen Dialog.
	 * Erwartet ein Objekt vom Typ <code>Lastschrift</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null)
			throw new ApplicationException(i18n.tr("Keine Lastschrift angegeben"));

		try {
			Lastschrift u = (Lastschrift) context;
			GUI.startView(LastschriftNew.class,u.duplicate());
		}
		catch (RemoteException e)
		{
			Logger.error("error while duplicating lastschrift",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Duplizieren der Lastschrift"));
		}
  }

}


/**********************************************************************
 * $Log: LastschriftDuplicate.java,v $
 * Revision 1.2  2005/10/17 22:00:44  willuhn
 * @B bug 143
 *
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/