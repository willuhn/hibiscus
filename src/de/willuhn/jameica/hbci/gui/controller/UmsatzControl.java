/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzControl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/03/06 18:25:10 $
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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.DateFormatter;
import de.willuhn.jameica.gui.parts.Table;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.views.KontoNeu;
import de.willuhn.jameica.hbci.gui.views.UmsatzListe;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller, der fuer die Umsaetze eines Kontos zustaendig ist.
 */
public class UmsatzControl extends AbstractControl {

	private I18N i18n;

	// Fach-Objekte
	private Konto konto = null;

  /**
   * ct.
   * @param view
   */
  public UmsatzControl(AbstractView view) {
    super(view);
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
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
		konto = (Konto) getCurrentObject();
		if (konto == null || konto.isNewObject())
			throw new RemoteException("konto cannot be null or new");
		return konto;
	}

	/**
	 * Liefert eine Tabelle mit allen Umsaetzen des Kontos.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Table getUmsatzListe() throws RemoteException
	{
		Table table = new Table(getKonto().getUmsaetze(),this);
		table.addColumn(i18n.tr("Empfänger"),"empfaenger_name");
		table.addColumn(i18n.tr("Betrag"),"betrag",
			new CurrencyFormatter(getKonto().getWaehrung(),HBCI.DECIMALFORMAT));
		table.addColumn(i18n.tr("Verwendungszweck"),"zweck");
		table.addColumn(i18n.tr("Datum"),"datum",new DateFormatter(HBCI.DATEFORMAT));
		table.addColumn(i18n.tr("Valuta"),"valuta",new DateFormatter(HBCI.DATEFORMAT));
		return table;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
  	try {
			GUI.startView(KontoNeu.class.getName(),getKonto());
  	}
  	catch (RemoteException e)
  	{
  		Application.getLog().error("error while loading konto view",e);
  		GUI.setActionText(i18n.tr("Fehler beim Laden des Kontos"));
  	}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
  }

	/**
   * Holt die Umsaetze vom HBCI-Server und zeigt sie an. 
   */
  public void handleGetUmsaetze()
	{
		GUI.startProgress();

		GUI.startSync(new Runnable() {
			public void run() {
				try {
					GUI.setActionText(i18n.tr("Umsätze werden abgerufen..."));
					getKonto().refreshUmsaetze();
					// Jetzt aktualisieren wir die GUI, indem wir uns selbst neu laden ;)
					GUI.startView(UmsatzListe.class.getName(),getKonto());
					GUI.setActionText(i18n.tr("...Umsätze erfolgreich übertragen"));
				}
				catch (ApplicationException e2)
				{
					GUI.setErrorText(e2.getLocalizedMessage());
				}
				catch (Exception e)
				{
					Application.getLog().error("error while reading saldo",e);
					GUI.setErrorText(i18n.tr("Fehler beim Abrufen der Umsätze."));
				}
			}
		});

		GUI.stopProgress();
	}
}


/**********************************************************************
 * $Log: UmsatzControl.java,v $
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