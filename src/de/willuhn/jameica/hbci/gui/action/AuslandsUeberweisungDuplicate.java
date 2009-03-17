/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AuslandsUeberweisungDuplicate.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/17 23:44:15 $
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
import de.willuhn.jameica.hbci.gui.views.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Duplizierung einer Auslandsueberweisung ausgefuehrt werden kann.
 * Er erwartet ein Objekt vom Typ <code>AuslandsUeberweisung</code> als Context.
 */
public class AuslandsUeberweisungDuplicate implements Action
{

	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Dupliziert die uebergebene Ueberweisung und oeffnet sie in einem neuen Dialog.
	 * Erwartet ein Objekt vom Typ <code>Ueberweisung</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null || !(context instanceof AuslandsUeberweisung))
			throw new ApplicationException(i18n.tr("Keine Auslandsüberweisung angegeben"));

		try {
			AuslandsUeberweisung u = (AuslandsUeberweisung) context;
			GUI.startView(AuslandsUeberweisungNew.class,u.duplicate());
		}
		catch (RemoteException e)
		{
			Logger.error("error while duplicating foreign transfer",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Duplizieren der Auslandsüberweisung"));
		}
  }

}


/**********************************************************************
 * $Log: AuslandsUeberweisungDuplicate.java,v $
 * Revision 1.1  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 **********************************************************************/