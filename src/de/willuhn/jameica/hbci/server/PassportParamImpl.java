/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportParamImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/27 01:10:18 $
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
import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportParam;
import de.willuhn.util.ApplicationException;

/**
 * Bildet einen Passport-Initialisierungsparameter ab.
 */
public class PassportParamImpl
  extends AbstractDBObject
  implements PassportParam {

  /**
   * ct.
   * @throws RemoteException
   */
  public PassportParamImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "passport_param";
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
  	// koennen ohne Probleme geloescht werden.
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {
			if (getName() == null || "".equals(getName()))
				throw new ApplicationException("Bitte geben Sie einen Namen für den Parameter ein.");

			if (getPassport() == null)
				throw new ApplicationException("Bitte wählen Sie einen Passport aus.");
			
			if (getPassport().isNewObject())
				throw new ApplicationException("Bitte speichern Sie zunächst den Passport.");
		}
		catch(RemoteException e)
		{
			Application.getLog().error("error while insertcheck",e);
			throw new ApplicationException("Fehler beim Prüfen des Parameters.");
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
  	if ("passport_id".equals(field))
  		return Passport.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportParam#getName()
   */
  public String getName() throws RemoteException {
    return (String) getField("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportParam#getValue()
   */
  public String getValue() throws RemoteException {
		return (String) getField("value");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportParam#getPassport()
   */
  public Passport getPassport() throws RemoteException {
    return (Passport) getField("passport_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportParam#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException {
		setField("name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportParam#setValue(java.lang.String)
   */
  public void setValue(String value) throws RemoteException {
		setField("value",value);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportParam#setPassport(de.willuhn.jameica.hbci.rmi.Passport)
   */
  public void setPassport(Passport passport) throws RemoteException {
		if (passport == null)
			return;
		setField("passport_id",new Integer(passport.getID()));
  }

}


/**********************************************************************
 * $Log: PassportParamImpl.java,v $
 * Revision 1.2  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/