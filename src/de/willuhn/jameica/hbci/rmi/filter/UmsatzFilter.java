/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/filter/Attic/UmsatzFilter.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/12/05 17:20:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi.filter;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.kapott.hbci.GV_Result.GVRKUms;

import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Interface fuer einen Umsatz-Filter.
 * Alle Klassen, die dieses Interface implementieren, werden von
 * Hibiscus automatisch geladen und beim Eintreffen neuer Umsaetze
 * informiert.
 */
public interface UmsatzFilter extends Remote
{
  /**
   * Ueber diese Funktion wird der Umsatz-Filter ueber das Eintreffen eines
   * neuen Umsatzes informiert.
   * HINWEIS. Hibiscus prueft nicht, ob der Umsatz schonmal gefiltert wurde.
   * Es wird also mit Sicherheits vorkommen, dass der Umsatzfilter einen
   * Umsatz erhaelt, ueber den er schonmal informiert wurde. Es ist Aufgabe
   * des Umsatzfilters, diese ggf. zu ignorieren.
   * @param umsatz der abgerufene Umsatz.
   * @param rawData die zugehoerigen Roh-Daten aus HBCI4Java.
   * Warnung: Der Parameter <code>rawData</code> kann durchaus <code>null</code>
   * sein. Naemlich genau dann, wenn das Filtern der Umsaetze manuell vom
   * Benutzer angestossen wird. In dem Fall existieren die originalen
   * HBCI4Java-Daten nicht mehr. Die implementierende Klasse muss also damit
   * rechnen, dass <code>rawData</code> <code>null</code> ist.
   * @throws RemoteException
   */
  public void filter(Umsatz umsatz, GVRKUms.UmsLine rawData) throws RemoteException;
}


/**********************************************************************
 * $Log: UmsatzFilter.java,v $
 * Revision 1.1  2005/12/05 17:20:40  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.1  2005/05/09 23:47:24  web0
 * @N added first code for the filter framework
 *
 **********************************************************************/