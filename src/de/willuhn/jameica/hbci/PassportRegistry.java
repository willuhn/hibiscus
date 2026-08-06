/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Sucht alle verfuegbaren Passports und prueft diese auf Verwendbarkeit.
 */
public class PassportRegistry {

	private static Map<String,Class<Passport>> passportsByName = null;
	private static Map<String,Class<Passport>> passportsByClass = null;

	/**
   * Initialisiert die Passport-Registry.
   */
  public static synchronized void init()
	{
    if (passportsByClass != null || passportsByName != null)
      return;
    
    passportsByClass = new HashMap<>();
    passportsByName  = new HashMap<>();

    try {
			Logger.info("searching for available passports");
	    final BeanService service = Application.getBootLoader().getBootable(BeanService.class);
	    final ClassFinder finder = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader().getClassFinder();
	    final Class[] found = finder.findImplementors(Passport.class);
			for (Class c:found)
			{
				try {
				  final Passport p = (Passport) service.get(c);
					Application.getCallback().getStartupMonitor().setStatusText("init passport " + p.getName());
 				  passportsByName.put(p.getName(),c);
					passportsByClass.put(c.getName(),c);
					Logger.info("  " + p.getName() + " [" + p.getClass().getName() + "]");
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
   * Erstellt eine Instanz die Passport-Klasse.
   * @param c die Passport-Klasse.
   * @return die Instanz.
   * @throws Exception
   */
  private static Passport load(Class<Passport> c) throws Exception
  {
    if (c == null)
      return null;
    
    final BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    final Passport p = service.get(c);
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
    
		return load(passportsByName.get(name));
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
		return load(passportsByClass.get(classname));
	}

	/**
	 * Liefert ein Array mit allen verfuegbaren Passports.
   * @return Liste der Passports.
   * @throws Exception
   */
  public static List<Passport> getPassports() throws Exception
	{
    init();
		final List<Passport> result = new ArrayList<>();
		for (Class<Passport> c:passportsByClass.values())
		{
			result.add(load(c));
		}
		return result;
	}
}
