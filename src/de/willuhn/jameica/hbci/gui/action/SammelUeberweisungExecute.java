/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelUeberweisungExecute.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/07/04 09:16:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.Arrays;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSammelUeberweisung;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Action, die zur Ausfuehrung einer Sammel-Ueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>SammelUeberweisung</code> als Context.
 */
public class SammelUeberweisungExecute extends AbstractSammelTransferExecute
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractSammelTransferExecute#execute(de.willuhn.jameica.hbci.rmi.SammelTransfer)
   */
  void execute(final SammelTransfer transfer) throws ApplicationException, RemoteException
  {
    Konto konto = transfer.getKonto();
    Class type = SynchronizeJobSammelUeberweisung.class;

    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
    SynchronizeBackend backend = engine.getBackend(type,konto);
    SynchronizeJob job         = backend.create(type,konto);
    
    job.setContext(SynchronizeJob.CTX_ENTITY,transfer);
    
    backend.execute(Arrays.asList(job));
  }

}
