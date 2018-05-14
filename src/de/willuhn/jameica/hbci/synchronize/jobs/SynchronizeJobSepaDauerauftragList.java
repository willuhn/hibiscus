/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.gui.action.SepaDauerauftragList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Standard-Job zum Abrufen der SEPA-Dauerauftraege.
 */
public class SynchronizeJobSepaDauerauftragList extends AbstractSynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    try
    {
      Konto kt = (Konto) this.getContext(CTX_ENTITY);
      return i18n.tr("{0}: SEPA-Daueraufträge abrufen",kt.getLongName());
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
    new SepaDauerauftragList().handleAction(this.getContext(CTX_ENTITY));
  }

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring()
  {
    return true;
  }
}


