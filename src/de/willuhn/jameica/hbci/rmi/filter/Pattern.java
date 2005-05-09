/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/filter/Attic/Pattern.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/09 23:47:24 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi.filter;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface fuer ein einzelnes Filter-Kriterium.
 */
public interface Pattern extends DBObject
{
  
  /**
   * Filter-Typ: Feld stimmt mit Pattern genau ueberein.
   */
  public final static int TYPE_EQUALS     = 1;
  
  /**
   * Filter-Typ: Feld enthaelt den Pattern.
   */
  public final static int TYPE_CONTAINS   = 2;
  
  /**
   * Filter-Typ: Feld beginnt mit Pattern.
   */
  public final static int TYPE_STARTSWITH = 3;
  
  /**
   * Filter-Typ: Feld endet mit Pattern.
   */
  public final static int TYPE_ENDSWITH   = 4;

  /**
   * Liefert den Namen des Umsatz-Attributes, mit dem verglichen werden soll.
   * @return Name des Attributes.
   * @throws RemoteException
   */
  public String getField() throws RemoteException;

  /**
   * Speichert den Namen des Umsatz-Attributes, mit dem verglichen werden soll.
   * @param field
   * @throws RemoteException
   */
  public void setField(String field) throws RemoteException;

  /**
   * Liefert den Pattern, nach dem gesucht werden soll.
   * @return Pattern.
   * @throws RemoteException
   */
  public String getPattern() throws RemoteException;

  /**
   * Speichert den Pattern, nach dem gesucht werden soll.
   * @param pattern.
   * @throws RemoteException
   */
  public void setPattern(String pattern) throws RemoteException;

  /**
   * Liefert den Typ des Patterns.
   * @see Pattern#TYPE_EQUALS
   * @see Pattern#TYPE_STARTSWITH
   * @see Pattern#TYPE_CONTAINS
   * @see Pattern#TYPE_ENDSWITH
   * @return Typ des Patterns.
   * @throws RemoteException
   */
  public int getType() throws RemoteException;

  /**
   * Speichert den Typ des Patterns.
   * @param type Typ.
   * @throws RemoteException
   */
  public void setType(int type) throws RemoteException;

  /**
   * Prueft, ob Gross-Kleinschreibung ignoriert werden soll.
   * @return <code>true</code>, wenn sie ignoriert werden soll.
   * @throws RemoteException
   */
  public boolean ignoreCase() throws RemoteException;

  /**
   * Legt fest, ob Gross-Kleinschreibung ignoriert werden soll.
   * @param b <code>true</code> wenn sie ignoriert werden soll.
   * @throws RemoteException
   */
  public void setIgnoreCase(boolean b) throws RemoteException;
}


/**********************************************************************
 * $Log: Pattern.java,v $
 * Revision 1.1  2005/05/09 23:47:24  web0
 * @N added first code for the filter framework
 *
 **********************************************************************/