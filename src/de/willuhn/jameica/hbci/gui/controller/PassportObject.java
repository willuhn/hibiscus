/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/PassportObject.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/06/18 19:47:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.GenericObject;
import de.willuhn.jameica.hbci.passport.Passport;

/**
 * Hilfklasse, die einen Passport in ein GenericObject wrappt.
 * Existiert lediglich, damit die Eigenschaften eines Passports
 * bequem mit den GUI-Bordmitteln von Jameica angezeigt werden koennen.
 */
public class PassportObject implements GenericObject
{

	private Passport passport;

	/**
	 * ct.
	 * @param p
	 */
	public PassportObject(Passport p)
	{
		this.passport = p;
	}

	/**
	 * Liefert den Passport.
   * @return
   */
  public Passport getPassport()
	{
		return passport;
	}

	/**
	 * Liefert das genannte Attribut.
	 * Ist Attribut="name", wird der sprechende Name geliefert, sonst
	 * der Passport selbst.
	 * @see de.willuhn.datasource.rmi.GenericObject#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) throws RemoteException
	{
		if ("name".equalsIgnoreCase(name))
			return passport.getName();
		return passport;
	}

	/**
	 * @see de.willuhn.datasource.rmi.GenericObject#getID()
	 */
	public String getID() throws RemoteException
	{
		return passport.getClass().getName();
	}

	/**
	 * @see de.willuhn.datasource.rmi.GenericObject#getPrimaryAttribute()
	 */
	public String getPrimaryAttribute() throws RemoteException
	{
		return "name";
	}

	/**
	 * @see de.willuhn.datasource.rmi.GenericObject#equals(de.willuhn.datasource.rmi.GenericObject)
	 */
	public boolean equals(GenericObject other) throws RemoteException
	{
		if (other == null)
			return false;
		return other.getID().equals(other.getID());
	}

}


/**********************************************************************
 * $Log: PassportObject.java,v $
 * Revision 1.1  2004/06/18 19:47:31  willuhn
 * *** empty log message ***
 *
 **********************************************************************/