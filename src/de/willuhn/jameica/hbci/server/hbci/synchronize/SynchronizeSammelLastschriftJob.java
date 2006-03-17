/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeSammelLastschriftJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/03/17 00:51:24 $
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

import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISammelLastschriftJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen Sammel-Lastschrift.
 */
public class SynchronizeSammelLastschriftJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param last
   */
  public SynchronizeSammelLastschriftJob(SammelLastschrift last)
  {
    super(last);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJob()
   */
  public AbstractHBCIJob createHBCIJob() throws RemoteException, ApplicationException
  {
    return new HBCISammelLastschriftJob((SammelLastschrift)getContext());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    SammelLastschrift last = (SammelLastschrift) getContext();
    Konto k = last.getKonto();
    return i18n.tr("Konto {0}: Sammel-Lastschrift {1} einziehen",new String[]{k.getLongName(), last.getBezeichnung()});
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new SammelLastschriftNew().handleAction(getContext());
  }
}


/*********************************************************************
 * $Log: SynchronizeSammelLastschriftJob.java,v $
 * Revision 1.1  2006/03/17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/