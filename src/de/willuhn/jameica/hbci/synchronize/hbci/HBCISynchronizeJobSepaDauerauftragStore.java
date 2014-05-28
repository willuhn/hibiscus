/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISepaDauerauftragListJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISepaDauerauftragStoreJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaDauerauftragStore;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren eines SEPA-Dauerauftrages.
 */
public class HBCISynchronizeJobSepaDauerauftragStore extends SynchronizeJobSepaDauerauftragStore implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    // Den brauchen wir, damit das Aendern funktioniert.
    HBCISepaDauerauftragListJob list = new HBCISepaDauerauftragListJob(this.getKonto());
    list.setExclusive(true);
    
    // Das eigentliche Speichern/Aendern
    HBCISepaDauerauftragStoreJob store = new HBCISepaDauerauftragStoreJob((SepaDauerauftrag)getContext(CTX_ENTITY));
    
    return new AbstractHBCIJob[] {list,store};
  }
}
