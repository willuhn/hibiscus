/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/LastschriftControl.java,v $
 * $Revision: 1.10 $
 * $Date: 2007/04/23 18:07:15 $
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

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.logging.Logger;

/**
 * Ueberschreiben wir von UeberweisungControl, weil es fast das
 * gleiche ist.
 */
public class LastschriftControl extends AbstractBaseUeberweisungControl
{

	private TablePart table = null;
  private Lastschrift transfer = null;
  private SelectInput type = null;
	
  /**
   * ct.
   * @param view
   */
  public LastschriftControl(AbstractView view)
  {
    super(view);
  }


  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getTransfer()
   */
  public HibiscusTransfer getTransfer() throws RemoteException
  {
    if (transfer != null)
      return transfer;

    transfer = (Lastschrift) getCurrentObject();
    if (transfer != null)
      return transfer;
      
    transfer = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
    return transfer;
  }

	/**
	 * Liefert eine Select-Box, ueber die der Typ der Lastschrift ausgewaehlt werden kann.
   * @return Typ der Lastschrift.
   * @throws RemoteException
   */
  public Input getTyp() throws RemoteException
	{
		// BUGZILLA #8 http://www.willuhn.de/bugzilla/show_bug.cgi?id=8
		if (type != null)
			return type;

		GenericObject[] types = new GenericObject[]
		{
			new TypeObject("05"),
			new TypeObject("04")
		};
		type = new SelectInput(PseudoIterator.fromArray(types),new TypeObject(((Lastschrift)getTransfer()).getTyp()));
		if (((Terminable)getTransfer()).ausgefuehrt())
			type.disable();
		return type;
	}
	
  /**
   * Liefert eine Liste existierender Lastschriften.
   * @return Liste der Lastschriften.
   * @throws RemoteException
   */
  public TablePart getLastschriftListe() throws RemoteException
  {
		if (table != null)
			return table;

    table = new de.willuhn.jameica.hbci.gui.parts.LastschriftList(new LastschriftNew());
		return table;
  }


	/**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#handleStore()
   */
  public synchronized boolean handleStore()
	{
		try
		{
			Lastschrift l = (Lastschrift) getTransfer();
			
			if (l.ausgefuehrt())
			{
				GUI.getStatusBar().setErrorText(i18n.tr("Der Auftrag wurde bereits ausgeführt und kann daher nicht geändert werden"));
				return false;
			}

			TypeObject to = (TypeObject) getTyp().getValue();
			l.setTyp(to == null ? null : to.getID());
			return super.handleStore();
		}
		catch (RemoteException re)
		{
			Logger.error("rollback failed",re);
			Logger.error("error while storing lastschrift",re);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Auftrags"));
		}
		return false;
	}


	/**
	 * Hilfsobjekt fuer die Lastschrift-Typen.
   */
  private class TypeObject implements GenericObject
	{

		private String id;

		/**
		 * ct.
     * @param id
     */
    private TypeObject(String id)
		{
			this.id = id;
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
    	if ("04".equals(id))
    		return i18n.tr("[04] Abbuchungsverfahren");
			if ("05".equals(id))
				return i18n.tr("[05] Einzugsermächtigung");
			return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return id;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
    	if (arg0 == null)
	      return false;
	    return getID().equals(arg0.getID());
    }
	}
}


/**********************************************************************
 * $Log: LastschriftControl.java,v $
 * Revision 1.10  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.9  2006/06/07 17:26:40  willuhn
 * @N DTAUS-Import fuer Lastschriften
 * @B Satusbar-Update in DTAUSImport gefixt
 *
 * Revision 1.8  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.7  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.6  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.5  2005/02/19 17:22:05  willuhn
 * @B Bug 8
 *
 * Revision 1.4  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.3  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/01/19 00:33:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 **********************************************************************/