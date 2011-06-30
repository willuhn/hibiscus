/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/SynchronizeJob.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/06/30 15:23:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer einen einzelnen Synchronisierungs-Job.
 * @author willuhn
 * @param <T> Typ dex Jobs.
 */
public interface SynchronizeJob<T extends GenericObject> extends GenericObject
{
  /**
   * Erzeugt einen oder mehrere HBCI-Jobs basierend auf dem SynchronizeJob.
   * @return der/die erzeugten HBCI-Jobs.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException;
  
  /**
   * Liefert einen sprechenden Namen fuer den Job.
   * @return sprechender Name.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Oeffnet den Synchronisierungs-Job zur Konfiguration.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void configure() throws RemoteException, ApplicationException;
  
  /**
   * Prueft, ob es sich um einen wiederkehrenden Job handelt.
   * Saldo- und Umsatzabfragen sind zBsp wiederkehrend, Ueberweisungen
   * jedoch nicht.
   * BUGZILLA 583
   * @return true, wenn es sich um einen wiederholenden Job handelt.
   * @throws RemoteException
   */
  public boolean isRecurring() throws RemoteException;
  
  /**
   * Liefert das Context-Objekt des Jobs.
   * @return das Context-Objekt des Jobs.
   * @throws RemoteException
   */
  public T getContext() throws RemoteException;
}


/*********************************************************************
 * $Log: SynchronizeJob.java,v $
 * Revision 1.5  2011/06/30 15:23:22  willuhn
 * @N Synchronize-Jobs getypt
 *
 * Revision 1.4  2008/04/13 04:20:41  willuhn
 * @N Bug 583
 *
 * Revision 1.3  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.2  2006/03/17 00:51:25  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 * Revision 1.1  2006/03/16 18:23:36  willuhn
 * @N first code for new synchronize system
 *
 *********************************************************************/