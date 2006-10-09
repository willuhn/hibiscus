/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeKontoauszugJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/09 21:43:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.synchronize;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISaldoJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUmsatzJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Abrufen der Umsaetze und des Saldos eines Kontos.
 */
public class SynchronizeKontoauszugJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param konto
   */
  public SynchronizeKontoauszugJob(Konto konto)
  {
    super(konto);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{
        new HBCISaldoJob((Konto)getContext()),
        new HBCIUmsatzJob((Konto)getContext())};
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    Konto k = (Konto) getContext();
    return i18n.tr("Konto {0}: Kontoauszüge abrufen",k.getLongName());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new KontoNew().handleAction(getContext());
  }
}


/*********************************************************************
 * $Log: SynchronizeKontoauszugJob.java,v $
 * Revision 1.1  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.1  2006/03/17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/