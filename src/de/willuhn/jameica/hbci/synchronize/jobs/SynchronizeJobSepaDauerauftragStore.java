/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Standard-Job zum Anlegen/Aendern eines SEPA-Dauerauftrages.
 */
public class SynchronizeJobSepaDauerauftragStore extends AbstractSynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    try
    {
      SepaDauerauftrag dauer = (SepaDauerauftrag) this.getContext(CTX_ENTITY);
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


