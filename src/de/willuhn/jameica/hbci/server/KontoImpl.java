/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/KontoImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/02/11 15:40:42 $
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
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportParam;
import de.willuhn.util.ApplicationException;

/**
 * Bildet eine Bankverbindung ab.
 */
public class KontoImpl extends AbstractDBObject implements Konto {

  /**
   * ct.
   * @throws RemoteException
   */
  public KontoImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "konto";
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#getPrimaryField()
   */
  public String getPrimaryField() throws RemoteException {
    return "kontonummer";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException {
    // TODO Pruefen, ob Buchungen o.ae. vorliegen
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {
			if (getName() == null || "".equals(getName()))
				throw new ApplicationException("Bitten geben Sie den Namen des Kontoinhabers ein.");

			if (getKontonummer() == null || "".equals(getKontonummer()))
				throw new ApplicationException("Bitte geben Sie eine Kontonummer ein.");

			if (getBLZ() == null || "".equals(getBLZ()))
				throw new ApplicationException("Bitte geben Sie eine Bankleitzahl ein.");
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while insertcheck",e);
			throw new ApplicationException("Fehler bei der Prüfung der Daten");
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
   * @see de.willuhn.jameica.hbci.rmi.Konto#getKontonummer()
   */
  public String getKontonummer() throws RemoteException {
    return (String) getField("kontonummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getBLZ()
   */
  public String getBLZ() throws RemoteException {
		return (String) getField("blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getName()
   */
  public String getName() throws RemoteException {
		return (String) getField("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getPassport()
   */
  public Passport getPassport() throws RemoteException {
		return (Passport) getField("passport_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setKontonummer(java.lang.String)
   */
  public void setKontonummer(String kontonummer) throws RemoteException {
		setField("kontonummer",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setBLZ(java.lang.String)
   */
  public void setBLZ(String blz) throws RemoteException {
  	setField("blz",blz);
  }

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Konto#setName(java.lang.String)
	 */
	public void setName(String name) throws RemoteException {
		setField("name",name);
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setPassport(de.willuhn.jameica.hbci.rmi.Passport)
   */
  public void setPassport(Passport passport) throws RemoteException {
		if (passport == null)
			return;
  	setField("passport_id",new Integer(passport.getID()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getPassportParams()
   */
  public DBIterator getPassportParams() throws RemoteException {
  	DBIterator list = Settings.getDatabase().createList(PassportParam.class);
  	list.addFilter("konto_id = " + this.getID());
  	return list;
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    // Wir muessen die PassportParameter mit loeschen
    try {
      transactionBegin();
      super.delete();
      DBIterator list = getPassportParams();
      PassportParam p = null;
      while (list.hasNext())
      {
        p = (PassportParam) list.next();
        p.delete();
      }
      transactionCommit();
    }
    catch (RemoteException e)
    {
      transactionRollback();
      throw e;
    }
    catch (ApplicationException e2)
    {
      transactionRollback();
      throw e2;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#getWaehrung()
   */
  public String getWaehrung() throws RemoteException
  {
    return (String) getField("waehrung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Konto#setWaehrung(java.lang.String)
   */
  public void setWaehrung(String waehrung) throws RemoteException
  {
    setField("waehrung",waehrung);
  }

}


/**********************************************************************
 * $Log: KontoImpl.java,v $
 * Revision 1.3  2004/02/11 15:40:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 10:33:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/