/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.rmi.DBProperty;

/**
 * Speichert ein einzelnes Property in der Datenbank.
 */
public class DBPropertyImpl extends AbstractDBObject implements DBProperty
{

  /**
   * ct
   * @throws RemoteException
   */
  public DBPropertyImpl() throws RemoteException
  {
    super();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name";
  }

  @Override
  protected String getTableName()
  {
    return "property";
  }

  @Override
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  @Override
  public String getValue() throws RemoteException
  {
    return (String) getAttribute("content");
  }

  @Override
  public void setName(String name) throws RemoteException
  {
    setAttribute("name",name);
  }

  @Override
  public void setValue(String value) throws RemoteException
  {
    setAttribute("content",value);
  }

}


/*********************************************************************
 * $Log: DBPropertyImpl.java,v $
 * Revision 1.1  2008/05/30 14:23:48  willuhn
 * @N Vollautomatisches und versioniertes Speichern der BPD und UPD in der neuen Property-Tabelle
 *
 **********************************************************************/