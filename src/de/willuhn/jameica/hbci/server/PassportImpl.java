/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportImpl.java,v $
 * $Revision: 1.5 $
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
import java.util.HashMap;
import java.util.Iterator;

import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportParam;
import de.willuhn.jameica.hbci.rmi.PassportType;
import de.willuhn.util.ApplicationException;

/**
 * Bildet einen generischen Passport (Sicherheitsmedium) in HBCI ab.
 * Konkrete Passports muessen von dieser Klasse ableiten.
 */
public class PassportImpl extends AbstractDBObject implements Passport {

	private HashMap params = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public PassportImpl() throws RemoteException {
    super();
  }

	/**
	 * Liefert den Wert des Parameters mit diesem Namen.
   * @param name Name des Parameters.
   * @return Wert des Parameters.
   * @throws RemoteException
   */
  protected String getParam(String name) throws RemoteException
	{
		if (params == null) loadParams();
		return (String) params.get(name);
	}

  /**
	 * Speichert den Wert eines Parameters.
   * @param name Name des Parameters.
   * @param value Wert.
   * @throws RemoteException
   */
  protected void setParam(String name, String value) throws RemoteException
	{
		if (params == null) loadParams();
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

		try {
			// Wir checken, ob der Passport einem Konto zugewiesen ist
			DBIterator list = Settings.getDatabase().createList(Konto.class);
			list.addFilter("passport_id='" + getID() + "'");
			if (list.hasNext())
			{
				throw new ApplicationException("Das Sicherheitsmedium kann nicht gelöscht werden, da es einem Konto zugewiesen ist.");
			}
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while delete check",e);
			throw new ApplicationException("Fehler beim Löschen",e);
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		updateCheck();
		try {
			DBIterator list = this.getList();
			Passport p = null;
			while (list.hasNext())
			{
				p = (Passport) list.next();
				if (getName().equals(p.getName()))
					throw new ApplicationException("Ein Sicherheitsmedium mit diesem Namen existiert bereits.");
			}
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while insert check",e);
			throw new ApplicationException("Fehler beim Speichern",e);
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		try {
			if (getName() == null || getName().length() == 0)
				throw new ApplicationException("Bitte geben Sie einen Namen ein.");

			if (getPassportType() == null)
			throw new ApplicationException("Bitte wählen Sie ein Sicherheitsmedium aus.");
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while insert check",e);
			throw new ApplicationException("Fehler beim Speichern",e);
		}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException {
    if ("passport_type_id".equals(field))
    	return PassportType.class;
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
		setField("name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#getPassportType()
   */
  public PassportType getPassportType() throws RemoteException
	{
		return (PassportType) getField("passport_type_id");
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Passport#setPassportType(de.willuhn.jameica.hbci.rmi.PassportType)
	 */
	public void setPassportType(PassportType type) throws RemoteException {
		if (type == null)
			return;
		this.setField("passport_type_id",new Integer(type.getID()));
	}

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   * Von DBObject ueberschrieben, damit wir auch gleich die
   * PassportParams mitloeschen koennen.
   */
  public void delete() throws RemoteException, ApplicationException {
		try {
			this.transactionBegin();

			// Erst die Parameter loeschen
			DBIterator list = getParams();
			PassportParam p = null;
			while (list.hasNext())
			{
				p = (PassportParam) list.next();
				p.delete();
			}

			// und jetzt uns selbst
			super.delete();

			this.transactionCommit();
		}
		catch (RemoteException e)
		{
			this.transactionRollback();
			throw e;
		}
		catch (ApplicationException e2)
		{
			this.transactionRollback();
			throw e2;
		}
  }

	/**
	 * Liefert einen Iterator mit Objekten des Typs <code>PassportParam</code>
	 * zu diesem Passport.
   * @return dbIterator mit PassportParams.
   * @throws RemoteException
   */
  private DBIterator getParams() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(PassportParam.class);
		list.addFilter("passport_id = " + this.getID());
		return list;
	}

	/**
	 * Laedt die Parameter aus der Datenbank und speichert sie in einer
	 * lokalen HashMap. Dort werden sie so lange gehalten, bis der
	 * ganze Passport gespeichert wird.
   * @throws RemoteException
   */
  private void loadParams() throws RemoteException
	{
		this.params = new HashMap();
		PassportParam p = null;
		DBIterator list = getParams();
		while (list.hasNext())
		{
			p = (PassportParam) list.next();
			this.params.put(p.getName(),p.getValue());
		}
	}

  /**
   * @see de.willuhn.datasource.rmi.DBObject#store()
   * Wir ueberschreiben diese Funktion, damit wir hier die PassportParams
   * neu schreiben koennen.
   */
  public void store() throws RemoteException, ApplicationException {
		try {
			this.transactionBegin();
			
			// wir muessen uns selbst zuerst speichern, damit die ID da ist
			super.store();

			// wir loeschen nun alle Parameter weg ...
			DBIterator list = getParams();
			PassportParam p = null;
			while (list.hasNext())
			{
				p = (PassportParam) list.next();
				p.delete();
			}

			// ...und jetzt schreiben wir sie neu.
			String name = null;
			String value = null;
			Iterator i = params.keySet().iterator();
			while (i.hasNext())
			{
				name = (String) i.next();
				value = getParam(name);
				p = (PassportParam) Settings.getDatabase().createObject(PassportParam.class,null);
				p.setPassport(this);
				p.setName(name);
				p.setValue(value);
				p.store();
			}
			this.transactionCommit();
		}
		catch (RemoteException e)
		{
			this.transactionRollback();
			throw e;
		}
		catch (ApplicationException e2)
		{
			this.transactionRollback();
			throw e2;
		}
				
  }
  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#close()
   */
  public void close() throws RemoteException {
  	throw new UnsupportedOperationException();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#getKonten()
   */
  public Konto[] getKonten() throws RemoteException {
		throw new UnsupportedOperationException();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#isOpen()
   */
  public boolean isOpen() throws RemoteException {
		throw new UnsupportedOperationException();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#open()
   */
  public HBCIHandler open() throws RemoteException {
		throw new UnsupportedOperationException();
  }

}


/**********************************************************************
 * $Log: PassportImpl.java,v $
 * Revision 1.5  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
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