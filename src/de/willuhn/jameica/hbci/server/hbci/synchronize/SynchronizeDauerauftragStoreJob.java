/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeDauerauftragStoreJob.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/06/15 11:20:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.synchronize;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragStoreJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren eines Dauerauftrages.
 */
public class SynchronizeDauerauftragStoreJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param dauer
   */
  public SynchronizeDauerauftragStoreJob(Dauerauftrag dauer)
  {
    super(dauer);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCIDauerauftragStoreJob((Dauerauftrag)getContext())};
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    Dauerauftrag dauer = (Dauerauftrag) getContext();
    Konto k = dauer.getKonto();
    String[] params = new String[] {
        k.getLongName(),
        dauer.getZweck(),
        HBCI.DECIMALFORMAT.format(dauer.getBetrag()),
        k.getWaehrung(),
        dauer.getGegenkontoName(),
        dauer.getTurnus().getBezeichnung()
       };
    return i18n.tr("{0}: ({1}) {2} {3} an {4}, Turnus: {5}",params);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new DauerauftragNew().handleAction(getContext());
  }
}


/*********************************************************************
 * $Log: SynchronizeDauerauftragStoreJob.java,v $
 * Revision 1.3  2007/06/15 11:20:32  willuhn
 * @N Saldo in Kontodetails via Messaging sofort aktualisieren
 * @N Mehr Details in den Namen der Synchronize-Jobs
 * @N Layout der Umsatzdetail-Anzeige ueberarbeitet
 *
 * Revision 1.2  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.1  2006/03/17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/