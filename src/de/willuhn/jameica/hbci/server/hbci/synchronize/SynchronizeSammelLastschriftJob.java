/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeSammelLastschriftJob.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/04/02 23:01:17 $
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

import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISammelLastschriftJob;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen Sammel-Lastschrift.
 */
public class SynchronizeSammelLastschriftJob extends AbstractSynchronizeJob
{

  /**
   * ct.
   * @param last
   */
  public SynchronizeSammelLastschriftJob(SammelLastschrift last)
  {
    super(last);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCISammelLastschriftJob((SammelLastschrift)getContext())};
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#getName()
   */
  public String getName() throws RemoteException
  {
    SammelLastschrift last = (SammelLastschrift) getContext();
    Konto k = last.getKonto();
    return i18n.tr("Konto {0}: Sammel-Lastschrift {1} einziehen",new String[]{k.getLongName(), last.getBezeichnung()});
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJob#configure()
   */
  public void configure() throws RemoteException, ApplicationException
  {
    new SammelLastschriftNew().handleAction(getContext());
  }
}


/*********************************************************************
 * $Log: SynchronizeSammelLastschriftJob.java,v $
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