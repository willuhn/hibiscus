/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeUeberweisungJob.java,v $
 * $Revision: 1.6 $
 * $Date: 2008/04/13 04:20:41 $
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
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUeberweisungJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen Ueberweisung.
 */
public class SynchronizeUeberweisungJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param ueb
   */
  public SynchronizeUeberweisungJob(Ueberweisung ueb)
  {
    super(ueb);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCIUeberweisungJob((Ueberweisung)getContext())};
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    Ueberweisung ueb = (Ueberweisung) getContext();
    Konto k = ueb.getKonto();
    if (ueb.isTerminUeberweisung())
    {
      String[] params = new String[] {
          k.getLongName(),
          ueb.getZweck(),
          HBCI.DECIMALFORMAT.format(ueb.getBetrag()),
          k.getWaehrung(),
          HBCI.DATEFORMAT.format(ueb.getTermin()),
          ueb.getGegenkontoName()
         };
      return i18n.tr("{0}: ({1}) {2} {3} per {4} an {5} überweisen",params);
    }
    String[] params = new String[] {
        k.getLongName(),
        ueb.getZweck(),
        HBCI.DECIMALFORMAT.format(ueb.getBetrag()),
        k.getWaehrung(),
        ueb.getGegenkontoName()
       };
    return i18n.tr("{0}: ({1}) {2} {3} an {4} überweisen",params);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new UeberweisungNew().handleAction(getContext());
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring() throws RemoteException
  {
    return false;
  }
}


/*********************************************************************
 * $Log: SynchronizeUeberweisungJob.java,v $
 * Revision 1.6  2008/04/13 04:20:41  willuhn
 * @N Bug 583
 *
 * Revision 1.5  2007/12/03 13:14:09  willuhn
 * @N Bei Termin-Ueberweisungen Termin mit anzeigen
 *
 * Revision 1.4  2007/06/15 11:20:32  willuhn
 * @N Saldo in Kontodetails via Messaging sofort aktualisieren
 * @N Mehr Details in den Namen der Synchronize-Jobs
 * @N Layout der Umsatzdetail-Anzeige ueberarbeitet
 *
 * Revision 1.3  2007/04/02 23:01:17  willuhn
 * @D diverse Javadoc-Warnings
 * @C Umstellung auf neues SelectInput
 *
 * Revision 1.2  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.1  2006/03/17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/