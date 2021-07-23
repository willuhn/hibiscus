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
  @Override
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    // Den brauchen wir, damit das Aendern funktioniert.
    HBCISepaDauerauftragListJob listBefore = new HBCISepaDauerauftragListJob(this.getKonto());
    listBefore.setExclusive(true);
    
    // Das eigentliche Speichern/Aendern
    HBCISepaDauerauftragStoreJob store = new HBCISepaDauerauftragStoreJob((SepaDauerauftrag)getContext(CTX_ENTITY));

    // Und danach nochmal, um die Aenderungen gleich abzurufen
    HBCISepaDauerauftragListJob listAfter = new HBCISepaDauerauftragListJob(this.getKonto());
    listAfter.setExclusive(true);

    return new AbstractHBCIJob[] {listBefore,store,listAfter};
  }
}
