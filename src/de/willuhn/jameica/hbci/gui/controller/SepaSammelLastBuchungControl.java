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
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Dialog "Buchung einer SEPA-Sammellastschrift bearbeiten".
 */
public class SepaSammelLastBuchungControl extends AbstractSepaSammelTransferBuchungControl<SepaSammelLastBuchung>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	// Fach-Objekte
	private SepaSammelLastBuchung buchung = null;
	
  private AddressInput empfName              = null;
  private TextInput creditorId               = null;
  private TextInput mandateId                = null;
  private DateInput signature                = null;

  private HibiscusAddress address            = null;
  

  /**
   * ct.
   * @param view
   */
  public SepaSammelLastBuchungControl(AbstractView view)
  {
    super(view);
		
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferBuchungControl#getBuchung()
   */
  public SepaSammelLastBuchung getBuchung()
	{
		if (this.buchung != null)
			return this.buchung;
		this.buchung = (SepaSammelLastBuchung) this.getCurrentObject();
		return this.buchung;
	}
  
  /**
   * Liefert das Eingabe-Feld fuer den Empfaenger-Namen.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AddressInput getEmpfaengerName() throws RemoteException
  {
    if (empfName != null)
      return empfName;
    
    SepaSammelLastBuchung s = this.getBuchung();
    
    // Wir schauen mal, ob wir in dem Auftrag schon eine Adress-ID haben
    // Wenn das der Fall ist, bearbeitet der User scheinbar gerade eine
    // existierende Lastschrift. Wir laden dann die Adresse, damit die
    // Mandats-Daten beim Speichern wieder dieser zugeordnet werden.
    try
    {
      if (!s.isNewObject())
      {
        String id = StringUtils.trimToNull(MetaKey.ADDRESS_ID.get(s));
        if (id != null)
          this.address = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,id);
      }
    }
    catch (ObjectNotFoundException e)
    {
      // Die Adresse gibts nicht mehr
    }
    catch (RemoteException re)
    {
      Logger.error("unable to restore linked address from transfer",re);
    }
    empfName = super.getEmpfaengerName();
    empfName.addListener(new EmpfaengerListener());
    return empfName;
  }


  /**
   * Liefert das Eingabe-Feld fuer die Glaeubiger-ID.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getCreditorId() throws RemoteException
  {
    if (this.creditorId != null)
      return this.creditorId;
    
    SepaSammelLastBuchung s = this.getBuchung();
    String creditorId       = s.getCreditorId();
    
    // Checken, ob wir im Konto eine Glaeubiger-ID haben
    if (StringUtils.trimToNull(creditorId) == null)
      creditorId = StringUtils.trimToNull(MetaKey.SEPA_CREDITOR_ID.get(s.getSammelTransfer().getKonto()));
    
    this.creditorId = new TextInput(creditorId,HBCIProperties.HBCI_SEPA_CREDITORID_MAXLENGTH);
    this.creditorId.setName(i18n.tr("Gläubiger-Identifikation"));
    this.creditorId.setValidChars(HBCIProperties.HBCI_SEPA_VALIDCHARS);
    this.creditorId.setEnabled(!s.getSammelTransfer().ausgefuehrt());
    this.creditorId.setMandatory(true);
    return this.creditorId;
  }

  /**
   * Liefert das Eingabe-Feld fuer die Mandate-ID.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getMandateId() throws RemoteException
  {
    if (this.mandateId != null)
      return this.mandateId;
    
    SepaSammelLastBuchung s = this.getBuchung();
    
    this.mandateId = new TextInput(s.getMandateId(),HBCIProperties.HBCI_SEPA_MANDATEID_MAXLENGTH);
    this.mandateId.setName(i18n.tr("Mandats-Referenz"));
    this.mandateId.setValidChars(HBCIProperties.HBCI_SEPA_MANDATE_VALIDCHARS);
    this.mandateId.setEnabled(!s.getSammelTransfer().ausgefuehrt());
    this.mandateId.setMandatory(true);
    return this.mandateId;
  }

  /**
   * Liefert das Eingabe-Feld fuer das Unterschriftsdatum.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getSignatureDate() throws RemoteException
  {
    if (this.signature != null)
      return this.signature;
    
    SepaSammelLastBuchung s = this.getBuchung();
    
    this.signature = new DateInput(s.getSignatureDate(),DateUtil.DEFAULT_FORMAT);
    this.signature.setName(i18n.tr("Unterschriftsdatum des Mandats"));
    this.signature.setEnabled(!s.getSammelTransfer().ausgefuehrt());
    this.signature.setMandatory(true);
    return this.signature;
  }


  /**
   * Ueberschrieben, um die Lastschrift-spezifischen Attribute zu setzen.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferBuchungControl#handleStore()
   */
  public synchronized boolean handleStore()
  {
    SepaSammelLastBuchung s;
    SepaSammelLastschrift t = null;
    
    try
    {
      s = this.getBuchung();
      t = s.getSammelTransfer();
      
      if (t.ausgefuehrt())
        return true;
      
      s.transactionBegin();
      
      s.setCreditorId(StringUtils.trimToNull((String) getCreditorId().getValue()));
      s.setMandateId((String) getMandateId().getValue());
      s.setSignatureDate((Date) getSignatureDate().getValue());
      
      this.store();
      
      Konto k = t.getKonto();
      
      // Glaeubiger-ID im Konto speichern, damit wir sie beim naechsten Mal parat haben
      MetaKey.SEPA_CREDITOR_ID.set(k,s.getCreditorId());
      
      // Wenn wir eine fest assoziierte Adresse haben, ordnen wir die Mandats-Daten dieser zu
      String id = MetaKey.ADDRESS_ID.get(s); // Die ID kann sich u.U. geaendert haben
      if (id != null)
      {
        this.address = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,id);
      }

      // Daten des Mandats als Meta-Daten an der Adresse speichern
      if (this.address != null)
      {
        MetaKey.SEPA_MANDATE_ID.set(this.address,s.getMandateId());
        MetaKey.SEPA_MANDATE_SIGDATE.set(this.address,DateUtil.DEFAULT_FORMAT.format(s.getSignatureDate()));
      }
      
      s.transactionCommit();
      return true;
    }
    catch (Exception e)
    {
      if (t != null) {
        try {
          t.transactionRollback();
        }
        catch (Exception xe) {
          Logger.error("rollback failed",xe);
        }
      }
      
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

      try {

        // Checken, ob wir in der Adresse Mandats-Daten haben
        if (a instanceof HibiscusAddress)
        {
          HibiscusAddress addressCur = address;
          
          // Wir merken uns die ausgewaehlte Adresse fuer die spaetere Speicherung dieser Daten an der Adresse.
          address = (HibiscusAddress) a;
          
          String miNew = StringUtils.trimToNull(MetaKey.SEPA_MANDATE_ID.get(address));
          String sdNew = StringUtils.trimToNull(MetaKey.SEPA_MANDATE_SIGDATE.get(address));
          String miCur               = StringUtils.trimToNull((String)getMandateId().getValue());
          Date sdCur                 = (Date) getSignatureDate().getValue();
          
          if (miNew != null)
            getMandateId().setValue(miNew);
          
          if (sdNew != null)
            getSignatureDate().setValue(sdNew);
          
          // Hatten wir vorher Eingaben drin?
          boolean haveCur = miCur != null || sdCur != null;
          
          // Haben wir jetzt komplett neue Eingaben drin?
          boolean haveNew = miNew != null && sdNew != null;
          
          // Wenn eine *andere* Adresse ausgewaehlt wurde und vorher schon Mandatsdaten drin
          // standen und wir die nicht komplett ueberschrieben haben, zeigen wir einen Warnhinweis an
          if (addressCur != null && !BeanUtil.equals(address,addressCur) && haveCur && !haveNew)
          {
            String msg = i18n.tr("Sie haben eine neue Adresse ausgewählt zu der noch keine vollständigen Mandatsdaten\n" +
                                 "hinterlegt sind. Die Daten des Mandats stammen u.U. noch von der vorher ausgewählten\n" +
                                 "Adresse.\n\nMandats-Referenz und Unterschriftsdatum entfernen und neu eingeben?");
            
            boolean clear = Application.getCallback().askUser(msg);
            if (clear)
            {
              getMandateId().setValue("");
              getSignatureDate().setValue(null);
              getMandateId().focus();
            }
          }
          
        }
        
      }
      catch (ApplicationException ae)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      catch (Exception e)
      {
        Logger.error("error while choosing empfaenger",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei der Auswahl des Gegenkontos"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }

}
