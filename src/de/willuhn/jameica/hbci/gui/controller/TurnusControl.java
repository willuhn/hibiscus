/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/TurnusControl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/11/14 19:21:37 $
 * $Author: willuhn $
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
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 */
public class TurnusControl extends AbstractControl
{

	// Das Fachobjekt
	private Turnus turnus = null;

	private Input intervall		= null;
	private Input zeiteinheit	= null;
	private Input tag					= null;
	private Input comment			= null;

	private I18N i18n;

  /**
   * @param view
   */
  public TurnusControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert den Turnus.
   * @return Turnus.
   */
  private Turnus getTurnus() throws RemoteException
	{
		if (turnus != null)
			return turnus;
		turnus = (Turnus) getCurrentObject();
		
		if (turnus != null)
			return turnus;
		
		turnus = (Turnus) Settings.getDBService().createObject(Turnus.class,null);
		return turnus;
	}

	/**
	 * Liefert ein zusaetzliches Kommentar-Feld, welches anzeigt, ob der
	 * Turnus geaendert werden darf.
   * @return 
   * @throws RemoteException
   */
  public Input getComment() throws RemoteException
	{
		if (comment != null)
			return comment;

		if (getTurnus().isInitial())
			comment = new LabelInput(i18n.tr("Turnus kann nicht gelöscht werden, da er Bestandteil der Initialdaten ist."));
		else
			comment = new LabelInput("");

		return comment;
	}

	/**
	 * Speichert den Turnus.
	 */
	public void handleStore()
	{
	}
}


/**********************************************************************
 * $Log: TurnusControl.java,v $
 * Revision 1.2  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 **********************************************************************/