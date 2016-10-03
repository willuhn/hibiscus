/**********************************************************************
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.gui.input.IBANInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzTypInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Addressbook;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Detailansicht eines Umsatzes.
 */
public class UmsatzDetailControl extends AbstractControl
{
  private static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  // Fachobjekte
  private Umsatz umsatz = null;
	
	// Eingabe-Felder
	private Input konto				 		= null;
	private Input empfaengerName  = null;
	private Input empfaengerKonto = null;
  private Input empfaengerBlz   = null;
	private Input datum						= null;
	private Input valuta					= null;
	private Input zweck           = null;

  private LabelInput betrag     = null;
	private LabelInput saldo		  = null;
	private Input primanota				= null;
	private Input art							= null;
	private Input customerRef			= null;
	private TextInput gvcode      = null;
  
  private Input kommentar       = null;
  
  private SelectInput umsatzTyp = null;
  
  private CheckboxInput zweckSwitch = null;

  /**
   * ct.
   * @param view
   */
  public UmsatzDetailControl(AbstractView view) {
    super(view);
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
    this.kommentar.setEnabled((getUmsatz().getFlags() & Umsatz.FLAG_NOTBOOKED) == 0);
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
      Logger.error("unable to check, if address in addressbook",e);
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
    konto.setComment(HBCIProperties.getNameForBank(k.getBLZ()));
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

    int typ = UmsatzTyp.TYP_EGAL;

    Umsatz u = getUmsatz();
    UmsatzTyp ut = u != null ? u.getUmsatzTyp() : null;
    
    // wenn noch keine Kategorie zugeordnet ist, bieten wir nur die passenden an.
    if (u != null && ut == null && u.getBetrag() != 0)
      typ = (u.getBetrag() > 0 ? UmsatzTyp.TYP_EINNAHME : UmsatzTyp.TYP_AUSGABE);
    
    // Ansonsten alle - damit die zugeordnete Kategorie auch dann noch
    // noch angeboten wird, der User nachtraeglich den Kat-Typ geaendert hat.
    this.umsatzTyp = new UmsatzTypInput(ut,typ);
    
    this.umsatzTyp.setEnabled((u.getFlags() & Umsatz.FLAG_NOTBOOKED) == 0);
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
      String s = getUmsatz().getGegenkontoNummer();
      
      if (StringUtils.trimToEmpty(s).length() > 10)
        this.empfaengerKonto = new IBANInput(s,null);
      else
        this.empfaengerKonto = new TextInput(s,HBCIProperties.HBCI_IBAN_MAXLENGTH);
      
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
    if (betrag != null)
      return betrag;
    
    double s = getUmsatz().getBetrag();
    betrag = new LabelInput(HBCI.DECIMALFORMAT.format(s));
    betrag.setComment(getUmsatz().getKonto().getWaehrung());
    betrag.setColor(ColorUtil.getColor(s,Color.ERROR,Color.SUCCESS,Color.FOREGROUND));
    return betrag;
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
		if (saldo != null)
			return saldo;
    
    double s = getUmsatz().getSaldo();
		saldo = new LabelInput(HBCI.DECIMALFORMAT.format(s));
		saldo.setComment(getUmsatz().getKonto().getWaehrung());
    saldo.setColor(ColorUtil.getColor(s,Color.ERROR,Color.SUCCESS,Color.FOREGROUND));
    return saldo;
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
	 * Liefert ein Eingabe-Feld fuer den GV-Code.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getGvCode() throws RemoteException
	{
	  if (this.gvcode == null)
	  {
	    String gv  = getUmsatz().getGvCode();
	    String add = getUmsatz().getAddKey();
	    
	    // Aus Platzgruenden zeigen wir das kombiniert an.
	    if (gv == null)
	      gv = "";
	    if (add != null && add.length() > 0)
	      gv = gv + "/" + add;
	    this.gvcode = new TextInput(gv,7);
	    this.gvcode.setValidChars("01234567890/");
	    this.gvcode.setEnabled(false);
	  }
	  return this.gvcode;
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
      this.zweck = new TextAreaInput("");
      this.zweck.setEnabled(false);
	  }
	  return this.zweck;
	}
	
	/**
	 * Liefert den Wert des Settings.
	 * @return der Wert des Settings.
	 */
	protected boolean getZweckSwitchValue()
	{
    return settings.getBoolean("usage.display.all",true);
	}
	
	/**
	 * Liefert eine Checkbox, mit der man umschalten kann, ob man die vereinfachte
	 * Version des Verwendungszwecks angezeigt bekommt oder die ausfuehrliche.
	 * @return Checkbox.
	 * @throws RemoteException
	 */
	public CheckboxInput getZweckSwitch() throws RemoteException
	{
	  if (this.zweckSwitch != null)
	    return this.zweckSwitch;
	  
	  this.zweckSwitch = new CheckboxInput(getZweckSwitchValue());
	  this.zweckSwitch.setName(i18n.tr("Alle Daten des Verwendungszwecks anzeigen"));
	  
	  Listener l = new Listener() {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          boolean b = ((Boolean) zweckSwitch.getValue()).booleanValue();
          settings.setAttribute("usage.display.all",b);
          Umsatz u = getUmsatz();
          getZweck().setValue(b ? VerwendungszweckUtil.toString(u,"\n") : (String) BeanUtil.get(u,Tag.SVWZ.name()));
        }
        catch (RemoteException re)
        {
          Logger.error("unable to display usage text",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen des Verwendungszweck: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
      }
    };
	  this.zweckSwitch.addListener(l);
	  l.handleEvent(null); // einmal initial ausloesen
	  
    return this.zweckSwitch;
	}

  /**
   * Speichert die editierbaren Properties.
   * @return true, wenn das Speichern erfolgreich war.
   */
  public boolean handleStore()
  {
    Umsatz u = getUmsatz();
    try
    {
      u.setKommentar((String)getKommentar().getValue());
      u.setUmsatzTyp((UmsatzTyp)getUmsatzTyp().getValue());
      getUmsatz().store();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Umsatz gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      return true;
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
    return false;
  }
}
