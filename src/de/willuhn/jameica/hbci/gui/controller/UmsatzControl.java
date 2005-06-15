/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzControl.java,v $
 * $Revision: 1.25 $
 * $Date: 2005/06/15 16:10:48 $
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
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Controller, der fuer die Umsatz-Liste eines Kontos zustaendig ist.
 */
public class UmsatzControl extends AbstractControl {

	// Fach-Objekte
	private Konto konto = null;

	/**
   * ct.
   * @param view
   */
  public UmsatzControl(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert das Konto, auf das sich diese Umsaetze beziehen.
   * @return das Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException
	{
		if (konto != null)
			return konto;
		Object o = getCurrentObject();
		if (o instanceof Umsatz)
			konto = ((Umsatz)o).getKonto();
		else
			konto = (Konto)o;
		if (konto == null || konto.isNewObject())
			throw new RemoteException("konto cannot be null or new");
		return konto;
	}

	/**
	 * Liefert eine Tabelle mit allen Umsaetzen des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getUmsatzListe() throws RemoteException
	{
    return new de.willuhn.jameica.hbci.gui.parts.UmsatzList(getKonto(),new UmsatzDetail());
	}

}


/**********************************************************************
 * $Log: UmsatzControl.java,v $
 * Revision 1.25  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.24  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.23  2005/03/21 23:09:34  web0
 * @B bug 23
 *
 * Revision 1.22  2004/10/25 23:22:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.20  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.19  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.17  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.15  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.14  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/05/02 17:04:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/04/19 22:53:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/19 22:05:52  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.9  2004/04/13 23:14:22  willuhn
 * @N datadir
 *
 * Revision 1.8  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/04/04 18:30:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.3  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.2  2004/03/05 08:38:47  willuhn
 * @N umsaetze works now
 *
 * Revision 1.1  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 **********************************************************************/