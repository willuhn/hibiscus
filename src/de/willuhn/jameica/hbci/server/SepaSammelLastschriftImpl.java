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
import java.util.List;

import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.reminder.ReminderStorageProviderHibiscus;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
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

      if (this.getTargetDate() == null)
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Zieltermin ein"));

      // Siehe http://www.onlinebanking-forum.de/forum/topic.php?t=20874&page=last#last_post - Ziel-Termin nur checken, wenn der Auftrag nicht schon als ausgefuehrt markiert ist
      if (this.getTargetDate().before(DateUtil.startOfDay(new Date())) && !ausgefuehrt())
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Zieltermin ein, der sich in der Zukunft befindet"));

      if (this.getType() == null)
        this.setType(SepaLastType.DEFAULT);
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
      l.setType(getType());
      l.setOrderId(getOrderId());
      l.setPmtInfId(getPmtInfId());

      // Wenn sich das Target-Date in der Vergangenheit befindet, muessen wir ein neues erzeugen.
      // Andernfalls wuerde das Speichern fehlschlagen, weil bei insertCheck geprueft wird, ob sich
      // das Ziel-Datum in der Zukunft befindet
      try
      {
        Date target = this.getTargetDate();
        Date now = new Date();
        Date newTarget = null;
        
        if (target != null && !target.after(now))
        {
          // Wenn der Auftrag auf einem Auftrag mit Reminder basiert, dann berechnen wir das naechste Target-Date
          // basierend auf dem Intervall.
          final String uuid = MetaKey.REMINDER_UUID.get(this);
          if (uuid != null)
          {
            Logger.info("calculating target date from duplication template");
            BeanService service = Application.getBootLoader().getBootable(BeanService.class);
            final ReminderStorageProvider provider = service.get(ReminderStorageProviderHibiscus.class);
            Reminder r = provider.get(uuid);
            final ReminderInterval ri = r.getReminderInterval();
            final List<Date> dates = ri.getDates(target,now,null);
            if (!dates.isEmpty())
              newTarget = dates.get(0); // Wir nehmen den ersten Treffer
          }
          else
          {
            // Ansonsten nehmen wir morgen
            newTarget = new Date(now.getTime() + (24 * 60 * 60 * 1000L));
          }

          l.setTargetDate(DateUtil.endOfDay(newTarget));
        }
        else
        {
          l.setTargetDate(target);
        }

      }
      catch (Exception e)
      {
        Logger.error("unable to apply target date",e);
      }
      
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
      
      String text = i18n.tr("Fehler beim Duplizieren des SEPA-Sammelauftrages");
      if (e instanceof ApplicationException)
        text = e.getMessage();
      
      throw new RemoteException(text,e);
    }
  }
}
