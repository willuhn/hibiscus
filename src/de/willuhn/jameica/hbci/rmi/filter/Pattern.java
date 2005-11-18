/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/filter/Attic/Pattern.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/11/18 00:43:29 $
 * $Author: willuhn $
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
  public final static int TYPE_EQUALS     = 0;
  
  /**
   * Filter-Typ: Feld enthaelt den Pattern.
   */
  public final static int TYPE_CONTAINS   = 1;
  
  /**
   * Filter-Typ: Feld beginnt mit Pattern.
   */
  public final static int TYPE_STARTSWITH = 2;
  
  /**
   * Filter-Typ: Feld endet mit Pattern.
   */
  public final static int TYPE_ENDSWITH   = 3;

  /**
   * Liefert den Namen des Attributes, mit dem verglichen werden soll.
   * @return Name des Attributes.
   * @throws RemoteException
   */
  public String getField() throws RemoteException;

  /**
   * Speichert den Namen des Attributes, mit dem verglichen werden soll.
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
   * @param pattern Pattern, nach dem gesucht werden soll.
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
   * Liefert eine sprechenden lokalisierte Bezeichnung fuer den Typ.
   * @param type Typ.
   * @return Bezeichnung.
   * @throws RemoteException
   */
  public String getNameForType(int type) throws RemoteException;

  /**
   * Liefert eine sprechenden lokalisierte Bezeichnung fuer das Feld.
   * Da dies oft der Name eines Datenbank-Feldes ist, kann es dem Nutzer
   * nicht zugemutet werden und muss daher ueber diese Funktion
   * in einen verstaendlichen Text gewandelt werden.
   * @param field
   * @return liefert eine sprechende Bezeichnung fuer das Feld.
   * @throws RemoteException
   */
  public String getNameForField(String field) throws RemoteException;

  /**
   * Liefert eine Liste der moeglichen Feld-Bezeichnungen.
   * @return Liste der moeglichen Feld-Bezeichnungen.
   * @throws RemoteException
   */
  public String[] getValidFields() throws RemoteException;
  
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
  
  /**
   * Liefert den Namen des Pattern.
   * @return Name
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Speichert den Namen des Patterns.
   * @param name Name.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
}


/**********************************************************************
 * $Log: Pattern.java,v $
 * Revision 1.7  2005/11/18 00:43:29  willuhn
 * @B bug 21
 *
 * Revision 1.6  2005/08/01 16:10:41  web0
 * @N synchronize
 *
 * Revision 1.5  2005/06/28 17:45:41  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.3  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/05/24 23:30:03  web0
 * @N Erster Code fuer OP-Verwaltung
 *
 * Revision 1.1  2005/05/09 23:47:24  web0
 * @N added first code for the filter framework
 *
 **********************************************************************/