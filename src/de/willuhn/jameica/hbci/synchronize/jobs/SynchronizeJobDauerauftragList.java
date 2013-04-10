/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.gui.action.DauerauftragList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Standard-Job zum Abrufen der Dauerauftraege.
 */
public class SynchronizeJobDauerauftragList extends AbstractSynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    try
    {
      Konto kt = (Konto) this.getContext(CTX_ENTITY);
      return i18n.tr("{0}: Daueraufträge abrufen",kt.getLongName());
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine job name",re);
      throw new ApplicationException(i18n.tr("Auftragsbezeichnung nicht ermittelbar: {0}",re.getMessage()));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.AbstractSynchronizeJob#configure()
   */
  public void configure() throws ApplicationException
  {
    // Ueberschrieben, weil wir hier stattdessen die _Liste_ der Dauerauftraege anzeigen wollen
    new DauerauftragList().handleAction(this.getContext(CTX_ENTITY));
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring()
  {
    return true;
  }
}


