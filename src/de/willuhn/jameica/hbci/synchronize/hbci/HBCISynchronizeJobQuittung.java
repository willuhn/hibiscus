/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIQuittungJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobQuittung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Synchronize-Job fuer das Senden von Empfangsquittungen.
 */
public class HBCISynchronizeJobQuittung extends SynchronizeJobQuittung implements HBCISynchronizeJob
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    Kontoauszug ka = (Kontoauszug) this.getContext(CTX_ENTITY);
    return new AbstractHBCIJob[]{new HBCIQuittungJob(ka)};
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.AbstractSynchronizeJob#getName()
   */
  @Override
  public String getName() throws ApplicationException
  {
    try
    {
      Kontoauszug ka = (Kontoauszug) this.getContext(CTX_ENTITY);
      Konto k        = ka.getKonto();
      Integer jahr   = ka.getJahr();
      Integer nr     = ka.getNummer();
      
      if (jahr != null && nr != null)
        return i18n.tr("{0}: Empfang von Kontoauszug {1}-{2} quittieren",k.getLongName(),Integer.toString(jahr),Integer.toString(nr));
      
      return i18n.tr("{0}: Empfang von Kontoauszug quittieren",k.getLongName());
    }
    catch (Exception e)
    {
      Logger.error("unable to stringify job",e);
    }
    return i18n.tr("Unbekannter Auftrag");
  }
}
