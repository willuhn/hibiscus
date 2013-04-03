/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/HBCISynchronizeJobUeberweisung.java,v $
 * $Revision: 1.7 $
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUeberweisungJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobUeberweisung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen Ueberweisung.
 */
public class HBCISynchronizeJobUeberweisung extends AbstractHBCISynchronizeJob implements SynchronizeJobUeberweisung
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCIUeberweisungJob((Ueberweisung) this.getContext(CTX_ENTITY))};
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    try
    {
      Ueberweisung ueb = (Ueberweisung) this.getContext(CTX_ENTITY);
      Konto k = ueb.getKonto();
      if (ueb.isTerminUeberweisung())
        return i18n.tr("{0}: ({1}) {2} {3} per {4} an {5} überweisen",k.getLongName(),ueb.getZweck(),HBCI.DECIMALFORMAT.format(ueb.getBetrag()),k.getWaehrung(),HBCI.DATEFORMAT.format(ueb.getTermin()),ueb.getGegenkontoName());
      
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
