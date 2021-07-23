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

  @Override
  protected String getTableName()
  {
    return "sepasueb";
  }

  @Override
  public List<SepaSammelUeberweisungBuchung> getBuchungen() throws RemoteException
  {
    DBIterator list = this.getService().createList(SepaSammelUeberweisungBuchung.class);
    list.addFilter("sepasueb_id = " + this.getID());
    list.setOrder("order by empfaenger_name,id");
    return PseudoIterator.asList(list);
  }

  @Override
  public SepaSammelUeberweisungBuchung createBuchung() throws RemoteException, ApplicationException
  {
    SepaSammelUeberweisungBuchung b = (SepaSammelUeberweisungBuchung) this.getService().createObject(SepaSammelUeberweisungBuchung.class,null);
    if (this.isNewObject())
      store();
    b.setSammelTransfer(this);
    return b;
  }
  
  @Override
  public Duplicatable duplicate() throws RemoteException
  {
    SepaSammelUeberweisung u = null;
    try
    {
      u = (SepaSammelUeberweisung) getService().createObject(SepaSammelUeberweisung.class,null);
      
      u.transactionBegin();
      u.setBezeichnung(this.getBezeichnung());
      u.setKonto(this.getKonto());
      u.setTerminUeberweisung(isTerminUeberweisung());
      u.setTermin(isTerminUeberweisung() ? getTermin() : new Date());
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
  
  @Override
  public boolean ueberfaellig() throws RemoteException
  {
    // Termin-Auftraege werden sofort faellig gestellt, weil sie ja durch die Bank terminiert werden
    if (isTerminUeberweisung())
      return !ausgefuehrt();
    
    return super.ueberfaellig();
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

}
