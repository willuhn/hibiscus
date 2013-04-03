/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/HBCISynchronizeJobDauerauftragStore.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/06/30 15:23:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragStoreJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobDauerauftragStore;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren eines Dauerauftrages.
 */
public class HBCISynchronizeJobDauerauftragStore extends AbstractHBCISynchronizeJob implements SynchronizeJobDauerauftragStore
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

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    try
    {
      Dauerauftrag dauer = (Dauerauftrag) this.getContext(CTX_ENTITY);
      Konto k = dauer.getKonto();
      return i18n.tr("{0}: ({1}) {2} {3} an {4}, Turnus: {5}",k.getLongName(),dauer.getZweck(),HBCI.DECIMALFORMAT.format(dauer.getBetrag()),k.getWaehrung(),dauer.getGegenkontoName(),dauer.getTurnus().getBezeichnung());
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine job name",re);
      throw new ApplicationException(i18n.tr("Auftragsbezeichnung nicht ermittelbar: {0}",re.getMessage()));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring()
  {
    return false;
  }
}
