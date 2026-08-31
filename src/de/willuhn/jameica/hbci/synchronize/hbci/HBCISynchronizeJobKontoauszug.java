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

import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.KontoType;
import de.willuhn.jameica.hbci.server.BPDUtil;
import de.willuhn.jameica.hbci.server.BPDUtil.Query;
import de.willuhn.jameica.hbci.server.BPDUtil.Support;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIKreditkartenUmsatzJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISaldoJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUmsatzJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.TypedProperties;

/**
 * Ein Synchronize-Job fuer das Abrufen der Umsaetze und des Saldos eines Kontos.
 */
public class HBCISynchronizeJobKontoauszug extends SynchronizeJobKontoauszug implements HBCISynchronizeJob
{
  @Override
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    // BUGZILLA 346: Das bleibt weiterhin
    // ein Sync-Job, der aber je nach Konfiguration ggf.
    // nur Saldo oder nur Umsaetze abruft
    Konto k              = (Konto) this.getContext(CTX_ENTITY);
    Boolean forceSaldo   = (Boolean) this.getContext(CTX_FORCE_SALDO);
    Boolean forceUmsatz  = (Boolean) this.getContext(CTX_FORCE_UMSATZ);
    
    SynchronizeOptions o = new SynchronizeOptions(k);
    
    List<AbstractHBCIJob> jobs = new ArrayList<AbstractHBCIJob>();
    if (o.getSyncSaldo() || (forceSaldo != null && forceSaldo.booleanValue())) jobs.add(new HBCISaldoJob(k));
    if (o.getSyncKontoauszuege() || (forceUmsatz != null && forceUmsatz.booleanValue()))
    {
      Support support = getDkkkuSupport(k);
      if (support != null)
      {
        TypedProperties bpd = support.getBpd();
        int timeRange = bpd != null ? bpd.getInt("timerange",0) : 0;
        boolean canMaxEntries = bpd != null && bpd.getBoolean("canmaxentries",false);
        Integer maxEntries = canMaxEntries ? Integer.valueOf(HBCIKreditkartenUmsatzJob.DEFAULT_MAX_ENTRIES) : null;
        jobs.add(new HBCIKreditkartenUmsatzJob(k,timeRange,maxEntries));
      }
      else
      {
        jobs.add(new HBCIUmsatzJob(k));
      }
    }

    return jobs.toArray(new AbstractHBCIJob[0]);
  }

  private static Support getDkkkuSupport(Konto konto) throws RemoteException
  {
    if (!isCreditCardAccount(konto))
      return null;

    Support support = BPDUtil.getSupport(konto,Query.KreditkartenUmsatz);
    return support != null && support.isSupported() ? support : null;
  }

  private static boolean isCreditCardAccount(Konto konto) throws RemoteException
  {
    if (konto == null)
      return false;

    KontoType type = KontoType.find(konto.getAccountType());
    if (type == KontoType.KREDITKARTE)
      return true;

    // Einige Institute liefern fuer Kartenkonten keinen Kontotyp. Eine
    // Kartennummer hat mindestens 12 Stellen, nationale Kontonummern hoechstens 10.
    String number = konto.getKontonummer();
    if (number == null)
      return false;
    number = number.replace(" ","").replace("-","");
    return number.matches("[0-9]{12,19}");
  }

}
