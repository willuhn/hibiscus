/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/DauerauftragChange.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/24 17:19:02 $
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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action zur Aenderung eines Dauerauftrag.
 */
public class DauerauftragChange implements Action
{

  /**
   * Erwartet einen Dauerauftrag als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		Dauerauftrag d = null;

		if (context == null || !(context instanceof Dauerauftrag))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Dauerauftrag aus"));

		d = (Dauerauftrag) context;
		
		try
		{
			if (!d.isActive())
				throw new ApplicationException(i18n.tr("Dauerauftrag liegt der Bank noch nicht vor und kann daher dort nicht geändert werden."));

			// TODO Hier gehts weiter
			GUI.startView(de.willuhn.jameica.hbci.gui.views.DauerauftragNeu.class.getName(),d);

		}
		catch (RemoteException e)
		{
			Logger.error("error while executing dauerauftragChange",e);
			throw new ApplicationException(i18n.tr("Fehler beim Ändern des Dauerauftrags."));
		}


  }

}


/**********************************************************************
 * $Log: DauerauftragChange.java,v $
 * Revision 1.1  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 **********************************************************************/