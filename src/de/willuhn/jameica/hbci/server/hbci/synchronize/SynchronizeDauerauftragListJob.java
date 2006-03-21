/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeDauerauftragListJob.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/03/21 00:43:14 $
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

import de.willuhn.jameica.hbci.gui.action.DauerauftragList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Abrufen der Dauerauftraege eines Kontos.
 */
public class SynchronizeDauerauftragListJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param konto
   */
  public SynchronizeDauerauftragListJob(Konto konto)
  {
    super(konto);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJob()
   */
  public AbstractHBCIJob createHBCIJob() throws RemoteException, ApplicationException
  {
    return new HBCIDauerauftragListJob((Konto)getContext());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    Konto k = (Konto) getContext();
    return i18n.tr("Konto {0}: Daueraufträge abrufen",k.getLongName());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new DauerauftragList().handleAction(getContext());
  }
}


/*********************************************************************
 * $Log: SynchronizeDauerauftragListJob.java,v $
 * Revision 1.3  2006/03/21 00:43:14  willuhn
 * @B bug 209
 *
 * Revision 1.2  2006/03/17 00:58:49  willuhn
 * @B typo
 *
 * Revision 1.1  2006/03/17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/