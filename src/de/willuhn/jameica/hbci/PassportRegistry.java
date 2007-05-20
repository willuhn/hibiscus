/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/PassportRegistry.java,v $
 * $Revision: 1.14 $
 * $Date: 2007/05/20 23:45:10 $
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

import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Sucht alle verfuegbaren Passports und prueft diese auf Verwendbarkeit.
 */
public class PassportRegistry {

	private static Hashtable passportsByName  = null;
	private static Hashtable passportsByClass = null;

	/**
   * Initialisiert die Passport-Registry.
   */
  public static synchronized void init()
	{
    if (passportsByClass != null || passportsByName != null)
      return;
    
    passportsByClass = new Hashtable();
    passportsByName  = new Hashtable();

    try {
			Logger.info("searching for available passports");
			ClassFinder finder = Application.getClassLoader().getClassFinder();
			Class[] found = finder.findImplementors(Passport.class);
			for (int i=0;i<found.length;++i)
			{
				Logger.info("found passport type " + found[i].getName() + ", try to instantiate");
				try {
					Passport p = (Passport) found[i].newInstance();
					Application.getCallback().getStartupMonitor().setStatusText("init passport " + p.getName());
 				  passportsByName.put(p.getName(),found[i]);
					passportsByClass.put(found[i].getName(),found[i]);
					Logger.info("[" + p.getName() + "] instantiated successfully");
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
	
  private static Passport load(Class c) throws Exception
  {
    if (c == null)
      return null;
    Passport p = (Passport) c.newInstance();
    Logger.debug("[" + c.getName() + "][" + p.getName() + "] instantiated successfully");
    return p;
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
    init();
		return (Passport) load((Class) passportsByName.get(name));
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
    init();
		return (Passport) load((Class) passportsByClass.get(classname));
	}

	/**
	 * Liefert ein Array mit allen verfuegbaren Passports.
   * @return Liste der Passports.
   * @throws Exception
   */
  public static Passport[] getPassports() throws Exception
	{
    init();
		Enumeration e = passportsByName.elements();
		Passport[] passports = new Passport[passportsByName.size()];
		int i=0;
		while (e.hasMoreElements())
		{
			passports[i++] = (Passport) load((Class)e.nextElement());
		}
		return passports;
	}
}


/**********************************************************************
 * $Log: PassportRegistry.java,v $
 * Revision 1.14  2007/05/20 23:45:10  willuhn
 * @N HBCI-Jobausfuehrung Servertauglich gemacht
 *
 * Revision 1.13  2005/12/05 10:35:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2005/07/04 21:57:08  web0
 * @B bug 80
 *
 * Revision 1.11  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.10  2005/01/30 20:45:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2005/01/09 18:48:40  willuhn
 * @N native lib for sizrdh
 *
 * Revision 1.8  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
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