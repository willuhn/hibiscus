/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/LastschriftControl.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/02/27 17:11:49 $
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
import java.util.Date;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.menus.LastschriftList;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.Transfer;
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
  public Transfer getTransfer() throws RemoteException
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
  public Part getLastschriftListe() throws RemoteException
  {
		if (table != null)
			return table;

		DBIterator list = Settings.getDBService().createList(Lastschrift.class);

		table = new TablePart(list,new LastschriftNew());
		table.setFormatter(new TableFormatter() {
			public void format(TableItem item) {
        Lastschrift l = (Lastschrift) item.getData();
				if (l == null)
					return;

				try {
					if (l.getTermin().before(new Date()) && !l.ausgefuehrt())
					{
						item.setForeground(Settings.getUeberfaelligForeground());
					}
				}
				catch (RemoteException e) { /*ignore */}
			}
		});
		table.addColumn(i18n.tr("Empfänger-Konto"),"konto_id");
		table.addColumn(i18n.tr("Zahlungspflichtiger"),"empfaenger_name");
		table.addColumn(i18n.tr("Belastetes Konto"),"empfaenger_konto");
		table.addColumn(i18n.tr("Verwendungszweck"),"zweck");
		table.addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
		table.addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.LONGDATEFORMAT));
		table.addColumn(i18n.tr("Status"),"ausgefuehrt",new Formatter() {
			public String format(Object o) {
				try {
					int i = ((Integer) o).intValue();
					return i == 1 ? i18n.tr("ausgeführt") : i18n.tr("offen");
				}
				catch (Exception e) {}
				return ""+o;
			}
		});
	
		table.setContextMenu(new LastschriftList());
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
     * @param name
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