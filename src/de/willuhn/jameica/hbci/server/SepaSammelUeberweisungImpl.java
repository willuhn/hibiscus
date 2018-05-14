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
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Containers fuer SEPA-Sammelueberweisungs-Buchungen.
 */
public class SepaSammelUeberweisungImpl extends AbstractSepaSammelTransferImpl<SepaSammelUeberweisungBuchung> implements SepaSammelUeberweisung
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public SepaSammelUeberweisungImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "sepasueb";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelTransfer#getBuchungen()
   */
  public List<SepaSammelUeberweisungBuchung> getBuchungen() throws RemoteException
  {
    DBIterator list = this.getService().createList(SepaSammelUeberweisungBuchung.class);
    list.addFilter("sepasueb_id = " + this.getID());
    list.setOrder("order by empfaenger_name,id");
    return PseudoIterator.asList(list);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SepaSammelTransfer#createBuchung()
   */
  public SepaSammelUeberweisungBuchung createBuchung() throws RemoteException, ApplicationException
  {
    SepaSammelUeberweisungBuchung b = (SepaSammelUeberweisungBuchung) this.getService().createObject(SepaSammelUeberweisungBuchung.class,null);
    if (this.isNewObject())
      store();
    b.setSammelTransfer(this);
    return b;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException
  {
    SepaSammelUeberweisung u = null;
    try
    {
      u = (SepaSammelUeberweisung) getService().createObject(SepaSammelUeberweisung.class,null);
      
      u.transactionBegin();
      u.setBezeichnung(this.getBezeichnung());
      u.setKonto(this.getKonto());
      u.setTermin(new Date());
      u.setPmtInfId(getPmtInfId());
      u.store();
      
      List<SepaSammelUeberweisungBuchung> list = this.getBuchungen();
      for (SepaSammelUeberweisungBuchung t:list)
      {
        SepaSammelUeberweisungBuchung copy = (SepaSammelUeberweisungBuchung) t.duplicate();
        copy.setSammelTransfer(u);
        copy.store();
      }
      u.transactionCommit();
      return (Duplicatable) u;
    }
    catch (Exception e)
    {
      if (u != null)
        u.transactionRollback();
      Logger.error("unable to duplicate sepa sammeltransfer",e);

      String text = i18n.tr("Fehler beim Duplizieren des SEPA-Sammelauftrages");
      if (e instanceof ApplicationException)
        text = e.getMessage();
      
      throw new RemoteException(text,e);
    }
  }
}
