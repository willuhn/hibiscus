/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportParamImpl.java,v $
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
import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.rmi.Konto;
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
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {
			if (getName() == null || "".equals(getName()))
				throw new ApplicationException("Bitte geben Sie einen Namen für den Parameter ein.");

			if (getKonto() == null)
				throw new ApplicationException("Bitte wählen Sie ein Konto aus.");
			
			if (getKonto().isNewObject())
				throw new ApplicationException("Bitte speichern Sie zunächst das Konto.");
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
  	if ("konto_id".equals(field))
  		return Konto.class;
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
   * @see de.willuhn.jameica.hbci.rmi.PassportParam#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    return (Konto) getField("konto_id");
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
   * @see de.willuhn.jameica.hbci.rmi.PassportParam#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException {
		if (konto == null)
			return;
		setField("konto_id",new Integer(konto.getID()));
  }

}


/**********************************************************************
 * $Log: PassportParamImpl.java,v $
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/