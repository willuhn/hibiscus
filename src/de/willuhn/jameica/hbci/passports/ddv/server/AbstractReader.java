/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.server;

import java.io.File;
import java.io.IOException;

import org.kapott.hbci.manager.HBCIVersion;

import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;
import de.willuhn.logging.Logger;

/**
 * Basis-Implementierung der Chipkartenleser.
 */
public abstract class AbstractReader implements Reader
{
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

      case Platform.OS_WINDOWS_64:
        try {

          f = new File("C:/Windows/SysWOW64");
          if (f.isDirectory() && f.exists()) return f;

          f = new File("C:/Windows/System32");
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
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getPort()
   */
  public String getPort()
  {
    return "COM2/USB2";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTNumber()
   */
  public int getCTNumber()
  {
    return 0;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#useSoftPin()
   */
  public boolean useSoftPin()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getType()
   */
  public Type getType()
  {
    return Type.DDV_CTAPI;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getDefaultHBCIVersion()
   */
  public String getDefaultHBCIVersion()
  {
    return HBCIVersion.HBCI_300.getId();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return this.getName();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (!(obj instanceof Reader))
      return false;
    
    Reader other = (Reader) obj;
    String s1 = this.getClass().getName()  + this.getName()  + this.getCTAPIDriver();
    String s2 = other.getClass().getName() + other.getName() + other.getCTAPIDriver();
    return s1.equals(s2);
  }
}
