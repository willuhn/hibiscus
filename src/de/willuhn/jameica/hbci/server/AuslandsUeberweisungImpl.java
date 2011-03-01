/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AuslandsUeberweisungImpl.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/03/01 10:52:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

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
      HBCIProperties.checkChars(getGegenkontoName(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);

      if (!HBCIProperties.checkIBANCRC(getGegenkontoNummer()))
        throw new ApplicationException(i18n.tr("Ungültige IBAN. Bitte prüfen Sie Ihre Eingaben."));
        
      HBCIProperties.checkLength(getZweck(), HBCIProperties.HBCI_FOREIGNTRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkChars(getZweck(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);
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
      throw new RemoteException("second usage not allowed for foreign transfer");
  }
}


/**********************************************************************
 * $Log: AuslandsUeberweisungImpl.java,v $
 * Revision 1.6  2011/03/01 10:52:18  willuhn
 * @B Exception nur dann werfen, wenn bei Textschluessel oder Verwendungszweck auch tatsaechlich etwas angegeben wurde - siehe Mail von Patrick vom 01.03.2011
 *
 * Revision 1.5  2010/04/27 11:02:32  willuhn
 * @R Veralteten Verwendungszweck-Code entfernt
 *
 * Revision 1.4  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.3  2009/05/07 15:13:37  willuhn
 * @N BIC in Auslandsueberweisung
 *
 * Revision 1.2  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/