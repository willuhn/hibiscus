/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Standard-Job zur Ausfuehrung von SEPA-Ueberweisungen.
 */
public class SynchronizeJobSepaUeberweisung extends AbstractSynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    try
    {
      AuslandsUeberweisung ueb = (AuslandsUeberweisung) this.getContext(CTX_ENTITY);
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


