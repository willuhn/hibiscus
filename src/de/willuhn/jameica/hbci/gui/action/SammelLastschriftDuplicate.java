/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/SammelLastschriftDuplicate.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/08/22 10:36:38 $
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
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Duplizierung einer Sammel-Lastschrift ausgefuehrt werden kann.
 * Er erwartet ein Objekt vom Typ <code>Sammel-Lastschrift</code> als Context.
 */
public class SammelLastschriftDuplicate implements Action
{
  // BUGZILLA 115 http://www.willuhn.de/bugzilla/show_bug.cgi?id=115

	private I18N i18n = null;

  /**
   * ct.
   */
  public SammelLastschriftDuplicate()
  {
    super();
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Dupliziert die uebergebene Sammel-Lastschrift und oeffnet sie in einem neuen Dialog.
	 * Erwartet ein Objekt vom Typ <code>SammelLastschrift</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null)
			throw new ApplicationException(i18n.tr("Keine Sammel-Lastschrift angegeben"));

		try {
			SammelLastschrift l = (SammelLastschrift) context;
			GUI.startView(GUI.getCurrentView().getClass(),l.duplicate());
      GUI.getStatusBar().setSuccessText(i18n.tr("Sammel-Lastschrift dupliziert"));
		}
		catch (RemoteException e)
		{
			Logger.error("error while duplicating lastschrift",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Duplizieren der Sammel-Lastschrift"));
		}
  }

}


/**********************************************************************
 * $Log: SammelLastschriftDuplicate.java,v $
 * Revision 1.1  2005/08/22 10:36:38  willuhn
 * @N bug 115, 116
 *
 * Revision 1.4  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.3  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
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