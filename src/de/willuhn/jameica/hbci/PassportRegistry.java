/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/PassportRegistry.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/06/30 20:58:29 $
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
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.util.ClassFinder;
import de.willuhn.util.Logger;

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
			Logger.info("searching for available passports");
			ClassFinder finder = Application.getClassLoader().getClassFinder();
			Class[] found = finder.findImplementors(Passport.class);
			for (int i=0;i<found.length;++i)
			{
				Logger.info("found passport type " + found[i].getName() + ", try to instanciate");
				try {
					Passport p = (Passport) found[i].newInstance();
					passportsByName.put(p.getName(),p);
					passportsByClass.put(found[i].getName(),p);
					Logger.info("[" + p.getName() + "] instanciated successfully");
				}
				catch (Exception e)
				{
					Logger.error("failed, skipping passport",e);
				}
			}
		}
		catch (ClassNotFoundException cn)
		{
			Logger.warn("no passports found");
		}
		catch (Throwable t)
		{
			Logger.error("error while searching for passports",t);
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
	 * @param classname Java-Klasse des Passports.
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
 * Revision 1.5  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/05/11 21:11:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/04 23:30:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 **********************************************************************/