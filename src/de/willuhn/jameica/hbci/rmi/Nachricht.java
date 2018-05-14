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
import java.util.Date;

/**
 * Interface fuer eine System-Nachricht der Bank.
 */
public interface Nachricht extends HibiscusDBObject
{
  /**
   * Liefert die BLZ dieser Nachricht.
   * @return BLZ
   * @throws RemoteException
   */
  public String getBLZ() throws RemoteException;

  /**
   * Liefert das Datum der Nachricht.
   * @return Datum
   * @throws RemoteException
   */
  public Date getDatum() throws RemoteException;

  /**
   * Prueft, ob die Nachricht bereits gelesen wurde.
   * @return true, wenn sie gelesen wurde.
   * @throws RemoteException
   */
  public boolean isGelesen() throws RemoteException;

  /**
   * Liefert den Nachrichtentext.
   * @return Nachrichtentext.
   * @throws RemoteException
   */
  public String getNachricht() throws RemoteException;

  /**
   * Speichert die BLZ.
   * @param blz
   * @throws RemoteException
   */
  public void setBLZ(String blz) throws RemoteException;

  /**
   * Speichert das Datum.
   * @param datum
   * @throws RemoteException
   */
  public void setDatum(Date datum) throws RemoteException;

  /**
   * Markiert die Nachricht als gelesen/ungelesen.
   * @param b
   * @throws RemoteException
   */
  public void setGelesen(boolean b) throws RemoteException;

  /**
   * Speichert den Nachrichtentext.
   * @param nachricht
   * @throws RemoteException
   */
  public void setNachricht(String nachricht) throws RemoteException;

}


/**********************************************************************
 * $Log: Nachricht.java,v $
 * Revision 1.2  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/