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

  @Override
  protected String getTableName() {
    return "aueberweisung";
  }

  @Override
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
    
    return u;
  }

  @Override
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
  
  @Override
  public boolean isTerminUeberweisung() throws RemoteException
  {
    Integer i = (Integer) getAttribute("banktermin");
    return i != null && i.intValue() == 1;
  }

  @Override
  public void setTerminUeberweisung(boolean termin) throws RemoteException
  {
    setAttribute("banktermin",termin ? new Integer(1) : null);
  }
  
  @Override
  public boolean isUmbuchung() throws RemoteException
  {
    Integer i = (Integer) getAttribute("umbuchung");
    return i != null && i.intValue() == 1;
  }

  @Override
  public void setUmbuchung(boolean b) throws RemoteException
  {
    setAttribute("umbuchung",b ? new Integer(1) : null);
  }


  @Override
  public boolean ueberfaellig() throws RemoteException
  {
    // Termin-Auftraege werden sofort faellig gestellt, weil sie ja durch die Bank terminiert werden
    if (isTerminUeberweisung())
      return !ausgefuehrt();
    
    return super.ueberfaellig();
  }

  @Override
  public void setTextSchluessel(String schluessel) throws RemoteException
  {
    if (schluessel != null && schluessel.length() > 0)
      throw new RemoteException("textschluessel not allowed for foreign transfer");
  }

  @Override
  public void setGegenkontoBLZ(String blz) throws RemoteException
  {
    setAttribute("empfaenger_bic",blz);
  }

  @Override
  public String getGegenkontoBLZ() throws RemoteException
  {
    return (String) getAttribute("empfaenger_bic");
  }
  
  @Override
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException
  {
    if (list != null && list.length > 0)
      throw new RemoteException("extended usages not allowed for foreign transfer");
  }

  @Override
  public void setZweck2(String zweck2) throws RemoteException
  {
    if (zweck2 != null && zweck2.length() > 0)
      throw new RemoteException("second usage not allowed for foreign transfer");
  }
  
  @Override
  public String getEndtoEndId() throws RemoteException
  {
    return (String) getAttribute("endtoendid");
  }
  
  @Override
  public void setEndtoEndId(String id) throws RemoteException
  {
    setAttribute("endtoendid",id);
  }
  
  @Override
  public String getPmtInfId() throws RemoteException
  {
    return (String) getAttribute("pmtinfid");
  }
  
  @Override
  public void setPmtInfId(String id) throws RemoteException
  {
    setAttribute("pmtinfid",id);
  }

  @Override
  public String getPurposeCode() throws RemoteException
  {
    return (String) getAttribute("purposecode");
  }
  
  @Override
  public void setPurposeCode(String code) throws RemoteException
  {
    setAttribute("purposecode",code);
  }
}
