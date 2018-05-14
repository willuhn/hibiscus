/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.kapott.hbci.GV_Result.GVRKontoauszug.Format;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.BPDUtil;
import de.willuhn.jameica.hbci.server.BPDUtil.Support;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIKontoauszugJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszugPdf;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Synchronize-Job fuer das Abrufen der Kontoauszuege im PDF-Format.
 */
public class HBCISynchronizeJobKontoauszugPdf extends SynchronizeJobKontoauszugPdf implements HBCISynchronizeJob
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    Konto k       = (Konto) this.getContext(CTX_ENTITY);
    Boolean force = (Boolean) this.getContext(CTX_FORCE);

    List<AbstractHBCIJob> jobs = new ArrayList<AbstractHBCIJob>();

    SynchronizeOptions o = new SynchronizeOptions(k);

    if (o.getSyncKontoauszuegePdf() || (force != null && force.booleanValue()))
      jobs.add(new HBCIKontoauszugJob(k));

    return jobs.toArray(new AbstractHBCIJob[jobs.size()]);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.AbstractSynchronizeJob#getName()
   */
  @Override
  public String getName() throws ApplicationException
  {
    try
    {
      Konto k = (Konto) this.getContext(CTX_ENTITY);

      String format = null;
      
      // Checken, ob wir PDF-Format unterstuetzen. Falls nicht, schreiben wir das
      // auch so in die Beschreibung des Jobs.
      Support support = BPDUtil.getSupport(k,BPDUtil.Query.KontoauszugPdf);
      if (support != null && support.isSupported())
      {
        format = "PDF";
      }
      else
      {
        support = BPDUtil.getSupport(k,BPDUtil.Query.Kontoauszug);
        if (support != null)
        {
          List<Format> formats = KontoauszugPdfUtil.getFormats(support.getBpd());

          // Wenn die Bank PDF nicht unterstuetzt, lassen wir den Parameter einfach weg
          if (formats.contains(Format.PDF))
            format = "PDF";
          else
            format = formats.get(0).name();
        }
      }
      
      if (format == null)
        return i18n.tr("{0}: Elektronische Kontoauszüge abrufen",k.getLongName());

      return i18n.tr("{0}: Elektr. Kontoauszüge im {1}-Format abrufen",k.getLongName(),format);
    }
    catch (Exception e)
    {
      Logger.error("unable to stringify job",e);
    }
    
    return i18n.tr("Unbekannter Auftrag");
}

}
