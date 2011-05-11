/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoDelete.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/05/11 10:20:28 $
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen eines Kontos.
 */
public class KontoDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Konto))
			throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));

		try {

			Konto k = (Konto) context;
			if (k.isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Bankverbindung löschen"));
			d.setText(i18n.tr("Wollen Sie diese Bankverbindung wirklich löschen?\nHierbei werden auch alle Umsätze, Überweisungen und Daueraufträge des Kontos lokal gelöscht."));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
	    catch (OperationCanceledException oce)
	    {
	      Logger.info(oce.getMessage());
	      return;
	    }
			catch (Exception e)
			{
				Logger.error("error while deleting konto",e);
				return;
			}

			// ok, wir loeschen das Objekt
			k.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Bankverbindung gelöscht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Bankverbindung."));
			Logger.error("unable to delete konto",e);
		}
  }

}


/**********************************************************************
 * $Log: KontoDelete.java,v $
 * Revision 1.4  2011/05/11 10:20:28  willuhn
 * @N OCE fangen
 *
 * Revision 1.3  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/