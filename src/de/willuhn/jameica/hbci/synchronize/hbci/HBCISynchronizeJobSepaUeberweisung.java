/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/HBCISynchronizeJobSepaUeberweisung.java,v $
 * $Revision: 1.2 $
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
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIAuslandsUeberweisungJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaUeberweisung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen AuslandsUeberweisung.
 */
public class HBCISynchronizeJobSepaUeberweisung extends AbstractHBCISynchronizeJob implements SynchronizeJobSepaUeberweisung
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCIAuslandsUeberweisungJob((AuslandsUeberweisung) this.getContext(CTX_ENTITY))};
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    try
    {
      AuslandsUeberweisung ueb = (AuslandsUeberweisung) this.getContext(CTX_ENTITY);
      Konto k = ueb.getKonto();
      return i18n.tr("{0}: ({1}) {2} {3} an {4} überweisen",k.getLongName(),ueb.getZweck(),HBCI.DECIMALFORMAT.format(ueb.getBetrag()),k.getWaehrung(),ueb.getGegenkontoName());
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
