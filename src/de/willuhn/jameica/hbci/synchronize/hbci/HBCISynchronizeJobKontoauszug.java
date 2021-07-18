/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISaldoJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUmsatzJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Abrufen der Umsaetze und des Saldos eines Kontos.
 */
public class HBCISynchronizeJobKontoauszug extends SynchronizeJobKontoauszug implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    // BUGZILLA 346: Das bleibt weiterhin
    // ein Sync-Job, der aber je nach Konfiguration ggf.
    // nur Saldo oder nur Umsaetze abruft
    Konto k              = (Konto) this.getContext(CTX_ENTITY);
    Boolean forceSaldo   = (Boolean) this.getContext(CTX_FORCE_SALDO);
    Boolean forceUmsatz  = (Boolean) this.getContext(CTX_FORCE_UMSATZ);

    SynchronizeOptions o = new SynchronizeOptions(k);

    List<AbstractHBCIJob> jobs = new ArrayList<AbstractHBCIJob>();
    if (o.getSyncSaldo() || (forceSaldo != null && forceSaldo.booleanValue())) jobs.add(new HBCISaldoJob(k));
    if (o.getSyncKontoauszuege() || (forceUmsatz != null && forceUmsatz.booleanValue())) jobs.add(new HBCIUmsatzJob(k));

    return jobs.toArray(new AbstractHBCIJob[jobs.size()]);
  }

}
