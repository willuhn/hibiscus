/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Markieren von System-Nachrichten als gelesen.
 */
public class NachrichtMarkRead implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Nachricht</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Keine System-Nachricht ausgewählt"));

    if (!(context instanceof Nachricht) && !(context instanceof Nachricht[]))
			throw new ApplicationException(i18n.tr("Keine System-Nachricht ausgewählt"));

    boolean array = (context instanceof Nachricht[]);

    Nachricht[] list = null;
    if (array)
      list = (Nachricht[]) context;
    else
      list = new Nachricht[]{(Nachricht)context}; // Array mit einem Element

		try {
      for (final Nachricht nachricht : list)
      {
        if (nachricht.isNewObject())
          continue; // muss nicht geloescht werden

        // ok, wir loeschen das Objekt
        nachricht.setGelesen(true);
        nachricht.store();
      }
      if (array)
        GUI.getStatusBar().setSuccessText(i18n.tr("{0} System-Nachrichten als gelesen markiert.",""+list.length));
      else
        GUI.getStatusBar().setSuccessText(i18n.tr("System-Nachricht als gelesen markiert."));

      // Reload view
      GUI.startView(GUI.getCurrentView().getClass(),null);
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Markieren der System-Nachrichten als gelesen."));
			Logger.error("unable to mark system message read",e);
		}
  }

}


/**********************************************************************
 * $Log: NachrichtMarkRead.java,v $
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/