/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UeberweisungDuplicate.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/18 23:38:17 $
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
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action, die zur Duplizierung einer Ueberweisung ausgefuehrt werden kann.
 * Er erwartet ein Objekt vom Typ <code>Ueberweisung</code> als Context.
 */
public class UeberweisungDuplicate implements Action
{

	private I18N i18n = null;

  /**
   * ct.
   */
  public UeberweisungDuplicate()
  {
    super();
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Dupliziert die uebergebene Ueberweisung und oeffnet sie in einem neuen Dialog.
	 * Erwartet ein Objekt vom Typ <code>Ueberweisung</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null)
			throw new ApplicationException(i18n.tr("Keine Überweisung angegeben"));

		try {
			Ueberweisung u = (Ueberweisung) context;
			GUI.startView(UeberweisungNeu.class.getName(),u.duplicate());
		}
		catch (RemoteException e)
		{
			Logger.error("error while duplicating ueberweisung",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Duplizieren der Überweisung"));
		}
  }

}


/**********************************************************************
 * $Log: UeberweisungDuplicate.java,v $
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 **********************************************************************/