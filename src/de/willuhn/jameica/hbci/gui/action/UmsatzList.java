/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzList.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/03/01 18:51:04 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer die Anzeige der Umsaetze eines Kontos.
 */
public class UmsatzList implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  	if (context == null || !(context instanceof Konto))
  		throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus."));

		Konto k = (Konto) context;
		
		try
		{
			if (k.isNewObject())
				k.store(); // wir speichern eigenmaechtig
		}
		catch (RemoteException e)
		{
			Logger.error("error while storing konto",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Kontos"));
		}
		GUI.startView(de.willuhn.jameica.hbci.gui.views.UmsatzList.class,k);
  }

}


/**********************************************************************
 * $Log: UmsatzList.java,v $
 * Revision 1.4  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.3  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.2  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.3  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.1  2004/10/12 23:48:39  willuhn
 * @N Actions
 *
 **********************************************************************/