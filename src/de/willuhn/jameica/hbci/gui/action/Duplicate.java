/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Duplicate.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/01/27 22:43:22 $
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
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Generische Action zum Duplizieren eines Auftrages.
 * Erwartet als Parameter ein Duplicatable.
 */
public class Duplicate implements Action
{

	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Dupliziert das Objekt und oeffnet es in einem neuen Dialog.
	 * Erwartet ein Objekt vom Typ <code>Duplicatable</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null || !(context instanceof Duplicatable))
			throw new ApplicationException(i18n.tr("Keine zu duplizierenden Daten angegeben"));

		try {
			Duplicatable o = (Duplicatable) context;
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Dupliziert"),StatusBarMessage.TYPE_SUCCESS));
			new Open().handleAction(o.duplicate());
		}
		catch (RemoteException e)
		{
			Logger.error("error while duplicating object",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Duplizieren"));
		}
  }

}


/**********************************************************************
 * $Log: Duplicate.java,v $
 * Revision 1.1  2012/01/27 22:43:22  willuhn
 * @N BUGZILLA 1181
 *
 **********************************************************************/