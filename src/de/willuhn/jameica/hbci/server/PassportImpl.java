/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/11 00:11:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.util.ApplicationException;

/**
 * Bildet einen Passport (Sicherheitsmedium) in HBCI ab.
 */
public class PassportImpl extends AbstractDBObject implements Passport {

  /**
   * ct.
   * @throws RemoteException
   */
  public PassportImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "passport";
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getPrimaryField()
   */
  public String getPrimaryField() throws RemoteException {
    return "name";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
		throw new ApplicationException("Passports dürfen nicht gelöscht werden.");
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		throw new ApplicationException("Neue Passports dürfen nicht angelegt werden.");
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		throw new ApplicationException("Passports dürfen nicht geändert werden.");
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#getName()
   */
  public String getName() throws RemoteException {
    return (String) getField("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException {
		throw new RemoteException("Passport darf nicht umbenannt werden.");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#getType()
   */
  public int getType() throws RemoteException
	{
		return new Integer(getID()).intValue();
	}
}


/**********************************************************************
 * $Log: PassportImpl.java,v $
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/