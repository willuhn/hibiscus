/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeDauerauftragStoreJob.java,v $
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
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragDeleteJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobDauerauftragDelete;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Loeschen eines Dauerauftrages.
 */
public class HBCISynchronizeJobDauerauftragDelete extends AbstractHBCISynchronizeJob implements SynchronizeJobDauerauftragDelete
{
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJobs()
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

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    Dauerauftrag dauer = (Dauerauftrag) this.getContext(CTX_ENTITY);
    Konto k = this.getKonto();
    
    try
    {
      return i18n.tr("{0}: Dauerauftrag {1} {2} an {3} löschen",k.getLongName(),HBCI.DECIMALFORMAT.format(dauer.getBetrag()),k.getWaehrung(),dauer.getGegenkontoName());
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
