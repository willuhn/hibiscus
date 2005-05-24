/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/OffenerPosten.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/24 23:30:03 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.rmi.filter.Filter;

/**
 * Interface fuer einen offenen Posten.
 */
public interface OffenerPosten extends DBObject, Filter
{
  /**
   * Liefert eine sprechende Bezeichnung.
   * @return Bezeichnung.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;
  
  /**
   * Speichert die Bezeichnung.
   * @param bezeichnung
   * @throws RemoteException
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException;

  /**
   * Prueft, ob der Offene Posten gar keiner mehr ist ;). 
   * @return true, wenn der Posten noch offen ist.
   * @throws RemoteException
   */
  public boolean isOffen() throws RemoteException;

  /**
   * Liefert das Datum, an dem der Umsatz zugewiesen und damit
   * der Offene Posten geschlossen wurde.
   * @return Datum der Zuweisung.
   * @throws RemoteException
   */
  public Date getDatum() throws RemoteException;

  /**
   * Liefert den zugewiesenen Umsatz oder null wenn noch keiner existiert.
   * @return der Umsatz.
   * @throws RemoteException
   */
  public Umsatz getUmsatz() throws RemoteException;

  /**
   * Weist den Umsatz zu.
   * Ab diesem Zeitpunkt gilt der OP nicht mehr als offen.
   * @param umsatz
   * @throws RemoteException
   */
  public void setUmsatz(Umsatz umsatz) throws RemoteException;  
}


/**********************************************************************
 * $Log: OffenerPosten.java,v $
 * Revision 1.1  2005/05/24 23:30:03  web0
 * @N Erster Code fuer OP-Verwaltung
 *
 **********************************************************************/