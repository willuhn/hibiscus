/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/EmpfaengerControl.java,v $
 * $Revision: 1.48 $
 * $Date: 2009/03/17 23:44:14 $
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
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungBuchungNew;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.parts.SammelTransferBuchungList;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Empfaenger-Adressen.
 */
public class EmpfaengerControl extends AbstractControl {

	// Fach-Objekte
	private Address address         = null;
	// Eingabe-Felder
	private TextInput kontonummer   = null;
	private TextInput blz					  = null;
	private Input name				      = null;

	private TextInput bic           = null;
	private TextInput iban          = null;
  private TextInput bank          = null;

	private Input kommentar         = null;

  private Part list               = null;
  private Part sammelList         = null;
  private Part sammelList2        = null;
  private Part umsatzList         = null;
  
	private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @param view
   */
  public EmpfaengerControl(AbstractView view) {
    super(view);
  }

	/**
	 * Liefert die Adresse.
	 * Existiert er nicht, wird ein neuer erzeugt.
   * @return die Adresse.
   * @throws RemoteException
   */
  public Address getAddress() throws RemoteException
	{
		if (address != null)
			return address;
		
    address = (Address) getCurrentObject();
		if (address != null)
			return address;

    address = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
		return address;
	}
  
  /**
   * Prueft, ob es sich bei der Adresse um eine Hibiscus-Adresse handelt und diese aenderbar ist.
   * @return true, wenn es eine Hibiscus-Adresse ist.
   * @throws RemoteException
   */
  public boolean isHibiscusAdresse() throws RemoteException
  {
    Address a = getAddress();
    return (a instanceof HibiscusAddress);
  }

	/**
	 * Liefert eine Tabelle mit allen vorhandenen Empfaengern.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getEmpfaengerListe() throws RemoteException
	{
    if (list != null)
      return list;
    list = new de.willuhn.jameica.hbci.gui.parts.EmpfaengerList(new EmpfaengerNew());
    return list;
	}

  // BUGZILLA 56 http://www.willuhn.de/bugzilla/show_bug.cgi?id=56
  /**
   * Liefert eine Liste von allen Umsaetzen an/von diese/dieser Adresse.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getUmsatzListe() throws RemoteException
  {
    if (this.umsatzList != null)
      return this.umsatzList;

    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("empfaenger_konto like ?",new Object[]{"%" + getAddress().getKontonummer()});
    list.addFilter("empfaenger_blz = ?",  new Object[]{getAddress().getBlz()});

    this.umsatzList = new UmsatzList(list,new UmsatzDetail());
    ((UmsatzList)this.umsatzList).setFilterVisible(false);
    return this.umsatzList;
  }

  // BUGZILLA 107 http://www.willuhn.de/bugzilla/show_bug.cgi?id=107
  /**
   * Liefert eine Liste von allen Sammel-Lastschrift-Buchungen, die von dieser
   * Adresse eingezogen wurden.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getSammelLastListe() throws RemoteException
  {
    if (this.sammelList != null)
      return this.sammelList;

    DBIterator list = Settings.getDBService().createList(SammelLastBuchung.class);
    list.addFilter("gegenkonto_nr like ?",  new Object[]{"%" + getAddress().getKontonummer()});
    list.addFilter("gegenkonto_blz = ?", new Object[]{getAddress().getBlz()});
    list.setOrder(" ORDER BY id DESC");

    this.sammelList = new SammelTransferBuchungList(list,new SammelLastBuchungNew());
    return this.sammelList;
  }

  /**
   * Liefert eine Liste von allen Sammel-Ueberweisung-Buchungen, die an diese
   * Adresse ueberwiesen wurden.
   * @return Tabelle.
   * @throws RemoteException
   */
  public Part getSammelUeberweisungListe() throws RemoteException
  {
    if (this.sammelList2 != null)
      return this.sammelList2;

    DBIterator list = Settings.getDBService().createList(SammelUeberweisungBuchung.class);
    list.addFilter("gegenkonto_nr like ?",  new Object[]{"%" + getAddress().getKontonummer()});
    list.addFilter("gegenkonto_blz = ?", new Object[]{getAddress().getBlz()});
    list.setOrder(" ORDER BY id DESC");

    this.sammelList2 = new SammelTransferBuchungList(list,new SammelUeberweisungBuchungNew());
    return this.sammelList2;
  }

  /**
	 * Liefert das Eingabe-Feld fuer die Kontonummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getKontonummer() throws RemoteException
	{
		if (kontonummer != null)
			return kontonummer;
		kontonummer = new TextInput(getAddress().getKontonummer(),HBCIProperties.HBCI_KTO_MAXLENGTH_SOFT);
    // BUGZILLA 280
    kontonummer.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    kontonummer.setEnabled(isHibiscusAdresse());
    return kontonummer;
	}

  /**
   * Liefert ein Eingabe-Feld fuer einen Kommentar.
   * @return Kommentar.
   * @throws RemoteException
   */
  public Input getKommentar() throws RemoteException
  {
    if (this.kommentar != null)
      return this.kommentar;
    this.kommentar = new TextAreaInput(getAddress().getKommentar());
    this.kommentar.setEnabled(isHibiscusAdresse());
    return this.kommentar;
  }
  
	/**
	 * Liefert das Eingabe-Feld fuer die BLZ.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getBlz() throws RemoteException
	{
		if (blz != null)
			return blz;
		blz = new BLZInput(getAddress().getBlz());
    blz.setEnabled(isHibiscusAdresse());
		return blz;
	}
	
  /**
   * Liefert das Eingabe-Feld fuer die IBAN.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getIban() throws RemoteException
  {
    if (this.iban == null)
    {
      String s = null;
      Address a = getAddress();
      if (a instanceof HibiscusAddress)
        s = ((HibiscusAddress)a).getIban();
      this.iban = new TextInput(s,HBCIProperties.HBCI_IBAN_MAXLENGTH);
      this.iban.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS);
      this.iban.setEnabled(isHibiscusAdresse());
    }
    return this.iban;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BIC.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBic() throws RemoteException
  {
    if (this.bic == null)
    {
      String s = null;
      Address a = getAddress();
      if (a instanceof HibiscusAddress)
        s = ((HibiscusAddress)a).getBic();
      this.bic = new TextInput(s,HBCIProperties.HBCI_BIC_MAXLENGTH);
      this.bic.setValidChars(HBCIProperties.HBCI_BIC_VALIDCHARS);
      this.bic.setEnabled(isHibiscusAdresse());
    }
    return this.bic;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Namen der Bank.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBank() throws RemoteException
  {
    if (this.bank == null)
    {
      String s = null;
      Address a = getAddress();
      if (a instanceof HibiscusAddress)
        s = ((HibiscusAddress)a).getBank();
      this.bank = new TextInput(s, HBCIProperties.HBCI_FOREIGNTRANSFER_USAGE_MAXLENGTH);
      this.bank.setEnabled(isHibiscusAdresse());
    }
    return this.bank;
  }

	/**
	 * Liefert das Eingabe-Feld fuer den Namen.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getName() throws RemoteException
	{
		if (name != null)
			return name;
		name = new TextInput(getAddress().getName(),HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    name.setEnabled(isHibiscusAdresse());
    name.setMandatory(true);
		return name;
	}

  /**
   * Speichert den Empfaenger.
   */
  public synchronized void handleStore()
  {
    try {

      if (isHibiscusAdresse())
      {
        HibiscusAddress a = (HibiscusAddress) getAddress();
        a.setKontonummer((String)getKontonummer().getValue());
        a.setBlz((String)getBlz().getValue());
        a.setName((String)getName().getValue());
        a.setKommentar((String)getKommentar().getValue());

        a.setBank((String)getBank().getValue());
        a.setIban((String)getIban().getValue());
        a.setBic((String)getBic().getValue());
        
        a.store();
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Adresse gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      }
    }
    catch (ApplicationException e2)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e2.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing address",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Speichern der Adresse: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }
}


/**********************************************************************
 * $Log: EmpfaengerControl.java,v $
 * Revision 1.48  2009/03/17 23:44:14  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.47  2009/02/18 00:35:54  willuhn
 * @N Auslaendische Bankverbindungen im Adressbuch
 *
 * Revision 1.46  2008/11/17 23:29:59  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.45  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.44  2007/08/07 23:54:15  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 * Revision 1.43  2007/04/23 21:03:48  willuhn
 * @R "getTransfers" aus Address entfernt - hat im Adressbuch eigentlich nichts zu suchen
 *
 * Revision 1.42  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.41  2007/04/09 22:45:12  willuhn
 * @N Bug 380
 *
 * Revision 1.40  2006/12/28 15:38:43  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.39  2006/10/06 16:00:42  willuhn
 * @B Bug 280
 *
 * Revision 1.38  2006/08/17 21:46:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.37  2006/08/05 20:44:39  willuhn
 * @B Bug 256
 *
 * Revision 1.36  2005/10/03 16:17:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.35  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.34  2005/08/22 12:23:18  willuhn
 * @N bug 107
 *
 * Revision 1.33  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 * Revision 1.32  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.31  2005/05/08 15:12:20  web0
 * @B wrong action in EmpfaengerList
 *
 * Revision 1.30  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.29  2005/04/05 21:51:54  web0
 * @B Begrenzung aller BLZ-Eingaben auf 8 Zeichen
 *
 * Revision 1.28  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.27  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.26  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "HibiscusAddress"
 *
 * Revision 1.25  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.24  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.21  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/10/15 20:09:43  willuhn
 * @B Laengen-Pruefung bei Empfaengername
 *
 * Revision 1.19  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.17  2004/07/23 15:51:43  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.16  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.14  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.13  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/06/03 00:23:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.9  2004/04/13 23:14:23  willuhn
 * @N datadir
 *
 * Revision 1.8  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 * Revision 1.5  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.4  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.3  2004/02/24 22:47:04  willuhn
 * @N GUI refactoring
 *
 * Revision 1.2  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/17 01:09:45  willuhn
 * *** empty log message ***
 *
 **********************************************************************/