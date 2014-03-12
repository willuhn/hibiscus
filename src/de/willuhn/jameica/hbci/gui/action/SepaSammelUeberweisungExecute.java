/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaSammelUeberweisung;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Action, die zur Ausfuehrung einer SEPA-Sammelueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>SepaSammelUeberweisung</code> als Context.
 */
public class SepaSammelUeberweisungExecute extends AbstractSepaSammelTransferExecute
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractSepaSammelTransferExecute#execute(de.willuhn.jameica.hbci.rmi.SepaSammelTransfer)
   */
  void execute(final SepaSammelTransfer transfer) throws ApplicationException, RemoteException
  {
    Konto konto = transfer.getKonto();
    Class<SynchronizeJobSepaSammelUeberweisung> type = SynchronizeJobSepaSammelUeberweisung.class;

    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
    SynchronizeBackend backend = engine.getBackend(type,konto);
    SynchronizeJob job         = backend.create(type,konto);
    
    job.setContext(SynchronizeJob.CTX_ENTITY,transfer);
    
    backend.execute(Arrays.asList(job));
  }

}
