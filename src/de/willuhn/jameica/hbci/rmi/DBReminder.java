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

import de.willuhn.jameica.reminder.Reminder;

/**
 * Ein in der Datenbank gespeicherter Reminder.
 */
public interface DBReminder extends HibiscusDBObject
{
  /**
   * Liefert die UUID des Reminders.
   * @return die UUID des Reminders.
   * @throws RemoteException
   */
  public String getUUID() throws RemoteException;
  
  /**
   * Speichert die UUID des Reminders.
   * @param uuid die UUID des Reminders.
   * @throws RemoteException
   */
  public void setUUID(String uuid) throws RemoteException;
  
  /**
   * Liefert das zugehoerige Reminder-Objekt.
   * @return das zugehoerige Reminder-Objekt.
   * @throws RemoteException
   */
  public Reminder getReminder() throws RemoteException;
  
  /**
   * Speichert das zugehoerige Reminder-Objekt.
   * @param reminder das zugehoerige Reminder-Objekt.
   * @throws RemoteException
   */
  public void setReminder(Reminder reminder) throws RemoteException;
}



/**********************************************************************
 * $Log: DBReminder.java,v $
 * Revision 1.1  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 **********************************************************************/