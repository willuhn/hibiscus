/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/EmpfaengerImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/17 00:53:22 $
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

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;

/**
 */
public class EmpfaengerImpl extends AbstractDBObject implements Empfaenger {

  /**
   * @throws RemoteException
   */
  public EmpfaengerImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "empfaenger";
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
		throw new ApplicationException("nicht implementiert");
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {

			if (getName() == null || getName().length() == 0)
				throw new ApplicationException("Bitte geben Sie einen Namen ein.");

			if (getBLZ() == null || getBLZ().length() == 0)
				throw new ApplicationException("Bitte geben Sie eine BLZ ein.");

			if (getKontonummer() == null || getKontonummer().length() == 0)
				throw new ApplicationException("Bitte geben Sie eine Kontonummer ein.");

			if (!HBCIUtils.checkAccountCRC(getBLZ(),getKontonummer()))
				throw new ApplicationException("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben.");

		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while checking empfaenger",e);
			throw new ApplicationException("Fehler bei der Prüfung des Empfängers");
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
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Empfaenger#getKontonummer()
   */
  public String getKontonummer() throws RemoteException {
    return (String) getField("kontonummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Empfaenger#getBLZ()
   */
  public String getBLZ() throws RemoteException {
		return (String) getField("blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Empfaenger#getName()
   */
  public String getName() throws RemoteException {
		return (String) getField("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Empfaenger#setKontonummer(java.lang.String)
   */
  public void setKontonummer(String kontonummer) throws RemoteException {
  	setField("kontonummer",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Empfaenger#setBLZ(java.lang.String)
   */
  public void setBLZ(String blz) throws RemoteException {
  	setField("blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Empfaenger#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException {
  	setField("name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Empfaenger#getUeberweisungen()
   */
  public DBIterator getUeberweisungen() throws RemoteException {
		DBIterator list = Settings.getDatabase().createList(Ueberweisung.class);
		list.addFilter("empfaenger_id = " + this.getID());
		return list;
  }

}


/**********************************************************************
 * $Log: EmpfaengerImpl.java,v $
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/