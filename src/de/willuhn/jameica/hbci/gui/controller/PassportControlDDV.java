/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/PassportControlDDV.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/02/20 01:36:56 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.CheckboxInput;
import de.willuhn.jameica.gui.parts.Input;
import de.willuhn.jameica.gui.parts.SelectInput;
import de.willuhn.jameica.gui.parts.TextInput;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.gui.views.KontoListe;
import de.willuhn.jameica.hbci.gui.views.KontoNeu;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.PassportDDV;
import de.willuhn.util.I18N;

/**
 * Controller, der die Eingaben zur Konfiguration des Passports handelt.
 */
public class PassportControlDDV extends AbstractControl {

	// Fachobjekte
	private Konto konto 				 = null;
	private PassportDDV passport = null;

	// Eingabe-Felder
	private Input port 			 = null;
	private Input ctNumber	 = null;
	private Input entryIndex = null;

	private CheckboxInput useBio 			= null;
	private CheckboxInput useSoftPin 	= null; 
	
	private boolean stored = false;

  /**
   * ct.
   * @param view
   */
  public PassportControlDDV(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert das Konto.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException
	{
		if (konto != null)
			return konto;

		konto = (Konto) getCurrentObject();
		if (konto == null)
		{
			GUI.setActionText(I18N.tr("Ausgewähltes Konto wurde nicht gefunden"));
			throw new RemoteException("konto not found");
		}
		return konto;
	}

	/**
	 * Liefert den Passport.
   * @return Passport.
   * @throws RemoteException
   */
  public PassportDDV getPassport() throws RemoteException
	{
		if (passport != null)
			return passport;
		
		passport = (PassportDDV) getKonto().getPassport();

		if (passport == null)
		{
			GUI.setActionText(I18N.tr("Ausgewähltes Sicherheitsmedium wurde nicht gefunden"));
			throw new RemoteException("passport not found");
		}
		return passport;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Port.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getPort() throws RemoteException
	{
		if (port != null)
			return port;

		port = new SelectInput(PassportDDV.PORTS,""+PassportDDV.PORTS[getPassport().getPort()]);
		port.addComment(I18N.tr("meist COM1 oder COM2"),null);
		return port;
	}		

	/**
	 * Liefert das Eingabe-Feld fuer die Nummer des Lesers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getCTNumber() throws RemoteException
	{
		if (ctNumber != null)
			return ctNumber;

		ctNumber = new TextInput(""+getPassport().getCTNumber());
		ctNumber.addComment(I18N.tr("meist 0"),null);
		return ctNumber;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die Index-Nummer des HBCI-Zugangs.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEntryIndex() throws RemoteException
	{
		if (entryIndex != null)
			return entryIndex;

		entryIndex = new TextInput(""+getPassport().getEntryIndex());
		entryIndex.addComment(I18N.tr("meist 1"),null);
		return entryIndex;
	}

	/**
	 * Liefert die Checkbox fuer die Auswahl biometrischer Verfahren.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getBio() throws RemoteException
	{
		if (useBio != null)
			return useBio;

		useBio = new CheckboxInput(getPassport().useBIO());
		return useBio;
	}

	/**
	 * Liefert die Checkbox fuer die Auswahl der Tastatur als PIN-Eingabe.
	 * @return Checkbox.
	 * @throws RemoteException
	 */
	public CheckboxInput getSoftPin() throws RemoteException
	{
		if (useSoftPin != null)
			return useSoftPin;

		useSoftPin = new CheckboxInput(getPassport().useSoftPin());
		return useSoftPin;
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
			Application.getLog().error("error while handleCancel",e);
			GUI.startView(KontoListe.class.getName(),null);
		}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {

		stored = false;	
			
  	try {
			try {
				getPassport().setCTNumber(Integer.parseInt(getCTNumber().getValue()));
			}
			catch (NumberFormatException e)
			{
				GUI.setActionText(I18N.tr("Bitte geben Sie im Feld \"Nummer des Chipkartenlesers\" eine gültige Zahl ein."));
				return;
			}

			try {
				getPassport().setEntryIndex(Integer.parseInt(getEntryIndex().getValue()));
			}
			catch (NumberFormatException e)
			{
				GUI.setActionText(I18N.tr("Bitte geben Sie im Feld \"Index des HBCI-Zugangs\" eine gültige Zahl ein."));
				return;
			}

			for (int i=0;i<PassportDDV.PORTS.length;++i)
			{
				if (PassportDDV.PORTS[i].equals(getPort().getValue()))
					getPassport().setPort(i);
			}

			getPassport().setBIO(CheckboxInput.ENABLED.equals(getBio().getValue()));
			getPassport().setSoftPin(CheckboxInput.ENABLED.equals(getSoftPin().getValue()));
			
			getPassport().store();

			GUI.setActionText(I18N.tr("Einstellungen gespeichert"));
			stored = true;
  	}
  	catch (Exception e)
  	{
  		Application.getLog().error("error while storing params",e);
  		GUI.setActionText(I18N.tr("Fehler beim Speichern der Einstellungen"));
  	}
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleLoad(java.lang.String)
   */
  public void handleLoad(String id) {
  }

	/**
   * Testet die Einstellungen.
   */
  public void handleTest()
	{

		GUI.setActionText(I18N.tr("Teste Chipkartenleser..."));

		GUI.startJob(new Runnable() {
      public void run() {
				handleStore();
				if (!stored)
					return;

				try {
					getPassport().open();
					getPassport().close();
					GUI.setActionText(I18N.tr("Chipkartenleser erfolgreich getestet."));
				}
				catch (RemoteException e)
				{
					GUI.setActionText(I18N.tr("Fehler beim Testen des Chipkartenlesers."));
					Application.getLog().debug("error while testing chipcard reader: " + e.getLocalizedMessage());
				}
      }
    });
	}
}


/**********************************************************************
 * $Log: PassportControlDDV.java,v $
 * Revision 1.4  2004/02/20 01:36:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/13 00:41:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/