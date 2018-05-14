/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer den Datenbank-Service von Hibiscus.
 * @author willuhn
 */
public interface HBCIDBService extends DBService
{
  /**
   * Einstellungen fuer die DB-Services.
   */
  public final static Settings SETTINGS = new Settings(HBCIDBService.class);

  /**
   * Initialisiert/erzeugt die Datenbank.
   * @throws RemoteException Wenn beim Initialisieren ein Fehler auftrat.
   */
  public void install() throws RemoteException;
  
  /**
   * Checkt die Konsistenz der Datenbank und fuehrt bei Bedarf Updates durch.
   * @throws RemoteException Wenn es beim Pruefen der Datenbank-Konsistenz zu einem Fehler kam.
   * @throws ApplicationException wenn die Datenbank-Konsistenz nicht gewaehrleistet ist.
   */
  public void checkConsistency() throws RemoteException, ApplicationException;
  
  /**
   * Liefert den verwendeten Treiber.
   * @return der Treiber.
   * @throws RemoteException
   */
  public DBSupport getDriver() throws RemoteException;
  
  /**
   * Liefert den Namen der SQL-Funktion, mit der die Datenbank aus einem DATE-Feld einen UNIX-Timestamp macht.
   * Bei MySQL ist das z.Bsp. "UNIX_TIMESTAMP".
   * @param content der Feld-Name.
   * @return Name der SQL-Funktion samt Parameter. Also zum Beispiel "TONUMBER(datum)".
   * @throws RemoteException
   */
  public String getSQLTimestamp(String content) throws RemoteException;
  
  /**
   * Fuehrt ein Update/Delete-Statement durch.
   * @param query das Query.
   * @param params die Parameter.
   * @return die Anzahl der betroffenen Datensaetze.
   * @throws RemoteException
   */
  public int executeUpdate(String query, String... params) throws RemoteException;

}
