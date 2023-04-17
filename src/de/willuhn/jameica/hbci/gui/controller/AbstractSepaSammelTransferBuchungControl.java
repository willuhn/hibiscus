/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.HibiscusAddressUpdate;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.BICInput;
import de.willuhn.jameica.hbci.gui.input.IBANInput;
import de.willuhn.jameica.hbci.gui.input.PurposeCodeInput;
import de.willuhn.jameica.hbci.gui.input.StoreAddressInput;
import de.willuhn.jameica.hbci.gui.input.ZweckInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung;
import de.willuhn.jameica.messaging.MessageBus;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakter Controller fuer die Dialoge "Buchung eines SEPA-Sammelauftrages bearbeiten".
 * @param <T> der konkrete Typ des Sammel-Auftrages.
 */
public abstract class AbstractSepaSammelTransferBuchungControl<T extends SepaSammelTransferBuchung> extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  // Eingabe-Felder
  private Input betrag                       = null;
  private ZweckInput zweck                   = null;

  private AddressInput empfName              = null;
  private TextInput empfkto                  = null;
  private TextInput bic                      = null;
  private TextInput endToEndId               = null;
  private PurposeCodeInput purposeCode       = null;


  private CheckboxInput storeEmpfaenger      = null;
  private HibiscusAddressUpdate aUpdate      = new HibiscusAddressUpdate();
  
  /**
   * ct.
   * @param view
   */
  public AbstractSepaSammelTransferBuchungControl(AbstractView view)
  {
    super(view);
  }

	/**
	 * Liefert die aktuelle Buchung.
   * @return die Buchung.
   */
  public abstract T getBuchung();

  /**
   * Liefert das Eingabe-Feld fuer den Empfaenger-Namen.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AddressInput getEmpfaengerName() throws RemoteException
  {
    if (empfName != null)
      return empfName;
    
    SepaSammelTransferBuchung s = this.getBuchung();
    
    empfName = new AddressInput(s.getGegenkontoName(), AddressFilter.FOREIGN);
    empfName.setMandatory(true);
    empfName.addListener(new EmpfaengerListener());
    empfName.setEnabled(!s.getSammelTransfer().ausgefuehrt());
    return empfName;
  }

  
  /**
   * Liefert das Eingabe-Feld fuer den Empfaenger.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getEmpfaengerKonto() throws RemoteException
  {
    if (empfkto != null)
      return empfkto;

    SepaSammelTransferBuchung s = this.getBuchung();

    empfkto = new IBANInput(s.getGegenkontoNummer(),this.getEmpfaengerBic());
    empfkto.setValidChars(HBCIProperties.HBCI_IBAN_VALIDCHARS);
    empfkto.setMandatory(true);
    empfkto.setEnabled(!s.getSammelTransfer().ausgefuehrt());
    return empfkto;
  }

  /**
   * Liefert das Eingabe-Feld fuer die BIC.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerBic() throws RemoteException
  {
    if (this.bic != null)
      return this.bic;
    
    SepaSammelTransferBuchung s = this.getBuchung();
    
    this.bic = new BICInput(s.getGegenkontoBLZ());
    this.bic.setEnabled(!s.getSammelTransfer().ausgefuehrt());
    return this.bic;
  }

  /**
   * Liefert das Eingabe-Feld fuer die End2End-ID.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEndToEndId() throws RemoteException
  {
    if (this.endToEndId != null)
      return this.endToEndId;

    SepaSammelTransferBuchung s = this.getBuchung();

    this.endToEndId = new TextInput(s.getEndtoEndId(),HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
    this.endToEndId.setName(i18n.tr("End-to-End ID"));
    this.endToEndId.setValidChars(HBCIProperties.HBCI_SEPA_VALIDCHARS);
    this.endToEndId.setEnabled(!s.getSammelTransfer().ausgefuehrt());
    this.endToEndId.setHint(i18n.tr("freilassen wenn nicht benötigt"));
    this.endToEndId.setMandatory(false);
    return this.endToEndId;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Purpose-Code.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getPurposeCode() throws RemoteException
  {
    if (this.purposeCode != null)
      return this.purposeCode;

    SepaSammelTransferBuchung s = this.getBuchung();

    this.purposeCode = new PurposeCodeInput(getBuchung().getPurposeCode());
    this.purposeCode.setEnabled(!s.getSammelTransfer().ausgefuehrt());
    this.purposeCode.setMandatory(false);
    return this.purposeCode;
  }


  /**
   * Liefert das Eingabe-Feld fuer den Verwendungszweck.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getZweck() throws RemoteException
  {
    if (zweck != null)
      return zweck;

    SepaSammelTransferBuchung s = this.getBuchung();

    zweck = new ZweckInput(s.getZweck());
    zweck.setEnabled(!s.getSammelTransfer().ausgefuehrt());
    return zweck;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Betrag.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBetrag() throws RemoteException
  {
    if (betrag != null)
      return betrag;
    
    SepaSammelTransferBuchung s = this.getBuchung();
    SepaSammelTransfer t        = s.getSammelTransfer();
    
    double d = s.getBetrag();
    if (Math.abs(d) < 0.01d) d = Double.NaN;
    betrag = new DecimalInput(d,HBCI.DECIMALFORMAT);
    betrag.setComment(t.getKonto().getWaehrung());
    betrag.setMandatory(true);
    betrag.setEnabled(!t.ausgefuehrt());
    return betrag;
  }

  /**
   * Liefert eine CheckBox ueber die ausgewaehlt werden kann,
   * ob der Empfaenger mitgespeichert werden soll.
   * @return CheckBox.
   * @throws RemoteException
   */
  public CheckboxInput getStoreEmpfaenger() throws RemoteException
  {
    if (storeEmpfaenger != null)
      return storeEmpfaenger;
    
    this.storeEmpfaenger = new StoreAddressInput(this.getBuchung());
    return this.storeEmpfaenger;
  }

  /**
   * Speichert die Buchung.
   * @throws Exception
   */
  protected synchronized void store() throws Exception
  {
    SepaSammelTransferBuchung s = this.getBuchung();
    if (s.getSammelTransfer().ausgefuehrt())
      return;
    
    Double d = (Double) getBetrag().getValue();
    s.setBetrag(d == null ? Double.NaN : d.doubleValue());
    
    s.setZweck((String)getZweck().getValue());
    s.setEndtoEndId((String) getEndToEndId().getValue());
    s.setPurposeCode((String) getPurposeCode().getValue());

    String kto  = (String)getEmpfaengerKonto().getValue();
    String name = getEmpfaengerName().getText();
    String bic  = (String) getEmpfaengerBic().getValue();

    s.setGegenkontoNummer(kto);
    s.setGegenkontoName(name);
    s.setGegenkontoBLZ(bic);
    
    s.store();

    {
      final Boolean store = (Boolean) getStoreEmpfaenger().getValue();
      this.aUpdate.setCreate(store.booleanValue());
      HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
      e.setIban(kto);
      e.setName(name);
      e.setBic(bic);
      this.aUpdate.handleAction(e);
      
      // wenn sie in der Action gespeichert wurde, sollte sie jetzt eine ID haben und wir koennen die Meta-Daten dran haengen
      if (e.getID() != null)
      {
        // Adress-ID am Auftrag speichern
        MetaKey.ADDRESS_ID.set(s,e.getID());
      }
    }

    Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Buchung gespeichert"),StatusBarMessage.TYPE_SUCCESS));
    MessageBus.send("hibiscus.transfer.check",s);
  }

  /**
   * Speichert die Buchung.
   * @return true, wenn das Speichern erfolgreich war, sonst false.
   */
  public synchronized boolean handleStore()
  {
    try
    {
      SepaSammelTransferBuchung s = this.getBuchung();
      SepaSammelTransfer t = s.getSammelTransfer();
      
      if (t.ausgefuehrt())
        return true;
      
      this.store();
      return true;
    }
    catch (Exception e)
    {
      if (e instanceof ApplicationException)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      else
      {
        Logger.error("error while saving order",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
      }
    }
    return false;
  }

  /**
   * Listener, der bei Auswahl des Empfaengers die restlichen Daten vervollstaendigt.
   */
  private class EmpfaengerListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
      if (event == null)
        return;
      
      if (!(event.data instanceof Address))
        return;
      
      Address a = (Address) event.data;
      aUpdate.setAddress(a);

      try {
        getEmpfaengerName().setText(a.getName());
        getEmpfaengerKonto().setValue(a.getIban());
        getEmpfaengerBic().setValue(a.getBic());

        // Wenn der Empfaenger aus dem Adressbuch kommt, deaktivieren wir die Checkbox
        getStoreEmpfaenger().setValue(Boolean.FALSE);
        
        try
        {
          String zweck = StringUtils.trimToNull((String) getZweck().getValue());
          if (zweck == null)
          {
            // Verwendungszweck vervollstaendigen
            SepaSammelTransferBuchung s = getBuchung();
            DBIterator list = s.getList();
            list.addFilter("empfaenger_konto = ?",a.getIban());
            list.addFilter("empfaenger_name = ?",a.getName());
            list.setOrder("order by id desc");
            if (list.hasNext())
            {
              SepaSammelTransferBuchung t = (SepaSammelTransferBuchung) list.next();
              getZweck().setValue(t.getZweck());
            }
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to autocomplete subject",e);
        }
      }
      catch (Exception e)
      {
        Logger.error("error while choosing empfaenger",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei der Auswahl des Gegenkontos"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }


}
