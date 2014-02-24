/*****************************************************************************
 * 
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 ****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Containers fuer SEPA-Sammellastschrift-Buchungen.
 */
public class SepaSammelLastschriftImpl extends AbstractSepaSammelTransferImpl<SepaSammelLastBuchung> implements SepaSammelLastschrift
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public SepaSammelLastschriftImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "sepaslast";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    super.insertCheck();

    try {
      if (getSequenceType() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie den Sequenz-Typ aus"));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking foreign ueberweisung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen des SEPA-Auftrages."));
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelTransfer#getBuchungen()
   */
  public List<SepaSammelLastBuchung> getBuchungen() throws RemoteException
  {
    DBIterator list = this.getService().createList(SepaSammelLastBuchung.class);
    list.addFilter("sepaslast_id = " + this.getID());
    list.setOrder("order by empfaenger_name,id");
    return PseudoIterator.asList(list);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelTransfer#createBuchung()
   */
  public SepaSammelLastBuchung createBuchung() throws RemoteException, ApplicationException
  {
    SepaSammelLastBuchung b = (SepaSammelLastBuchung) this.getService().createObject(SepaSammelLastBuchung.class,null);
    if (this.isNewObject())
      store();
    b.setSammelTransfer(this);
    return b;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift#getSequenceType()
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
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift#setSequenceType(de.willuhn.jameica.hbci.rmi.SepaLastSequenceType)
   */
  public void setSequenceType(SepaLastSequenceType type) throws RemoteException
  {
    setAttribute("sequencetype",type != null ? type.name() : null);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift#getTargetDate()
   */
  public Date getTargetDate() throws RemoteException
  {
    return (Date) getAttribute("targetdate");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift#setTargetDate(java.util.Date)
   */
  public void setTargetDate(Date date) throws RemoteException
  {
    setAttribute("targetdate",date);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift#getType()
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
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift#setType(de.willuhn.jameica.hbci.rmi.SepaLastType)
   */
  public void setType(SepaLastType type) throws RemoteException
  {
    setAttribute("sepatype",type != null ? type.name() : null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift#getOrderId()
   */
  public String getOrderId() throws RemoteException
  {
    return (String) this.getAttribute("orderid");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift#setOrderId(java.lang.String)
   */
  public void setOrderId(String orderId) throws RemoteException
  {
    this.setAttribute("orderid",orderId);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException
  {
    SepaSammelLastschrift l = null;
    try
    {
      l = (SepaSammelLastschrift) getService().createObject(SepaSammelLastschrift.class,null);
      
      l.transactionBegin();
      l.setBezeichnung(this.getBezeichnung());
      l.setKonto(this.getKonto());
      l.setTermin(new Date());
      l.setSequenceType(getSequenceType());
      l.setTargetDate(getTargetDate());
      l.setType(getType());
      l.setOrderId(getOrderId());
      l.store();
      
      List<SepaSammelLastBuchung> list = this.getBuchungen();
      for (SepaSammelLastBuchung t:list)
      {
        SepaSammelLastBuchung copy = (SepaSammelLastBuchung) t.duplicate();
        copy.setSammelTransfer(l);
        copy.store();
      }
      l.transactionCommit();
      return (Duplicatable) l;
    }
    catch (Exception e)
    {
      if (l != null)
        l.transactionRollback();
      Logger.error("unable to duplicate sepa sammeltransfer",e);
      throw new RemoteException(i18n.tr("Fehler beim Duplizieren des SEPA-Sammelauftrages"),e);
    }
  }
}
