/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISepaDauerauftragDeleteJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISepaDauerauftragListJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaDauerauftragDelete;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Loeschen eines SEPA-Dauerauftrages.
 */
public class HBCISynchronizeJobSepaDauerauftragDelete extends SynchronizeJobSepaDauerauftragDelete implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    // Den brauchen wir, damit das Loeschen funktioniert.
    HBCISepaDauerauftragListJob list = new HBCISepaDauerauftragListJob(this.getKonto());
    list.setExclusive(true);
    
    // Das eigentliche Loeschen
    Date date = (Date) this.getContext(CTX_DATE);
    HBCISepaDauerauftragDeleteJob delete = new HBCISepaDauerauftragDeleteJob((SepaDauerauftrag)getContext(CTX_ENTITY),date);
    
    return new AbstractHBCIJob[] {list,delete};
  }
}
