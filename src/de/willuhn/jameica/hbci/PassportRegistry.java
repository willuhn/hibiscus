/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/PassportRegistry.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/04 23:07:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import java.util.Enumeration;
import java.util.Hashtable;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.util.MultipleClassLoader;

/**
 * Sucht alle verfuegbaren Passports und prueft diese auf Verwendbarkeit.
 */
public class PassportRegistry {

	private static Hashtable passportsByName  = new Hashtable();
	private static Hashtable passportsByClass = new Hashtable();

	/**
   * Initialisiert die Passport-Registry.
   */
  public static void init()
	{

		try {
			Application.getLog().info("searching for available passports");
			MultipleClassLoader.ClassFinder finder = Application.getClassLoader().getClassFinder();
			Class[] found = finder.findImplementors(Passport.class);
			for (int i=0;i<found.length;++i)
			{
				Application.getLog().info("found passport type " + found[i].getName() + ", try to instanciate");
				try {
					Passport p = (Passport) found[i].newInstance();
					passportsByName.put(p.getName(),p);
					passportsByClass.put(found[i].getName(),p);
					Application.getLog().info("[" + p.getName() + "] instanciated successfully");
				}
				catch (Exception e)
				{
					Application.getLog().error("failed, skipping passport",e);
				}
			}
		}
		catch (Throwable t)
		{
			Application.getLog().error("error while searching for passports",t);
		}
	}
	
	/**
	 * Liefert eine Instanz des angegebenen Passports.
	 * @param name Sprechender Name des Passports.
	 * @return Instanz des Passports.
	 * @throws Exception
	 */
	public static Passport findByName(String name) throws Exception
	{
		if (name == null)
			return null;
		return (Passport) passportsByName.get(name);
	}

	/**
	 * Liefert eine Instanz des angegebenen Passports.
	 * @param className Java-Klasse des Passports.
	 * @return Instanz des Passports.
	 * @throws Exception
	 */
	public static Passport findByClass(String classname) throws Exception
	{
		if (classname == null)
			return null;
		Passport p = (Passport) passportsByClass.get(classname);
		return p;
	}

	/**
	 * Liefert ein Array mit allen verfuegbaren Passports.
   * @return Liste der Passports.
   */
  public static Passport[] getPassports()
	{
		Enumeration e = passportsByName.elements();
		Passport[] passports = new Passport[passportsByName.size()];
		int i=0;
		while (e.hasMoreElements())
		{
			passports[i++] = (Passport) e.nextElement();
		}
		return passports;
	}
}


/**********************************************************************
 * $Log: PassportRegistry.java,v $
 * Revision 1.1  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 **********************************************************************/