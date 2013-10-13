/**********************************************************************
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.system.Application;
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
    u.setMandateId(getMandateId());
    u.setSignatureDate(getSignatureDate());
    u.setCreditorId(getCreditorId());
    u.setSequenceType(getSequenceType());
    u.setTargetDate(getTargetDate());
    u.setType(getType());
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
      HBCIProperties.checkChars(getGegenkontoBLZ(), HBCIProperties.HBCI_BIC_VALIDCHARS);
      HBCIProperties.checkLength(getGegenkontoBLZ(), HBCIProperties.HBCI_BIC_MAXLENGTH);

      if (getGegenkontoName() == null || getGegenkontoName().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Kontoinhabers des Gegenkontos ein"));
      HBCIProperties.checkLength(getGegenkontoName(), HBCIProperties.HBCI_FOREIGNTRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(getGegenkontoName(), HBCIProperties.HBCI_SEPA_VALIDCHARS);

      if (!HBCIProperties.checkIBANCRC(getGegenkontoNummer()))
        throw new ApplicationException(i18n.tr("Ungültige IBAN. Bitte prüfen Sie Ihre Eingaben."));
        
      HBCIProperties.checkLength(getZweck(), HBCIProperties.HBCI_FOREIGNTRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(getZweck(), HBCIProperties.HBCI_SEPA_VALIDCHARS);

      String creditorId = getCreditorId();
      if (creditorId == null || creditorId.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die Gläubiger-Identifikation ein."));
      HBCIProperties.checkLength(creditorId, HBCIProperties.HBCI_SEPA_CREDITORID_MAXLENGTH);
      HBCIProperties.checkChars(creditorId, HBCIProperties.HBCI_SEPA_VALIDCHARS);

      String mandateId = getMandateId();
      if (mandateId == null || mandateId.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die Mandatsreferenz ein."));
      HBCIProperties.checkLength(mandateId, HBCIProperties.HBCI_SEPA_MANDATEID_MAXLENGTH);
      HBCIProperties.checkChars(mandateId, HBCIProperties.HBCI_SEPA_VALIDCHARS);
      
      if (this.getSignatureDate() == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie das Unterschriftsdatum des Mandats ein"));
      
      if (getSequenceType() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie den Sequenz-Typ aus"));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking foreign ueberweisung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen der SEPA-Überweisung."));
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
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#getEndtoEndId()
   */
  public String getEndtoEndId() throws RemoteException
  {
    return (String) getAttribute("endtoendid");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#setEndtoEndId(java.lang.String)
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
}
