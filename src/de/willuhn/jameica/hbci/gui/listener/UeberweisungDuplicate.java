/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/listener/Attic/UeberweisungDuplicate.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/09 00:04:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.listener;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Listener, der zur Duplizierung einer Ueberweisung ausgefuehrt werden kann.
 * Er erwartet ein Objekt vom Typ <code>Ueberweisung</code> im <code>data</code>-Member
 * des Events.
 */
public class UeberweisungDuplicate implements Listener
{

	private I18N i18n = null;

  /**
   * ct.
   */
  public UeberweisungDuplicate()
  {
    super();
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Dupliziert die uebergebene Ueberweisung und oeffnet sie in einem neuen Dialog.
	 * Erwartet ein Objekt vom Typ <code>Ueberweisung</code> im <code>data</code>-Member
	 * des Events.
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent(Event event)
  {
		try {
			Ueberweisung u = (Ueberweisung) event.data;
			if (u == null)
				return;
			GUI.startView(UeberweisungNeu.class.getName(),u.duplicate());
		}
		catch (Exception e)
		{
			Logger.error("error while duplicating ueberweisung",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Duplizieren der Überweisung"));
		}
  }

}


/**********************************************************************
 * $Log: UeberweisungDuplicate.java,v $
 * Revision 1.1  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 **********************************************************************/