/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzControl.java,v $
 * $Revision: 1.14 $
 * $Date: 2004/06/30 20:58:28 $
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

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.KontoNeu;
import de.willuhn.jameica.hbci.gui.views.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.views.UmsatzListe;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Controller, der fuer die Umsatz-Liste eines Kontos zustaendig ist.
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
  public TablePart getUmsatzListe() throws RemoteException
	{
		TablePart table = new TablePart(getKonto().getUmsaetze(),this);
		table.setFormatter(new TableFormatter() {
      public void format(TableItem item) {
      	Umsatz u = (Umsatz) item.getData();
      	if (u == null) return;
				try {
					if (u.getBetrag() < 0.0)
					{
						item.setBackground(Settings.getBuchungSollBackground());
						item.setForeground(Settings.getBuchungSollForeground());
					}
					else
					{
						item.setBackground(Settings.getBuchungHabenBackground());
						item.setForeground(Settings.getBuchungHabenForeground());
					}
				}
				catch (RemoteException e)
				{
				}
      }
    });
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
  		Logger.error("error while loading konto view",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden des Kontos"));
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
    GUI.startView(UmsatzDetail.class.getName(),o);
  }

	/**
   * Holt die Umsaetze vom HBCI-Server und zeigt sie an. 
   */
  public synchronized void handleGetUmsaetze()
	{
		GUI.getStatusBar().startProgress();

		GUI.startSync(new Runnable() {
			public void run() {
				try {
					GUI.getStatusBar().setSuccessText(i18n.tr("Umsätze werden abgerufen..."));
					getKonto().refreshUmsaetze();
					// Jetzt aktualisieren wir die GUI, indem wir uns selbst neu laden ;)
					GUI.startView(UmsatzListe.class.getName(),getKonto());
					GUI.getStatusBar().setSuccessText(i18n.tr("...Umsätze erfolgreich übertragen"));
				}
				catch (ApplicationException e2)
				{
					GUI.getView().setErrorText(i18n.tr(e2.getMessage()));
				}
				catch (Throwable t)
				{
					Logger.error("error while reading saldo",t);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Abrufen der Umsätze."));
				}
			}
		});

		GUI.getStatusBar().stopProgress();
	}

	/**
   * Loescht alle Umsaetze aus der Liste.
   */
  public void handleDeleteUmsaetze()
	{

		YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
		d.setTitle(i18n.tr("Sicher?"));
		d.setText(i18n.tr("Sind Sie sicher, daß Sie alle Umsätze des Kontos löschen möchten?"));

		try {
			if (!((Boolean)d.open()).booleanValue())
				return;
		}
		catch (Exception e)
		{
			Logger.error("error while deleting umsaetze",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Umsätze"));
			return;
		}


		GUI.getStatusBar().startProgress();

		GUI.startSync(new Runnable() {
			public void run() {
				try {
					getKonto().deleteUmsaetze();
					GUI.getStatusBar().setSuccessText(i18n.tr("Umsätze gelöscht."));
					// View reloaden
					GUI.startView(UmsatzListe.class.getName(),getKonto());
				}
				catch (ApplicationException e2)
				{
					GUI.getView().setErrorText(i18n.tr(e2.getMessage()));
				}
				catch (Exception e)
				{
					Logger.error("error while deleting umsaetze",e);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Umsätze."));
				}
			}
		});

		GUI.getStatusBar().stopProgress();
	}
}


/**********************************************************************
 * $Log: UmsatzControl.java,v $
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