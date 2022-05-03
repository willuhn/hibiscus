/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine Auslands-Ueberweisung.
 */
public class AuslandsUeberweisungImpl extends AbstractBaseUeberweisungImpl implements AuslandsUeberweisung
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @throws RemoteException
   */
  public AuslandsUeberweisungImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "aueberweisung";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException {
    AuslandsUeberweisung u = (AuslandsUeberweisung) getService().createObject(AuslandsUeberweisung.class,null);
    u.setBetrag(getBetrag());
    u.setGegenkontoNummer(getGegenkontoNummer());
    u.setGegenkontoName(getGegenkontoName());
    u.setGegenkontoBLZ(getGegenkontoBLZ());
    u.setKonto(getKonto());
    u.setZweck(getZweck());
    u.setEndtoEndId(getEndtoEndId());
    u.setPmtInfId(getPmtInfId());
    u.setTerminUeberweisung(isTerminUeberweisung());
    u.setTermin(isTerminUeberweisung() ? getTermin() : new Date());
    u.setUmbuchung(isUmbuchung());
    u.setPurposeCode(getPurposeCode());
    u.setInstantPayment(isInstantPayment());
    
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

      //////////////////////////////////////
      // IBAN und BIC pruefen
      String s = StringUtils.trimToNull(getGegenkontoNummer());
      if (s == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die IBAN des Gegenkontos ein"));

      HBCIProperties.checkChars(s, HBCIProperties.HBCI_IBAN_VALIDCHARS);
      HBCIProperties.checkLength(s, HBCIProperties.HBCI_IBAN_MAXLENGTH);
      HBCIProperties.checkIBAN(s);

      if (StringUtils.trimToNull(getGegenkontoBLZ()) != null)
        HBCIProperties.checkBIC(getGegenkontoBLZ());
      //
      //////////////////////////////////////

      if (StringUtils.trimToNull(getGegenkontoName()) == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Kontoinhabers des Gegenkontos ein"));
      HBCIProperties.checkLength(getGegenkontoName(), HBCIProperties.HBCI_SEPATRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(getGegenkontoName(), HBCIProperties.HBCI_SEPA_VALIDCHARS);

      HBCIProperties.checkLength(getZweck(), HBCIProperties.HBCI_SEPATRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(getZweck(), HBCIProperties.HBCI_SEPA_VALIDCHARS);
      
      HBCIProperties.checkLength(getEndtoEndId(), HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
      HBCIProperties.checkChars(getEndtoEndId(), HBCIProperties.HBCI_SEPA_VALIDCHARS);
      
      HBCIProperties.checkLength(getPmtInfId(), HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
      HBCIProperties.checkChars(getPmtInfId(), HBCIProperties.HBCI_SEPA_PMTINF_VALIDCHARS);

      HBCIProperties.checkLength(getPurposeCode(), HBCIProperties.HBCI_SEPA_PURPOSECODE_MAXLENGTH);
      HBCIProperties.checkChars(getPurposeCode(), HBCIProperties.HBCI_SEPA_PURPOSECODE_VALIDCHARS);
      
      if (isUmbuchung() && isTerminUeberweisung())
        throw new ApplicationException(i18n.tr("Eine Umbuchung kann nicht als Termin-Auftrag gesendet werden"));
      
      if (this.getTermin() == null)
        this.setTermin(new Date());

    }
    catch (RemoteException e)
    {
      Logger.error("error while checking job",e);
      if (!this.markingExecuted())
        throw new ApplicationException(i18n.tr("Fehler beim Prüfen des SEPA-Auftrages."));
    }
    catch (ApplicationException ae)
    {
      if (!this.markingExecuted())
        throw ae;
      
      Logger.warn(ae.getMessage());
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#isTerminUeberweisung()
   */
  public boolean isTerminUeberweisung() throws RemoteException
  {
    Integer i = (Integer) getAttribute("banktermin");
    return i != null && i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#setTerminUeberweisung(boolean)
   */
  public void setTerminUeberweisung(boolean termin) throws RemoteException
  {
    setAttribute("banktermin",termin ? Integer.valueOf(1) : null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#isUmbuchung()
   */
  public boolean isUmbuchung() throws RemoteException
  {
    Integer i = (Integer) getAttribute("umbuchung");
    return i != null && i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#setUmbuchung(boolean)
   */
  public void setUmbuchung(boolean b) throws RemoteException
  {
    setAttribute("umbuchung",b ? Integer.valueOf(1) : null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#isInstantPayment()
   */
  @Override
  public boolean isInstantPayment() throws RemoteException
  {
    Integer i = (Integer) getAttribute("instantpayment");
    return i != null && i.intValue() == 1;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung#setInstantPayment(boolean)
   */
  @Override
  public void setInstantPayment(boolean b) throws RemoteException
  {
    setAttribute("instantpayment",b ? Integer.valueOf(1) : null);
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractBaseUeberweisungImpl#ueberfaellig()
   */
  public boolean ueberfaellig() throws RemoteException
  {
    // Termin-Auftraege werden sofort faellig gestellt, weil sie ja durch die Bank terminiert werden
    if (isTerminUeberweisung())
      return !ausgefuehrt();
    
    return super.ueberfaellig();
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
      throw new RemoteException("second usage not allowed for foreign transfer");
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
