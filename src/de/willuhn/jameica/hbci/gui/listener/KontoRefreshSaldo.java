/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/listener/Attic/KontoRefreshSaldo.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/07/21 23:54:30 $
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

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Listener, der den Saldo eines Kontos aktualisiert.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> im <code>data</code>-Member
 * des Events.
 */
public class KontoRefreshSaldo implements Listener
{

	private I18N i18n = null;

  /**
   * ct.
   */
  public KontoRefreshSaldo()
  {
    super();
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> im <code>data</code>-Member
	 * des Events.
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent(Event event)
  {

		try {
			Konto k = (Konto) event.data;
			if (k == null)
				return;
			k.refreshSaldo();
		}
		catch (ApplicationException e)
		{
			GUI.getStatusBar().setErrorText(e.getMessage());
		}
		catch (Exception e2)
		{
			Logger.error("error while refreshing saldo",e2);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Aktualisieren des Saldos"));
		}
  }

}


/**********************************************************************
 * $Log: KontoRefreshSaldo.java,v $
 * Revision 1.2  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 **********************************************************************/