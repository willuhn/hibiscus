/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.Arrays;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaSammelLastschrift;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Action, die zur Ausfuehrung einer SEPA-Sammellastschrift verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>SepaSammelLastschrift</code> als Context.
 */
public class SepaSammelLastschriftExecute extends AbstractSepaSammelTransferExecute
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractSepaSammelTransferExecute#execute(de.willuhn.jameica.hbci.rmi.SepaSammelTransfer)
   */
  void execute(final SepaSammelTransfer transfer) throws ApplicationException, RemoteException
  {
    Konto konto = transfer.getKonto();
    Class<SynchronizeJobSepaSammelLastschrift> type = SynchronizeJobSepaSammelLastschrift.class;

    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
    SynchronizeBackend backend = engine.getBackend(type,konto);
    SynchronizeJob job         = backend.create(type,konto);
    
    job.setContext(SynchronizeJob.CTX_ENTITY,transfer);
    
    backend.execute(Arrays.asList(job));
  }

}
