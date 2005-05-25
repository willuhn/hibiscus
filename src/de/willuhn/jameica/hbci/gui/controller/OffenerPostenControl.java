/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/OffenerPostenControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/25 00:42:04 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.OffenerPostenNew;
import de.willuhn.jameica.hbci.rmi.OffenerPosten;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Controller fuer die OP-Verwaltung.
 */
public class OffenerPostenControl extends AbstractControl {

	// Fach-Objekte
	private OffenerPosten posten = null;

  private Part list         = null;

	private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @param view
   */
  public OffenerPostenControl(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert den aktuellen offenen Posten.
	 * Existiert er nicht, wird ein neuer erzeugt.
   * @return der offene Posten.
   * @throws RemoteException
   */
  public OffenerPosten getOffenerPosten() throws RemoteException
	{
		if (posten != null)
			return posten;
		
		posten = (OffenerPosten) getCurrentObject();
		if (posten != null)
			return posten;

		posten = (OffenerPosten) Settings.getDBService().createObject(OffenerPosten.class,null);
		return posten;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen offenen Posten.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getOPListe() throws RemoteException
	{
    if (list != null)
      return list;
    list = new de.willuhn.jameica.hbci.gui.parts.OffenerPostenList(new OffenerPostenNew());
    return list;
	}
}


/**********************************************************************
 * $Log: OffenerPostenControl.java,v $
 * Revision 1.1  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 **********************************************************************/