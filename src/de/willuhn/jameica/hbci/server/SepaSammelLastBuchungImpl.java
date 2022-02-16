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

  @Override
  protected String getTableName()
  {
    return "sepaslastbuchung";
  }

  @Override
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("sepaslast_id".equals(arg0))
      return SepaSammelLastschrift.class;

    return super.getForeignObject(arg0);
  }
  
  @Override
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
  
  @Override
  public Duplicatable duplicate() throws RemoteException
  {
    SepaSammelLastBuchung b = (SepaSammelLastBuchung) super.duplicate();
    b.setMandateId(this.getMandateId());
    b.setSignatureDate(this.getSignatureDate());
    b.setCreditorId(this.getCreditorId());
    return b;
  }

  @Override
  public SepaSammelLastschrift getSammelTransfer() throws RemoteException
  {
    return (SepaSammelLastschrift) getAttribute("sepaslast_id");
  }

  @Override
  public void setSammelTransfer(SepaSammelLastschrift s) throws RemoteException
  {
    setAttribute("sepaslast_id",s);
  }

  @Override
  public String getMandateId() throws RemoteException
  {
    return (String) getAttribute("mandateid");
  }
  
  @Override
  public void setMandateId(String id) throws RemoteException
  {
    setAttribute("mandateid",id);
  }
  
  @Override
  public Date getSignatureDate() throws RemoteException
  {
    return (Date) getAttribute("sigdate");
  }
  
  @Override
  public void setSignatureDate(Date date) throws RemoteException
  {
    setAttribute("sigdate",date);
  }
  
  @Override
  public String getCreditorId() throws RemoteException
  {
    return (String) getAttribute("creditorid");
  }
  
  @Override
  public void setCreditorId(String id) throws RemoteException
  {
    setAttribute("creditorid",id);
  }
}
