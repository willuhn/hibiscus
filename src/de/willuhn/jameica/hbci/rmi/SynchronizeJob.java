/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/SynchronizeJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/03/16 18:23:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;

/**
 * Interface fuer einen einzelnen Synchronisierungs-Job.
 * @author willuhn
 */
public interface SynchronizeJob extends GenericObject
{
  /**
   * Erzeugt einen HBCI-Job basierend auf dem SynchronizeJob.
   * @return der erzeugte HBCI-Job.
   * @throws RemoteException
   */
  public AbstractHBCIJob createHBCIJob() throws RemoteException;
}


/*********************************************************************
 * $Log: SynchronizeJob.java,v $
 * Revision 1.1  2006/03/16 18:23:36  willuhn
 * @N first code for new synchronize system
 *
 *********************************************************************/