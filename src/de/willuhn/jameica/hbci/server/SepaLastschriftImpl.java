/**********************************************************************
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine SEPA-Lastschrift.
 */
public class SepaLastschriftImpl extends AbstractBaseUeberweisungImpl implements SepaLastschrift
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @throws RemoteException
   */
  public SepaLastschriftImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "sepalastschrift";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException {
    SepaLastschriftImpl u = (SepaLastschriftImpl) getService().createObject(SepaLastschrift.class,null);
    u.setBetrag(getBetrag());
    u.setGegenkontoNummer(getGegenkontoNummer());
    u.setGegenkontoName(getGegenkontoName());
    u.setGegenkontoBLZ(getGegenkontoBLZ());
    u.setKonto(getKonto());
    u.setZweck(getZweck());
    u.setEndtoEndId(getEndtoEndId());
    u.setPmtInfId(getPmtInfId());
    u.setMandateId(getMandateId());
    u.setSignatureDate(getSignatureDate());
    u.setCreditorId(getCreditorId());
    u.setSequenceType(getSequenceType());
    u.setType(getType());
    u.setOrderId(getOrderId());
    u.setPurposeCode(getPurposeCode());

    // Wenn sich das Target-Date in der Vergangenheit befindet, muessen wir ein neues erzeugen.
    // Andernfalls wuerde das Speichern fehlschlagen, weil bei insertCheck geprueft wird, ob sich
    // das Ziel-Datum in der Zukunft befindet
    Date target = this.getTargetDate();
    Date now = new Date();
    if (target != null && !target.after(now))
    {
      // Wir nehmen morgen.
      target = DateUtil.endOfDay(new Date(now.getTime() + (24 * 60 * 60 * 1000L)));
      u.setTargetDate(target);
    }
    else
    {
      u.setTargetDate(target);
    }

    return u;
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
    try {
      Konto k = getKonto();

      if (k == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus."));
      if (k.isNewObject())
        throw new ApplicationException(i18n.tr("Bitte speichern Sie zunächst das Konto"));
      
      String kiban = k.getIban();
      if (kiban == null || kiban.length() == 0)
        throw new ApplicationException(i18n.tr("Das ausgewählte Konto besitzt keine IBAN"));
      
      String bic = k.getBic();
      if (bic == null || bic.length() == 0)
        throw new ApplicationException(i18n.tr("Das ausgewählte Konto besitzt keine BIC"));

      double betrag = getBetrag();
      if (betrag == 0.0 || Double.isNaN(betrag))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen gültigen Betrag ein."));

      if (getGegenkontoNummer() == null || getGegenkontoNummer().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die IBAN des Gegenkontos ein"));
      HBCIProperties.checkChars(getGegenkontoNummer(), HBCIProperties.HBCI_IBAN_VALIDCHARS);
      HBCIProperties.checkLength(getGegenkontoNummer(), HBCIProperties.HBCI_IBAN_MAXLENGTH);

      if (getGegenkontoBLZ() == null || getGegenkontoBLZ().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die BIC des Gegenkontos ein"));
      HBCIProperties.checkBIC(getGegenkontoBLZ());

      if (StringUtils.trimToNull(getGegenkontoName()) == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Kontoinhabers des Gegenkontos ein"));
      HBCIProperties.checkLength(getGegenkontoName(), HBCIProperties.HBCI_FOREIGNTRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(getGegenkontoName(), HBCIProperties.HBCI_SEPA_VALIDCHARS);

      HBCIProperties.getIBAN(getGegenkontoNummer());
        
      HBCIProperties.checkLength(getZweck(), HBCIProperties.HBCI_FOREIGNTRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(getZweck(), HBCIProperties.HBCI_SEPA_VALIDCHARS);

      HBCIProperties.checkLength(getEndtoEndId(), HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
      HBCIProperties.checkChars(getEndtoEndId(), HBCIProperties.HBCI_SEPA_VALIDCHARS);

      HBCIProperties.checkLength(getPmtInfId(), HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
      HBCIProperties.checkChars(getPmtInfId(), HBCIProperties.HBCI_SEPA_VALIDCHARS);

      HBCIProperties.checkLength(getPurposeCode(), HBCIProperties.HBCI_SEPA_PURPOSECODE_MAXLENGTH);
      HBCIProperties.checkChars(getPurposeCode(), HBCIProperties.HBCI_SEPA_PURPOSECODE_VALIDCHARS);

      String creditorId = getCreditorId();
      if (creditorId == null || creditorId.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die Gläubiger-Identifikation ein."));
      HBCIProperties.checkLength(creditorId, HBCIProperties.HBCI_SEPA_CREDITORID_MAXLENGTH);
      HBCIProperties.checkChars(creditorId, HBCIProperties.HBCI_SEPA_VALIDCHARS);
      if (!HBCIProperties.checkCreditorIdCRC(creditorId))
        throw new ApplicationException(i18n.tr("Ungültige Gläubiger-Identifikation. Bitte prüfen Sie Ihre Eingaben."));

      String mandateId = getMandateId();
      if (mandateId == null || mandateId.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die Mandatsreferenz ein."));
      HBCIProperties.checkLength(mandateId, HBCIProperties.HBCI_SEPA_MANDATEID_MAXLENGTH);
      HBCIProperties.checkChars(mandateId, HBCIProperties.HBCI_SEPA_VALIDCHARS);
      
      if (this.getSignatureDate() == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie das Unterschriftsdatum des Mandats ein"));
      
      if (getSequenceType() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie den Sequenz-Typ aus"));
      
      if (this.getTargetDate() == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Zieltermin ein"));

      if (!this.getTargetDate().after(DateUtil.startOfDay(new Date())))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Zieltermin ein, der sich in der Zukunft befindet"));

      if (this.getType() == null)
        this.setType(SepaLastType.DEFAULT);
      
      if (this.getTermin() == null)
        this.setTermin(new Date());
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking foreign ueberweisung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen des SEPA-Auftrages."));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractBaseUeberweisungImpl#setTextSchluessel(java.lang.String)
   */
  public void setTextSchluessel(String schluessel) throws RemoteException
  {
    if (schluessel != null && schluessel.length() > 0)
      throw new RemoteException("textschluessel not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#setGegenkontoBLZ(java.lang.String)
   */
  public void setGegenkontoBLZ(String blz) throws RemoteException
  {
    setAttribute("empfaenger_bic",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#getGegenkontoBLZ()
   */
  public String getGegenkontoBLZ() throws RemoteException
  {
    return (String) getAttribute("empfaenger_bic");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#setGegenkonto(de.willuhn.jameica.hbci.rmi.Address)
   */
  public void setGegenkonto(Address e) throws RemoteException
  {
    if (e == null)
      return;

    // BUGZILLA 1437 - wir uebernehmen hier stattdessen BIC und IBAN
    setGegenkontoBLZ(e.getBic());
    setGegenkontoNummer(e.getIban());
    setGegenkontoName(e.getName());
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#setWeitereVerwendungszwecke(java.lang.String[])
   */
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException
  {
    if (list != null && list.length > 0)
      throw new RemoteException("extended usages not allowed for foreign transfer");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException
  {
    if (zweck2 != null && zweck2.length() > 0)
      throw new RemoteException("second usage not allowed for sepa debit");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaBooking#getEndtoEndId()
   */
  public String getEndtoEndId() throws RemoteException
  {
    return (String) getAttribute("endtoendid");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaBooking#setEndtoEndId(java.lang.String)
   */
  public void setEndtoEndId(String id) throws RemoteException
  {
    setAttribute("endtoendid",id);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#getMandateId()
   */
  public String getMandateId() throws RemoteException
  {
    return (String) getAttribute("mandateid");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#setMandateId(java.lang.String)
   */
  public void setMandateId(String id) throws RemoteException
  {
    setAttribute("mandateid",id);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#getSignatureDate()
   */
  public Date getSignatureDate() throws RemoteException
  {
    return (Date) getAttribute("sigdate");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#getCreditorId()
   */
  public String getCreditorId() throws RemoteException
  {
    return (String) getAttribute("creditorid");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#setCreditorId(java.lang.String)
   */
  public void setCreditorId(String id) throws RemoteException
  {
    setAttribute("creditorid",id);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#setSignatureDate(java.util.Date)
   */
  public void setSignatureDate(Date date) throws RemoteException
  {
    setAttribute("sigdate",date);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#getSequenceType()
   */
  public SepaLastSequenceType getSequenceType() throws RemoteException
  {
    String val = (String) getAttribute("sequencetype");
    if (val == null || val.length() == 0)
      return null;
    
    try
    {
      return SepaLastSequenceType.valueOf(val);
    }
    catch (Exception e)
    {
      Logger.error("invalid sequencetype: " + val,e);
      return null;
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#setSequenceType(de.willuhn.jameica.hbci.rmi.SepaLastSequenceType)
   */
  public void setSequenceType(SepaLastSequenceType type) throws RemoteException
  {
    setAttribute("sequencetype",type != null ? type.name() : null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#getTargetDate()
   */
  public Date getTargetDate() throws RemoteException
  {
    return (Date) getAttribute("targetdate");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#setTargetDate(java.util.Date)
   */
  public void setTargetDate(Date date) throws RemoteException
  {
    setAttribute("targetdate",date);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#getType()
   */
  public SepaLastType getType() throws RemoteException
  {
    String val = (String) getAttribute("sepatype");
    if (val == null || val.length() == 0)
      return null;
    
    try
    {
      return SepaLastType.valueOf(val);
    }
    catch (Exception e)
    {
      Logger.error("invalid sepa-type: " + val,e);
      return null;
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#setType(de.willuhn.jameica.hbci.rmi.SepaLastType)
   */
  public void setType(SepaLastType type) throws RemoteException
  {
    setAttribute("sepatype",type != null ? type.name() : null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#getOrderId()
   */
  public String getOrderId() throws RemoteException
  {
    return (String) this.getAttribute("orderid");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaLastschrift#setOrderId(java.lang.String)
   */
  public void setOrderId(String orderId) throws RemoteException
  {
    this.setAttribute("orderid",orderId);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaPayment#getPmtInfId()
   */
  public String getPmtInfId() throws RemoteException
  {
    return (String) getAttribute("pmtinfid");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaPayment#setPmtInfId(java.lang.String)
   */
  public void setPmtInfId(String id) throws RemoteException
  {
    setAttribute("pmtinfid",id);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaBooking#getPurposeCode()
   */
  @Override
  public String getPurposeCode() throws RemoteException
  {
    return (String) getAttribute("purposecode");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaBooking#setPurposeCode(java.lang.String)
   */
  @Override
  public void setPurposeCode(String code) throws RemoteException
  {
    setAttribute("purposecode",code);
  }

}
