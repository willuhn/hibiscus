/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/SynchronizeJobProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/05/16 11:32:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericIterator;

/**
 * Provider zur Ermittlung von HBCI-Synchronisierungsjobs.
 * Hiermit ist es moeglich, die Liste der zu synchronisierenden
 * HBCI-Jobs zu erweitern, indem man dieses Interface implementiert.
 * Das Interface implementiert bewusst <code>Comparable</code>,
 * damit die Liste der Synchronisierungsjobs in einer definierten
 * Reihenfolge ausgefuehrt werden konnen.
 * Das Abrufen der Umsaetze und Saldos sollte zum Beispiel erst
 * nach dem Senden von faelligen Ueberweisungen erfolgen. Also sollte
 * die Saldo/Umsatz-Implementierung des JobProviders so implementiert
 * sein, dass es sich beim Sortieren ganz nach hinten einsortiert.
 * Alle Implementierungen muessen einen parameterlosen Konstruktor
 * mit dem <code>public</code>-Modifier besitzen (Bean-Spezifikation),
 * um automatisch erkannt zu werden. 
 */
public interface SynchronizeJobProvider extends Comparable
{
  /**
   * Liefert eine Liste der auszufuehrenden HBCI-Synchronisierungsjobs auf dem angegebenen Konto.
   * @param k das Konto.
   * Es werden nur Konten uebergeben, die fuer die Synchronsisierung freigeschaltet sind.
   * @return Liste der auszufuehrenden Jobs. Die Objekte muessen vom Typ <code>SynchronizeJob</code> sein.
   * Wenn fuer das Konto keine Jobs auszufuehren sind, kann die Funktion <code>null</code> zurueckliefern.
   * @throws RemoteException
   */
  public GenericIterator getSynchronizeJobs(Konto k) throws RemoteException;
}


/*********************************************************************
 * $Log: SynchronizeJobProvider.java,v $
 * Revision 1.1  2007/05/16 11:32:30  willuhn
 * @N Redesign der SynchronizeEngine. Ermittelt die HBCI-Jobs jetzt ueber generische "SynchronizeJobProvider". Damit ist die Liste der Sync-Jobs erweiterbar
 *
 **********************************************************************/