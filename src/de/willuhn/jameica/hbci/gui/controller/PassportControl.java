/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/PassportControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/11 00:11:20 $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.CheckboxInput;
import de.willuhn.jameica.gui.parts.Input;
import de.willuhn.jameica.gui.parts.SelectInput;
import de.willuhn.jameica.gui.parts.TextInput;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.KontoListe;
import de.willuhn.jameica.hbci.gui.views.KontoNeu;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportParam;
import de.willuhn.util.I18N;

/**
 * Controller, der die Eingaben zur Konfiguration des Passports handelt.
 */
public class PassportControl extends AbstractControl {

	// Fachobjekte
	private Konto konto 			= null;
	private Passport passport = null;
	private HashMap values		= null;
	private HashMap fields    = new HashMap();

	// Eingabe-Felder
	private LinkedHashMap labelpairs = null;
	private LinkedHashMap checkboxes = null;


  /**
   * ct.
   * @param view
   */
  public PassportControl(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert das Konto, fuer das die Passport-Einstellungen konfiguriert werden sollen.
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
	 * Liefert den zu konfigurierenden Passport.
   * @return Passport.
   * @throws RemoteException
   */
  public Passport getPassport() throws RemoteException
	{
		if (passport != null)
			return passport;

		passport = getKonto().getPassport();
		return passport;
	}

  /**
	 * Liefert eine Liste mit Name-Wert-Paaren der PassportParams.
   * @return Liste mit Namen und Werten der Params.
   * @throws RemoteException
   */
  private HashMap getParamValues() throws RemoteException
	{
		if (values != null)
			return values;

		HashMap values = new HashMap();
		
		// Wir holen uns eine Liste mit allen Params und lesen die Werte aus.
		DBIterator list = getKonto().getPassportParams();
		PassportParam p = null;
		while (list.hasNext())
		{
			p = (PassportParam) list.next();
			values.put(p.getName(),p.getValue());
		}

		return values;
	}

	/**
	 * Liefert eine Hashtable mit folgenden Schluessel-Wertepaaren:
	 * key   = Name der Eingabe-Felder als String.
	 * value = die zugehoerigen Eingabefelder als <code>Input</code>.
	 * Sie sind zu Anzeige als "Labelpair" gedacht. Also links Name, rechts Eingabefeld.
   * @return Hashtable mit Eingabefeldern.
   * @throws RemoteException
   */
  public LinkedHashMap getParamLabelPairs() throws RemoteException
	{
		if (labelpairs != null)
			return labelpairs;

		labelpairs = new LinkedHashMap();
		HashMap v = getParamValues();
		
		switch (getPassport().getType())
		{
			case Passport.TYPE_DDV:
				Input port = new SelectInput(PassportParam.DDV_PORTS,(String) v.get(PassportParam.DDV_PORT));
				labelpairs.put("Port",port);
				fields.put(PassportParam.DDV_PORT,port);

				Input ctnumber = new TextInput((String)v.get(PassportParam.DDV_CTNUMBER));
				fields.put(PassportParam.DDV_CTNUMBER,ctnumber);
				labelpairs.put("Nummer des Readers",ctnumber);

				Input idx = new TextInput((String)v.get(PassportParam.DDV_ENTRYIDX));
				fields.put(PassportParam.DDV_ENTRYIDX,idx);
				labelpairs.put("Index des HBCI-Zugangs",idx);

				break;
				
			default:
				throw new RemoteException("Unable to detect passport type");
		}

		return labelpairs;
	}

	/**
	 * Liefert eine Hashtable mit folgenden Schluessel-Wertepaaren:
	 * key   = Name der Eingabe-Felder als String.
	 * value = die zugehoerigen Eingabefelder als <code>Input</code>.
	 * Sie sind zu Anzeige als "Checkbox" gedacht. Also links Checkbox, rechts Text.
	 * @return Hashtable mit Eingabefeldern.
	 * @throws RemoteException
	 */
	public LinkedHashMap getParamCheckboxes() throws RemoteException
	{
		if (checkboxes != null)
			return checkboxes;

		checkboxes = new LinkedHashMap();
		HashMap v = getParamValues();
		
		switch (getPassport().getType())
		{
			case Passport.TYPE_DDV:
				String bio 	= (String) v.get(PassportParam.DDV_USEBIO);
				Input b = new CheckboxInput(bio != null && CheckboxInput.ENABLED.equals(bio));
				fields.put(PassportParam.DDV_USEBIO,b);
				checkboxes.put("Biometrie verwenden",b);

				String soft = (String) v.get(PassportParam.DDV_SOFTPIN);
				Input s = new CheckboxInput(soft != null && CheckboxInput.ENABLED.equals(soft));
				fields.put(PassportParam.DDV_SOFTPIN,s);
				checkboxes.put("PIN-Eingabe über Tastatur",s);

				break;
				
			default:
				throw new RemoteException("Unable to detect passport type");
		}

		return checkboxes;
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
  	try {

			DBIterator list = getKonto().getPassportParams();
			PassportParam p = null;
			while (list.hasNext())
			{
				p = (PassportParam) list.next();
				p.delete();				
			}
			Iterator i = fields.keySet().iterator();
			while (i.hasNext())
			{
				String name = (String) i.next();
				Input input = (Input) fields.get(name);
				PassportParam pp = (PassportParam) Settings.getDatabase().createObject(PassportParam.class,null);
				pp.setKonto(getKonto());
				pp.setName(name);
				pp.setValue(input.getValue());
				pp.store();
			}
			GUI.setActionText(I18N.tr("Einstellungen gespeichert"));
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

}


/**********************************************************************
 * $Log: PassportControl.java,v $
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/