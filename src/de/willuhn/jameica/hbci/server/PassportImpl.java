/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/02/12 23:46:46 $
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
import java.util.HashMap;
import java.util.Iterator;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportParam;
import de.willuhn.util.ApplicationException;

/**
 * Bildet einen Passport (Sicherheitsmedium) in HBCI ab.
 */
public class PassportImpl extends AbstractDBObject implements Passport {

	private Konto konto = null;

	private HashMap params = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public PassportImpl() throws RemoteException {
    super();
  }

	/**
	 * Speichert das Konto, zu dem der Passport gehoert.
   * @param k das Konto.
   */
  protected void setKonto(Konto k) throws RemoteException
	{
		this.konto = k;
		
		if (k == null)
			return;
		
		// So, das Konto ist bekannt, nun koennen wir die Parameter laden.
		params = new HashMap();
		DBIterator list = getParams();
		PassportParam p = null;
		while (list.hasNext())
		{
			p = (PassportParam) list.next();
			params.put(p.getName(),p.getValue());
		}
	}

	/**
	 * Liefert den Wert des Parameters mit diesem Namen.
   * @param name Name des Parameters.
   * @return Wert des Parameters.
   */
  protected String getParam(String name)
	{
		if (params == null)
			return null;
		return (String) params.get(name);
	}

	/**
	 * Speichert den Wert eines Parameters.
   * @param name Name des Parameters.
   * @param value Wert.
   */
  protected void setParam(String name, String value)
	{
		if (params == null)
			params = new HashMap();
		
		params.put(name,value);
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
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
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

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   * Wir loeschen hier natuerlich nicht den Passport selbst sondern
   * die Einstellungen, die der Passport fuer dieses Konto hat.
   */
  public void delete() throws RemoteException, ApplicationException {
  	DBIterator list = getParams();
  	PassportParam p = null;
  	while (list.hasNext())
  	{
  		p = (PassportParam) list.next();
  		p.delete();
  	}
  }

	/**
	 * Liefert einen Iterator mit Objekten des Typs <code>PassportParam</code>
	 * zu diesem Passport.
   * @return dbIterator mit PassportParams.
   * @throws RemoteException
   */
  protected DBIterator getParams() throws RemoteException
	{
		if (konto == null)
			return null;

		DBIterator list = Settings.getDatabase().createList(PassportParam.class);
		list.addFilter("konto_id = " + konto.getID());
		return list;
	}

  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   * Wir ueberschreiben diese Funktion, damit wir hier die PassportParams
   * neu schreiben koennen.
   */
  public void store() throws RemoteException, ApplicationException {
		// wir loeschen erstmal alle Params
		delete();
		
		// und jetzt schreiben wir sie neu.
		String name = null;
		String value = null;
		PassportParam p = null;
		Iterator i = params.keySet().iterator();
		while (i.hasNext())
		{
			name = (String) i.next();
			value = getParam(name);
			p = (PassportParam) Settings.getDatabase().createObject(PassportParam.class,null);
			p.setKonto(konto);
			p.setName(name);
			p.setValue(value);
			p.store();
		}
		
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#open()
   */
  public void open() throws RemoteException {
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#close()
   */
  public void close() throws RemoteException {
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#isOpen()
   */
  public boolean isOpen() throws RemoteException {
    return false;
  }

}


/**********************************************************************
 * $Log: PassportImpl.java,v $
 * Revision 1.3  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 00:38:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/