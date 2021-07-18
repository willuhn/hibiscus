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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Standard-Job zum Loeschen eines SEPA-Dauerauftrages.
 */
public class SynchronizeJobSepaDauerauftragDelete extends AbstractSynchronizeJob
{
  /**
   * Context-Key fuer das Ziel-Datum zum Loeschen des Dauerauftrages.
   * Der Wert des Keys muss vom Typ {@link java.util.Date} sein.
   */
  public final static String CTX_DATE = "ctx.da.delete.date";

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    SepaDauerauftrag dauer = (SepaDauerauftrag) this.getContext(CTX_ENTITY);
    Konto k = this.getKonto();

    try
    {
      return i18n.tr("{0}: SEPA-Dauerauftrag {1} {2} an {3} löschen",k.getLongName(),HBCI.DECIMALFORMAT.format(dauer.getBetrag()),k.getWaehrung(),dauer.getGegenkontoName());
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
