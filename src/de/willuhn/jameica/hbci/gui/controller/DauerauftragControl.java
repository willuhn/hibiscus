/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/DauerauftragControl.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/10/08 13:37:47 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.DauerauftragNeu;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.util.Logger;

/**
 * Controller fuer Dauer-Auftraege.
 */
public class DauerauftragControl extends AbstractTransferControl {

  /**
   * ct.
   * @param view
   */
  public DauerauftragControl(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert den Dauerauftrag oder erzeugt bei Bedarf eine neue.
   * @return der Dauerauftrag.
   * @throws RemoteException
   */
  public Dauerauftrag getDauerauftrag() throws RemoteException
	{
		if (getTransfer() != null)
			return (Dauerauftrag) getTransfer();
		
		transfer = (Dauerauftrag) Settings.getDBService().createObject(Dauerauftrag.class,null);
		return (Dauerauftrag) transfer;
	}

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Dauerauftraegen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public Part getDauerauftragListe() throws RemoteException
	{
		DBIterator list = Settings.getDBService().createList(Dauerauftrag.class);

		TablePart table = new TablePart(list,this);
		// table.addMenu(i18n.tr("Duplizieren"), new UeberweisungDuplicate()); TODO

		table.addColumn(i18n.tr("Konto"),"konto_id");
		table.addColumn(i18n.tr("Kto. des Empfängers"),"empfaenger_konto");
		table.addColumn(i18n.tr("BLZ des Empfängers"),"empfaenger_blz");
		table.addColumn(i18n.tr("Name des Empfängers"),"empfaenger_name");
		table.addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
		table.addColumn(i18n.tr("aktiv"),"aktiv",new Formatter()
    {
      public String format(Object o)
      {
      	if (o == null)
      		return "nein";
      	try {
      		int i = ((Integer) o).intValue();
      		return i == 0 ? i18n.tr("nein") : i18n.tr("ja");
      	}
      	catch (Exception e)
      	{
      		Logger.error("error while formatting attribute",e);
      		return "unbekannt";
      	}
      }
    });
		table.addColumn(i18n.tr("Turnus"),"turnus_id");
		return table;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public synchronized void handleStore()
  {
		super.handleStore();
		// TODO: Turnus
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
		GUI.startView(DauerauftragNeu.class.getName(),null);
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
		GUI.startView(DauerauftragNeu.class.getName(),o);
  }
}


/**********************************************************************
 * $Log: DauerauftragControl.java,v $
 * Revision 1.5  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.3  2004/07/20 00:11:07  willuhn
 * @C Code sharing zwischen Ueberweisung und Dauerauftrag
 *
 * Revision 1.2  2004/07/16 00:07:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/