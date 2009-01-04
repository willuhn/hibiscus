/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzDetailControl.java,v $
 * $Revision: 1.35 $
 * $Date: 2009/01/04 01:25:47 $
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

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzTypInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Addressbook;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Detailansicht eines Umsatzes.
 */
public class UmsatzDetailControl extends AbstractControl {

  I18N i18n = null;

  // Fachobjekte
  private Umsatz umsatz = null;
	
	// Eingabe-Felder
	private Input konto				 		= null;
	private Input empfaengerName  = null;
	private Input empfaengerKonto = null;
  private Input empfaengerBlz   = null;
	private Input betrag	  	    = null;
	private Input datum						= null;
	private Input valuta					= null;
	private Input zweck           = null;

	private LabelInput saldo		  = null;
	private Input primanota				= null;
	private Input art							= null;
	private Input customerRef			= null;
  
  private Input kommentar       = null;
  
  private SelectInput umsatzTyp = null;

  /**
   * ct.
   * @param view
   */
  public UmsatzDetailControl(AbstractView view) {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert das Umsatz-Objekt, das auf dem Detail-Dialog angezeigt wird.
   * @return das Umsatz-Objekt.
   */
  public Umsatz getUmsatz()
  {
    if (umsatz != null)
      return umsatz;
    umsatz = (Umsatz) getCurrentObject();
    return umsatz;
  }

  /**
   * Liefert ein Eingabe-Feld fuer einen zusaetzlichen Kommentar
   * @return liefert ein Eingabefeld fuer einen zusaetzlichen Kommentar.
   * @throws RemoteException
   */
  public Input getKommentar() throws RemoteException
  {
    if (this.kommentar != null)
      return this.kommentar;
    this.kommentar = new TextAreaInput(this.getUmsatz().getKommentar());
    return this.kommentar;
  }
  
  /**
   * Prueft, ob sich das Gegenkonto im Adressbuch befindet.
   * @return die ggf. gefundene Adresse oder null.
   * @throws RemoteException
   */
  public Address getAddressbookEntry() throws RemoteException
  {
    try
    {
      Umsatz u = getUmsatz();
      HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
      Addressbook ab = (Addressbook) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
      e.setBlz(u.getGegenkontoBLZ());
      e.setKontonummer(u.getGegenkontoNummer());
      e.setName(u.getGegenkontoName());
      return ab.contains(e);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to check, if address in addressbook");
    }
    return null;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Konto des Umsatzes.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getKonto() throws RemoteException
  {
    if (konto != null)
      return konto;
    Konto k = getUmsatz().getKonto();
    konto = new LabelInput(k.getKontonummer());
    String comment = k.getBezeichnung();
    String s = HBCIUtils.getNameForBLZ(k.getBLZ());
    if (s != null && s.length() > 0)
      comment += " [" + s + "]";
    konto.setComment(s);
    return konto;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Namen des Empfaengers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerName() throws RemoteException
  {
    if (this.empfaengerName == null)
    {
      this.empfaengerName = new AddressInput(getUmsatz().getGegenkontoName());
      this.empfaengerName.setEnabled(false);
    }
    return this.empfaengerName;
  }
  
  /**
   * Liefert eine Auswahlbox fuer die Umsatz-Kategorie.
   * @return Umsatz-Kategorie.
   * @throws RemoteException
   */
  public SelectInput getUmsatzTyp() throws RemoteException
  {
    if (this.umsatzTyp != null)
      return this.umsatzTyp;
    DBIterator list = Settings.getDBService().createList(UmsatzTyp.class);
    list.setOrder("ORDER BY nummer,name");
    Umsatz u = getUmsatz();
    UmsatzTyp ut = u == null ? null : u.getUmsatzTyp();
    if (ut == null)
    {
      int typ = UmsatzTyp.TYP_EGAL;
      if (u != null)
        typ = u.getBetrag() > 0 ? UmsatzTyp.TYP_EINNAHME : UmsatzTyp.TYP_AUSGABE;
      this.umsatzTyp = new UmsatzTypInput(list,typ);
    }
    else
    {
      this.umsatzTyp = new UmsatzTypInput(list,ut);
    }
    return this.umsatzTyp;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Konto des Empfaengers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerKonto() throws RemoteException
  {
    if (this.empfaengerKonto == null)
    {
      this.empfaengerKonto = new TextInput(getUmsatz().getGegenkontoNummer(),15);
      this.empfaengerKonto.setEnabled(false);
    }
    return this.empfaengerKonto;
  }
  
  /**
   * Liefert ein Eingabe-Feld mit der BLZ des Empfaengers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerBLZ() throws RemoteException
  {
    if (this.empfaengerBlz == null)
    {
      this.empfaengerBlz = new BLZInput(getUmsatz().getGegenkontoBLZ());
      this.empfaengerBlz.setEnabled(false);
    }
    return this.empfaengerBlz;
  }

  /**
   * Liefert ein Eingabe-Feld mit Betrag der Buchung,
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBetrag() throws RemoteException
  {
    if (this.betrag != null)
      return this.betrag;
    
    double s = getUmsatz().getBetrag();
    this.betrag = new DecimalInput(s,HBCI.DECIMALFORMAT);
    this.betrag.setComment(getUmsatz().getKonto().getWaehrung());
    this.betrag.setEnabled(false);
    return this.betrag;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Datum der Buchung.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getDatum() throws RemoteException
  {
    if (this.datum == null)
    {
      this.datum = new DateInput(getUmsatz().getDatum(),HBCI.DATEFORMAT);
      this.datum.setEnabled(false);
    }
    return this.datum;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Valuta der Buchung.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getValuta() throws RemoteException
  {
    if (this.valuta == null)
    {
      this.valuta = new DateInput(getUmsatz().getValuta(),HBCI.DATEFORMAT);
      this.valuta.setEnabled(false);
    }
    return this.valuta;
  }

	/**
	 * Liefert ein Eingabe-Feld mit dem Saldo nach der Buchung.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getSaldo() throws RemoteException
	{
		if (this.saldo != null)
			return this.saldo;
    
    double s = getUmsatz().getSaldo();
    this.saldo = new LabelInput(HBCI.DECIMALFORMAT.format(s));
    this.saldo.setComment(getUmsatz().getKonto().getWaehrung());
    return this.saldo;
	}

	/**
	 * Liefert ein Eingabe-Feld mit dem Primanota-Kennzeichen.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getPrimanota() throws RemoteException
	{
		if (this.primanota == null)
		{
      this.primanota = new TextInput(getUmsatz().getPrimanota());
      this.primanota.setEnabled(false);
		}
		return this.primanota;
	}

	/**
	 * Liefert ein Eingabe-Feld mit einem Text der Umsatz-Art.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getArt() throws RemoteException
	{
		if (this.art == null)
		{
      this.art = new TextInput(getUmsatz().getArt());
      this.art.setEnabled(false);
		}
		return this.art;
	}

	/**
	 * Liefert ein Eingabe-Feld mit der Kundenreferenz.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getCustomerRef() throws RemoteException
	{
		if (this.customerRef == null)
		{
      this.customerRef = new TextInput(getUmsatz().getCustomerRef());
      this.customerRef.setEnabled(false);
		}
		return this.customerRef;
	}
	
	/**
	 * Liefert ein Eingabe-Feld fuer den Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getZweck() throws RemoteException
	{
	  if (this.zweck == null)
	  {
	    StringBuffer sb = new StringBuffer();
	    String z1 = getUmsatz().getZweck();
	    if (z1 != null && z1.length() > 0)
	    {
        sb.append(z1);
        sb.append("\n");
	    }
      String z2 = getUmsatz().getZweck2();
      if (z2 != null && z2.length() > 0)
      {
        sb.append(z2);
        sb.append("\n");
      }
      String[] lines = getUmsatz().getWeitereVerwendungszwecke();
      for (int i=0;i<lines.length;++i)
      {
        if (lines[i] == null || lines[i].length() == 0)
          continue;
        sb.append(lines[i]);
        sb.append("\n");
      }
      
      this.zweck = new TextAreaInput(sb.toString());
      this.zweck.setEnabled(false);
	  }
	  return this.zweck;
	}

  /**
   * Speichert die editierbaren Properties.
   */
  public synchronized void handleStore()
  {

    Umsatz u = getUmsatz();
    try {
      
      u.setKommentar((String)getKommentar().getValue());
      u.setUmsatzTyp((UmsatzTyp)getUmsatzTyp().getValue());
      getUmsatz().store();
      GUI.getStatusBar().setSuccessText(i18n.tr("Umsatz gespeichert"));
    }
    catch (ApplicationException e2)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e2.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing umsatz",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Speichern des Umsatzes: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }
}


/**********************************************************************
 * $Log: UmsatzDetailControl.java,v $
 * Revision 1.35  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 * Revision 1.34  2008/11/17 23:29:59  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.33  2008/08/29 16:46:23  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.32  2007/12/14 17:06:36  willuhn
 * @B Bug 518
 *
 * Revision 1.31  2007/12/03 10:00:27  willuhn
 * @N Umsatz-Kategorien nach Name sortieren, wenn keine Nummer angegeben
 *
 * Revision 1.30  2007/06/15 11:20:32  willuhn
 * @N Saldo in Kontodetails via Messaging sofort aktualisieren
 * @N Mehr Details in den Namen der Synchronize-Jobs
 * @N Layout der Umsatzdetail-Anzeige ueberarbeitet
 *
 * Revision 1.29  2007/04/24 17:52:17  willuhn
 * @N Bereits in den Umsatzdetails erkennen, ob die Adresse im Adressbuch ist
 * @C Gross-Kleinschreibung in Adressbuch-Suche
 *
 * Revision 1.28  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.27  2007/04/09 22:45:12  willuhn
 * @N Bug 380
 *
 * Revision 1.26  2007/03/18 08:13:28  jost
 * Sortierte Anzeige der Umsatz-Kategorien.
 **********************************************************************/