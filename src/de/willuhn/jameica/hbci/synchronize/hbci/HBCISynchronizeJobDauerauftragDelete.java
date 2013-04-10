/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragDeleteJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobDauerauftragDelete;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Loeschen eines Dauerauftrages.
 */
public class HBCISynchronizeJobDauerauftragDelete extends SynchronizeJobDauerauftragDelete implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    // Den brauchen wir, damit das Loeschen funktioniert.
    HBCIDauerauftragListJob list = new HBCIDauerauftragListJob(this.getKonto());
    list.setExclusive(true);
    
    // Das eigentliche Loeschen
    Date date = (Date) this.getContext(CTX_DATE);
    HBCIDauerauftragDeleteJob delete = new HBCIDauerauftragDeleteJob((Dauerauftrag)getContext(CTX_ENTITY),date);
    
    return new AbstractHBCIJob[] {list,delete};
  }
}
