/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/UeberweisungDelete.java,v $
 * $Revision: 1.8 $
 * $Date: 2005/02/04 18:27:54 $
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
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen einer Ueberweisung.
 */
public class UeberweisungDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Ueberweisung</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Ueberweisung))
			throw new ApplicationException(i18n.tr("Keine Überweisung ausgewählt"));

		try {

			Ueberweisung u = (Ueberweisung) context;
			if (u.isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Überweisung löschen"));
			d.setText(i18n.tr("Wollen Sie diese Überweisung wirklich löschen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting ueberweisung",e);
				return;
			}

			// ok, wir loeschen das Objekt
			u.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Überweisung gelöscht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Überweisung."));
			Logger.error("unable to delete ueberweisung",e);
		}
  }

}


/**********************************************************************
 * $Log: UeberweisungDelete.java,v $
 * Revision 1.8  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.7  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.5  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.3  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/21 14:05:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/