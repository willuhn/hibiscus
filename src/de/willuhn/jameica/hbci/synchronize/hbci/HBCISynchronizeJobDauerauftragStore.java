/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragStoreJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobDauerauftragStore;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren eines Dauerauftrages.
 */
public class HBCISynchronizeJobDauerauftragStore extends SynchronizeJobDauerauftragStore implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    // Den brauchen wir, damit das Aendern funktioniert.
    HBCIDauerauftragListJob list = new HBCIDauerauftragListJob(this.getKonto());
    list.setExclusive(true);
    
    // Das eigentliche Speichern/Aendern
    HBCIDauerauftragStoreJob store = new HBCIDauerauftragStoreJob((Dauerauftrag)getContext(CTX_ENTITY));
    
    return new AbstractHBCIJob[] {list,store};
  }
}
