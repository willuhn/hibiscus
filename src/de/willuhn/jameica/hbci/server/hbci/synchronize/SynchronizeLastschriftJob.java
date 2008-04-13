/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeLastschriftJob.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/04/13 04:20:41 $
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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCILastschriftJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen Lastschrift.
 */
public class SynchronizeLastschriftJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param last
   */
  public SynchronizeLastschriftJob(Lastschrift last)
  {
    super(last);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCILastschriftJob((Lastschrift)getContext())};
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    Lastschrift last = (Lastschrift) getContext();
    Konto k = last.getKonto();
    String[] params = new String[] {
        k.getLongName(),
        last.getZweck(),
        HBCI.DECIMALFORMAT.format(last.getBetrag()),
        k.getWaehrung(),
        last.getGegenkontoName()
       };
    return i18n.tr("{0}: ({1}) {2} {3} von {4} einziehen",params);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new LastschriftNew().handleAction(getContext());
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring() throws RemoteException
  {
    return false;
  }
}


/*********************************************************************
 * $Log: SynchronizeLastschriftJob.java,v $
 * Revision 1.4  2008/04/13 04:20:41  willuhn
 * @N Bug 583
 *
 * Revision 1.3  2007/06/15 11:20:32  willuhn
 * @N Saldo in Kontodetails via Messaging sofort aktualisieren
 * @N Mehr Details in den Namen der Synchronize-Jobs
 * @N Layout der Umsatzdetail-Anzeige ueberarbeitet
 *
 * Revision 1.2  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.1  2006/03/17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/