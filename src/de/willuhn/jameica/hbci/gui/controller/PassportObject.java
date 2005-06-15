/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/PassportObject.java,v $
 * $Revision: 1.8 $
 * $Date: 2005/06/15 16:10:48 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.PassportRegistry;
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
   * ct.
   * @param passportClass
   * @throws Exception
   */
  public PassportObject(String passportClass) throws Exception
  {
    this.passport = PassportRegistry.findByClass(passportClass);
  }

	/**
	 * Liefert den Passport.
	 * @return Passport.
   */
  public Passport getPassport()
	{
		return passport;
	}

	/**
	 * Liefert das genannte Attribut.
	 * Ist Attribut="name", wird der sprechende Name geliefert, sonst
	 * der Passport selbst.
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) throws RemoteException
	{
		if ("name".equalsIgnoreCase(name))
			return passport.getName();
		return passport;
	}

	/**
	 * @see de.willuhn.datasource.GenericObject#getID()
	 */
	public String getID() throws RemoteException
	{
		return passport.getClass().getName();
	}

	/**
	 * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
	 */
	public String getPrimaryAttribute() throws RemoteException
	{
		return "name";
	}

	/**
	 * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
	 */
	public boolean equals(GenericObject other) throws RemoteException
	{
		if (other == null)
			return false;
		return getID().equals(other.getID());
	}

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[]{"name"};
  }

}


/**********************************************************************
 * $Log: PassportObject.java,v $
 * Revision 1.8  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.7  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.6  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.5  2005/02/01 18:27:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2005/01/05 15:17:50  willuhn
 * @N Neues Service-System in Jameica
 *
 * Revision 1.3  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.2  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.1  2004/06/18 19:47:31  willuhn
 * *** empty log message ***
 *
 **********************************************************************/