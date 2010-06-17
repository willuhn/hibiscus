/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/AbstractReader.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:45:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.server;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;
import de.willuhn.logging.Logger;

/**
 * Basis-Implementierung der Chipkartenleser.
 */
public abstract class AbstractReader extends UnicastRemoteObject implements Reader
{
  /**
   * @throws RemoteException
   */
  protected AbstractReader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) throws RemoteException
  {
  	if (name == null)
  		return null;
    if ("name".equals(name))
    	return getName();
    if ("ctapidriver".equals(name))
    	return getCTAPIDriver();
		return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return getClass().getName();
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
	 * Liefert abhaengig vom Betriebssystem das Verzeichnis, in dem
	 * vermutlich der CTAPI-Treiber liegen wird.
   * @return vermutlicher Pfad zum CTAPI-Treiber.
   */
  File getCTAPIDriverPath()
	{

		File f = new File(Settings.getLibPath());
    
    switch(Application.getPlatform().getOS())
    {

      case Platform.OS_LINUX:
        try
        {
          return f.getCanonicalFile();
        }
        catch (IOException e)
        {
          Logger.error("error while converting ctapi path into canonical path",e);
        }
        return f;
      

      case Platform.OS_WINDOWS:
      case Platform.OS_WINDOWS_64:
          // OK, sehr wahrscheinlich Windows. Dann schauen wir mal, ob
          // wir das System32-Verzeichnis finden
          try {

            f = new File("C:/Windows/System32");
            if (f.isDirectory() && f.exists()) return f;

            f = new File("C:/WinNT/System32");
            if (f.isDirectory() && f.exists()) return f;

            f = new File("C:/Win2000/System32");
            if (f.isDirectory() && f.exists()) return f;

            f = new File("C:/WinXP/System32");
            if (f.isDirectory() && f.exists()) return f;

            f = new File("C:/Win/System32");
            if (f.isDirectory() && f.exists()) return f;

          }
          catch (Throwable t)
          {
            // muessen wir nicht loggen
          }
          return f;
          
        case Platform.OS_MAC:
          return f;

        default:
          return f;
    }
		
		
	}
  
  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"name","ctapidriver"};
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getJNILib()
   */
  public String getJNILib() throws RemoteException
  {
    return getDefaultJNILib();
  }

  /**
   * Liefert die per Default zu verwendende JNI-Lib.
   * @return per Default zu verwendende JNI-Lib.
   */
  public static String getDefaultJNILib()
  {
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_LINUX:
        return Settings.getLibPath() + "/libhbci4java-card-linux-32.so";
      
      case Platform.OS_LINUX_64:
        return Settings.getLibPath() + "/libhbci4java-card-linux-64.so";

      case Platform.OS_WINDOWS:
        return Settings.getLibPath() + "/hbci4java-card-win32.dll";
      
      case Platform.OS_WINDOWS_64:
        return Settings.getLibPath() + "/hbci4java-card-win32_x86-64.dll";

      case Platform.OS_MAC:
        return Settings.getLibPath() + "/libhbci4java-card-mac.jnilib";
      
      default:
        Logger.warn("unable to detect operating system. fallback to windows");
        return Settings.getLibPath() + "/hbci4java-card-win32.dll";
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getPort()
   */
  public String getPort() throws RemoteException
  {
    return "COM2/USB2";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#useBIO()
   */
  public boolean useBIO() throws RemoteException
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTNumber()
   */
  public int getCTNumber() throws RemoteException
  {
    return 0;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#useSoftPin()
   */
  public boolean useSoftPin() throws RemoteException
  {
    return false;
  }
}


/**********************************************************************
 * $Log: AbstractReader.java,v $
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.13  2008/12/01 00:47:51  willuhn
 * @N Update auf HBCI4Java 2.5.10
 *
 * Revision 1.12  2008/11/17 23:22:38  willuhn
 * @C Code zur Ermittlung des OS in Jameica verschoben
 *
 * Revision 1.11  2008/09/29 23:14:27  willuhn
 * @N 64Bit-JNI-Lib fuer Windows
 *
 * Revision 1.10  2008/09/15 22:01:04  willuhn
 * @N Presets aktualisiert
 *
 * Revision 1.9  2006/08/03 22:13:49  willuhn
 * @N OmniKey 4000 Preset
 *
 * Revision 1.8  2006/04/05 15:15:42  willuhn
 * @N Alternativer Treiber fuer Towitoko Kartenzwerg
 *
 * Revision 1.7  2006/01/08 22:22:37  willuhn
 * @B 176
 * @B 177
 *
 * Revision 1.6  2005/08/08 15:07:36  willuhn
 * @N added jnilib for mac os
 * @N os autodetection for mac os
 *
 * Revision 1.5  2005/01/05 15:00:33  willuhn
 * @N getAttributeNames
 *
 * Revision 1.4  2004/11/12 18:25:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/17 12:52:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/27 23:51:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 **********************************************************************/