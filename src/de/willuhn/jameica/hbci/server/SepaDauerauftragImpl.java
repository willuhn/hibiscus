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
import java.util.zip.CRC32;

import org.apache.commons.lang.StringUtils;

import de.jost_net.OBanToo.SEPA.IBAN;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines SEPA-Dauerauftrags.
 */
public class SepaDauerauftragImpl extends AbstractBaseDauerauftragImpl implements SepaDauerauftrag
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws RemoteException
   */
  public SepaDauerauftragImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "sepadauerauftrag";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Checksum#getChecksum()
   */
  public long getChecksum() throws RemoteException
  {
    String ersteZahlung  = getErsteZahlung() == null ? "" : HBCI.DATEFORMAT.format(getErsteZahlung());
    String letzteZahlung = getLetzteZahlung() == null ? "" : HBCI.DATEFORMAT.format(getLetzteZahlung());
    String s = getTurnus().getChecksum() +
               getBetrag() +
               getGegenkontoBLZ() +
               getGegenkontoNummer() +
               getGegenkontoName() +
               getKonto().getChecksum() +
               getZweck() +
               getEndtoEndId() +
               ersteZahlung +
               letzteZahlung;
    CRC32 crc = new CRC32();
    crc.update(s.getBytes());
    return crc.getValue();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
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

      Date ersteZahlung = getErsteZahlung();
      Date letzteZahlung = getLetzteZahlung();
      
      // BUGZILLA 197
      double betrag = getBetrag();
      if (betrag == 0.0 || Double.isNaN(betrag))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen gültigen Betrag ein."));

      if (getTurnus() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Zahlungsturnus aus"));

      if (ersteZahlung == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie ein Datum für die erste Zahlung an"));

      // Und jetzt noch checken, dass sich das Datum der letzten Zahlung
      // nicht vor der ersten Zahlung befindet
      // BUGZILLA 371
      if (letzteZahlung != null && letzteZahlung.before(ersteZahlung))
        throw new ApplicationException(i18n.tr("Bei Angabe eines Datum für die letzte Zahlung ({0}) muss dieses nach der ersten Zahlung ({1}) liegen", new String[]{HBCI.DATEFORMAT.format(letzteZahlung), HBCI.DATEFORMAT.format(ersteZahlung)}));

      //////////////////////////////////////
      // IBAN und BIC pruefen
      String s = StringUtils.trimToNull(getGegenkontoNummer());
      if (s == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die IBAN des Gegenkontos ein"));

      HBCIProperties.checkChars(s, HBCIProperties.HBCI_IBAN_VALIDCHARS);
      HBCIProperties.checkLength(s, HBCIProperties.HBCI_IBAN_MAXLENGTH);

      IBAN iban = HBCIProperties.getIBAN(s);

      // Automatisch aus IBAN vervollstaendigen, wenn sie fehlt
      if (iban != null && StringUtils.trimToNull(getGegenkontoBLZ()) == null)
        setGegenkontoBLZ(iban.getBIC());

      if (StringUtils.trimToNull(getGegenkontoBLZ()) != null)
        HBCIProperties.checkBIC(getGegenkontoBLZ());
      //
      //////////////////////////////////////

      if (StringUtils.trimToNull(getGegenkontoName()) == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Kontoinhabers des Gegenkontos ein"));
      HBCIProperties.checkLength(getGegenkontoName(), HBCIProperties.HBCI_SEPATRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(getGegenkontoName(), HBCIProperties.HBCI_SEPA_VALIDCHARS);

      String zweck = this.getZweck();
      if (zweck == null || zweck.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Verwendungszweck ein"));

      HBCIProperties.checkLength(zweck, HBCIProperties.HBCI_SEPATRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(zweck, HBCIProperties.HBCI_SEPA_VALIDCHARS);
      
      HBCIProperties.checkLength(getEndtoEndId(), HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
      HBCIProperties.checkChars(getEndtoEndId(), HBCIProperties.HBCI_SEPA_VALIDCHARS);
    
      HBCIProperties.checkLength(getPmtInfId(), HBCIProperties.HBCI_SEPA_ENDTOENDID_MAXLENGTH);
      HBCIProperties.checkChars(getPmtInfId(), HBCIProperties.HBCI_SEPA_VALIDCHARS);

      HBCIProperties.checkLength(getPurposeCode(), HBCIProperties.HBCI_SEPA_PURPOSECODE_MAXLENGTH);
      HBCIProperties.checkChars(getPurposeCode(), HBCIProperties.HBCI_SEPA_PURPOSECODE_VALIDCHARS);

    }
    catch (RemoteException e)
    {
      Logger.error("insert check failed",e);
      throw new ApplicationException(i18n.tr("Fehler bei der Prüfung des SEPA-Dauerauftrags"));
    }
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
   * @see de.willuhn.jameica.hbci.rmi.SepaDauerauftrag#canChange()
   */
  public boolean canChange() throws RemoteException
  {
    Integer i = (Integer) getAttribute("canchange");
    return i != null && i.intValue() == 1;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaDauerauftrag#canDelete()
   */
  public boolean canDelete() throws RemoteException
  {
    Integer i = (Integer) getAttribute("candelete");
    return i != null && i.intValue() == 1;
  }
  
  /**
   * Legt fest, ob der Auftrag der Bank zufolge aenderbar ist.
   * @param b true, wenn die Bank mitgeteilt hat, dass der Auftrag aenderbar ist.
   * @throws RemoteException
   */
  public void setChangable(boolean b) throws RemoteException
  {
    setAttribute("canchange", new Integer(b ? 1 : 0));
  }
  
  /**
   * Legt fest, ob der Auftrag der Bank zufolge loeschbar ist.
   * @param b true, wenn die Bank mitgeteilt hat, dass der Auftrag loeschbar ist.
   * @throws RemoteException
   */
  public void setDeletable(boolean b) throws RemoteException
  {
    setAttribute("candelete", new Integer(b ? 1 : 0));
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
