/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportTypeImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/04/27 22:23:56 $
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
import de.willuhn.jameica.hbci.rmi.PassportType;
import de.willuhn.util.ApplicationException;

/**
 * Implementiert die Liste der unterstuetzten Passports.
 */
public class PassportTypeImpl
  extends AbstractDBObject
  implements PassportType {

  /**
   * @throws RemoteException
   */
  public PassportTypeImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "passport_type";
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
		throw new ApplicationException("Löschen nicht erlaubt");
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		throw new ApplicationException("Speichern nicht erlaubt");
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		throw new ApplicationException("Speichern nicht erlaubt");
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportType#getName()
   */
  public String getName() throws RemoteException {
    return (String) getField("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportType#getImplementor()
   */
  public String getImplementor() throws RemoteException {
		return (String) getField("implementor");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportType#getAbstractView()
   */
  public String getAbstractView() throws RemoteException {
		return (String) getField("abstractview");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportType#getController()
   */
  public String getController() throws RemoteException {
		return (String) getField("controller");
  }

}


/**********************************************************************
 * $Log: PassportTypeImpl.java,v $
 * Revision 1.2  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.1  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 **********************************************************************/