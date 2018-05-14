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

import de.willuhn.datasource.rmi.DBObject;

/**
 * Basis-Interface fuer (fast (ausser DBProperty und Version)) alle Entity-Klassen in Hibiscus.
 */
public interface HibiscusDBObject extends DBObject
{
  /**
   * Liefert den Wert eines Meta-Attributes.
   * @param name Name des Meta-Attributes.
   * @param defaultValue der Default-Wert.
   * @return der Wert des Attributes.
   * @throws RemoteException
   */
  public String getMeta(String name, String defaultValue) throws RemoteException;
  
  /**
   * Speichert den Wert des Meta-Attributes.
   * Die Aenderung wird sofort in die Datenbank uebernommen.
   * Der Aufruf von "store()" ist nicht noetig. Wenn man das Setzen des Wertes
   * in bestimmten Situationen rueckgaengig machen will, dann muss der Aufruf
   * innerhalb einer Transaktion stehen. Beim Rollback wird auch das Meta-Attribute
   * wieder geloescht.
   * 
   * Weiterhin wichtig: Das Objekt muss sich bereits in der Datenbank befinden
   * (also eine ID haben) - andernfalls koennen die Meta-Informationen ja nicht korrekt
   * zugeordnet zugeordnet werden.
   * @param name Name des Meta-Attributes.
   * @param value Wert des Attributes.
   * @throws RemoteException
   */
  public void setMeta(String name, String value) throws RemoteException;
}



/**********************************************************************
 * $Log: HibiscusDBObject.java,v $
 * Revision 1.1  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 **********************************************************************/