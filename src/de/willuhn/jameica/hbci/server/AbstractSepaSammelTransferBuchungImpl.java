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

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung einer einzelnen Buchung eines SEPA-Sammel-Auftrages.
 * @param <T> der konkrete Typ des SEPA-Sammelauftrages.
 */
public abstract class AbstractSepaSammelTransferBuchungImpl<T extends SepaSammelTransfer> extends AbstractHibiscusDBObject implements SepaSammelTransferBuchung<T>, Duplicatable
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @throws java.rmi.RemoteException
   */
  public AbstractSepaSammelTransferBuchungImpl() throws RemoteException
  {
    super();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "zweck";
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try {
      if (getSammelTransfer() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie den zugehörigen Sammel-Auftrag aus."));

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

      HBCIProperties.checkLength(getPurposeCode(), HBCIProperties.HBCI_SEPA_PURPOSECODE_MAXLENGTH);
      HBCIProperties.checkChars(getPurposeCode(), HBCIProperties.HBCI_SEPA_PURPOSECODE_VALIDCHARS);
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking sammeltransferbuchung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen der Buchung."));
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override
  public String getGegenkontoNummer() throws RemoteException
  {
    return (String) getAttribute("empfaenger_konto");
  }

  @Override
  public String getGegenkontoBLZ() throws RemoteException
  {
    return (String) getAttribute("empfaenger_bic");
  }

  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("this".equals(arg0))
      return this;
    return super.getAttribute(arg0);
  }

  @Override
  public String getGegenkontoName() throws RemoteException
  {
    return (String) getAttribute("empfaenger_name");
  }

  @Override
  public void setGegenkontoNummer(String kontonummer) throws RemoteException
  {
    setAttribute("empfaenger_konto",kontonummer != null ? kontonummer.toUpperCase() : null);
  }

  @Override
  public void setGegenkontoBLZ(String blz) throws RemoteException
  {
    setAttribute("empfaenger_bic",blz);
  }

  @Override
  public void setGegenkontoName(String name) throws RemoteException
  {
    setAttribute("empfaenger_name",name);
  }

  @Override
  public double getBetrag() throws RemoteException
  {
    Double d = (Double) getAttribute("betrag");
    if (d == null)
      return 0;
    return d.doubleValue();
  }

  @Override
  public String getZweck() throws RemoteException
  {
    return (String) getAttribute("zweck");
  }

  @Override
  public String getZweck2() throws RemoteException
  {
    return null;
  }

  @Override
  public void setBetrag(double betrag) throws RemoteException
  {
    setAttribute("betrag", new Double(betrag));
  }

  @Override
  public void setZweck(String zweck) throws RemoteException
  {
    setAttribute("zweck",zweck);
  }

  @Override
  public Duplicatable duplicate() throws RemoteException
  {
    SepaSammelTransferBuchung b = (SepaSammelTransferBuchung) getService().createObject(this.getClass(),null);
    b.setBetrag(getBetrag());
    b.setGegenkontoBLZ(getGegenkontoBLZ());
    b.setGegenkontoNummer(getGegenkontoNummer());
    b.setGegenkontoName(getGegenkontoName());
    b.setSammelTransfer(getSammelTransfer());
    b.setZweck(getZweck());
    b.setEndtoEndId(getEndtoEndId());
    b.setPurposeCode(getPurposeCode());
    return (Duplicatable) b;
  }

  @Override
  public String[] getWeitereVerwendungszwecke() throws RemoteException
  {
    return null;
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
