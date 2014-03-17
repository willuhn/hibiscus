/*****************************************************************************
 * 
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 ****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer einzelnen Buchung einer SEPA-Sammellastschrift.
 */
public class SepaSammelLastBuchungImpl extends AbstractSepaSammelTransferBuchungImpl<SepaSammelLastschrift> implements SepaSammelLastBuchung
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @throws java.rmi.RemoteException
   */
  public SepaSammelLastBuchungImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "sepaslastbuchung";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("sepaslast_id".equals(arg0))
      return SepaSammelLastschrift.class;

    return super.getForeignObject(arg0);
  }
  
  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    super.insertCheck();
    
    try
    {
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
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking foreign ueberweisung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen des SEPA-Auftrages."));
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.AbstractSepaSammelTransferBuchungImpl#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException
  {
    SepaSammelLastBuchung b = (SepaSammelLastBuchung) super.duplicate();
    b.setMandateId(this.getMandateId());
    b.setSignatureDate(this.getSignatureDate());
    b.setCreditorId(this.getCreditorId());
    return b;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung#getSammelTransfer()
   */
  public SepaSammelLastschrift getSammelTransfer() throws RemoteException
  {
    return (SepaSammelLastschrift) getAttribute("sepaslast_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung#setSammelTransfer(de.willuhn.jameica.hbci.rmi.SepaSammelTransfer)
   */
  public void setSammelTransfer(SepaSammelLastschrift s) throws RemoteException
  {
    setAttribute("sepaslast_id",s);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung#getMandateId()
   */
  public String getMandateId() throws RemoteException
  {
    return (String) getAttribute("mandateid");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung#setMandateId(java.lang.String)
   */
  public void setMandateId(String id) throws RemoteException
  {
    setAttribute("mandateid",id);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung#getSignatureDate()
   */
  public Date getSignatureDate() throws RemoteException
  {
    return (Date) getAttribute("sigdate");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung#setSignatureDate(java.util.Date)
   */
  public void setSignatureDate(Date date) throws RemoteException
  {
    setAttribute("sigdate",date);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung#getCreditorId()
   */
  public String getCreditorId() throws RemoteException
  {
    return (String) getAttribute("creditorid");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung#setCreditorId(java.lang.String)
   */
  public void setCreditorId(String id) throws RemoteException
  {
    setAttribute("creditorid",id);
  }
}
