/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Standard-Job-Implementierung zum Abrufen der Kontoauszuege.
 */
public class SynchronizeJobKontoauszug extends AbstractSynchronizeJob
{
  /**
   * Context-Key fuer das forcierte Abrufen des Saldos. Auch dann, wenn es
   * in den Synchronisierungsoptionen deaktiviert ist. Der Wert des Keys
   * muss vom Typ {@link Boolean} sein.
   */
  public final static String CTX_FORCE_SALDO = "ctx.konto.saldo.force";

  /**
   * Context-Key fuer das forcierte Abrufen der Umsaetze. Auch dann, wenn es
   * in den Synchronisierungsoptionen deaktiviert ist. Der Wert des Keys
   * muss vom Typ {@link Boolean} sein.
   */
  public final static String CTX_FORCE_UMSATZ = "ctx.konto.umsatz.force";

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#getName()
   */
  public String getName() throws ApplicationException
  {
    try
    {
      Konto k = (Konto) this.getContext(CTX_ENTITY);
      SynchronizeOptions o = new SynchronizeOptions(k);
      
      String s = "{0}: ";
      
      if (o.getSyncKontoauszuege())
        s += "Kontoauszüge";
      if (o.getSyncSaldo())
      {
        if (o.getSyncKontoauszuege())
          s += "/";
        s += "Salden";
      }
      s += " abrufen";
      return i18n.tr(s,k.getLongName());
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
    return true;
  }

}


