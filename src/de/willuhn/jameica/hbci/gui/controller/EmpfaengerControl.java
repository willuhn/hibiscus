/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/EmpfaengerControl.java,v $
 * $Revision: 1.51 $
 * $Date: 2010/04/15 10:30:07 $
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.jost_net.OBanToo.SEPA.IBAN;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
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
public class EmpfaengerControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  // Fach-Objekte
	private Address address         = null;
	// Eingabe-Felder
	private TextInput kontonummer   = null;
	private TextInput blz					  = null;
	private Input name				      = null;

	private TextInput bic           = null;
	private TextInput iban          = null;
  private TextInput bank          = null;

  private SelectInput kategorie   = null;
  
	private Input kommentar         = null;

  private Part list               = null;
  private Part sammelList         = null;
  private Part sammelList2        = null;
  private Part umsatzList         = null;
  
  private IbanListener ibanListener = new IbanListener();
  
  /**
   * @param view
   */
  public EmpfaengerControl(AbstractView view)
  {
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

    Address a = this.getAddress();
    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    // BUGZILLA 1328 https://www.willuhn.de/bugzilla/show_bug.cgi?id=1328
    // wenn wir eine IBAN haben, muessen wir auch nach der suchen.
    // BUGZILLA 1395 wenn der Adressbuch-Eintrag nur IBAN hat und keine Kontonummer/BLZ, dann nur nach IBAN suchen
    String iban = a.getIban();
    String konto = a.getKontonummer();
    
    if (StringUtils.isNotEmpty(iban)) // haben wir eine IBAN?
    {
      if (StringUtils.isNotEmpty(konto)) // haben wir ausserdem Konto/BLZ?
        list.addFilter("((empfaenger_konto like ? and empfaenger_blz = ?) or lower(empfaenger_konto) = ?)","%" + konto, a.getBlz(), iban.toLowerCase());
      else // nur IBAN // BUGZILLA 1395
        list.addFilter("lower(empfaenger_konto) = ?",iban.toLowerCase());
    }
    else
    {
      list.addFilter("empfaenger_konto like ?","%" + konto);
      list.addFilter("empfaenger_blz = ?",a.getBlz());
    }

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
    
    boolean b = this.isHibiscusAdresse();
    kontonummer.setEnabled(b);
    if (b)
      kontonummer.addListener(this.ibanListener);
    
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
   * Liefert ein editierbares Auswahlfeld mit der Kategorie.
   * @return Auswahlfeld.
   * @throws RemoteException
   */
  public SelectInput getKategorie() throws RemoteException
  {
    if (this.kategorie != null)
      return this.kategorie;
    
    List<String> list = (List<String>) Settings.getDBService().execute("select kategorie from empfaenger where kategorie is not null and kategorie != '' group by kategorie order by LOWER(kategorie)",null,new ResultSetExtractor()
    {
      /**
       * @see de.willuhn.datasource.rmi.ResultSetExtractor#extract(java.sql.ResultSet)
       */
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        List<String> list = new ArrayList<String>();
        list.add(""); // <Keine Kategorie>
        while (rs.next())
          list.add(rs.getString(1));
        return list;
      }
    });

    this.kategorie = new SelectInput(list,this.getAddress().getKategorie());
    this.kategorie.setName(i18n.tr("Gruppe"));
    this.kategorie.setEditable(true);
    this.kategorie.setEnabled(isHibiscusAdresse());
    return this.kategorie;
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
    
    boolean b = this.isHibiscusAdresse();
    blz.setEnabled(b);
    if (b)
      blz.addListener(this.ibanListener);
    
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
      this.iban = new TextInput(getAddress().getIban(),HBCIProperties.HBCI_IBAN_MAXLENGTH);
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
      this.bic = new TextInput(getAddress().getBic(),HBCIProperties.HBCI_BIC_MAXLENGTH);
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
	 * Vervollstaendigt IBAN/BIC.
	 */
	private class IbanListener implements Listener
	{
	  /**
	   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	   */
	  public void handleEvent(Event event)
	  {
	    try
	    {
        String blz  = StringUtils.trimToNull((String) getBlz().getValue());
        String bic  = StringUtils.trimToNull((String) getBic().getValue());
        
        String kto  = StringUtils.trimToNull((String) getKontonummer().getValue());
        String iban = StringUtils.trimToNull((String) getIban().getValue());

	      if (blz != null && blz.length() == HBCIProperties.HBCI_BLZ_LENGTH)
	      {
	        String newBic = null;
	        
	        if (HBCI.COMPLETE_IBAN && kto != null && iban == null)
	        {
	          IBAN newIban = HBCIProperties.getIBAN(blz,kto);
	          newBic = newIban.getBIC();
            getIban().setValue(newIban.getIBAN());
	        }
	        
          if (bic == null)
          {
            if (newBic == null) // nur wenn sie nicht schon von obantoo ermittelt wurde
              newBic = HBCIUtils.getBICForBLZ(blz);
            getBic().setValue(newBic);
          }
	      }
	    }
	    catch (ApplicationException ae)
	    {
	      Logger.warn("unable to complete IBAN/BIC: " + ae.getMessage());
	    }
	    catch (Exception e)
	    {
	      Logger.error("unable to auto-complete IBAN/BIC",e);
	    }
	  }
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
        a.setKategorie((String)getKategorie().getValue());

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
