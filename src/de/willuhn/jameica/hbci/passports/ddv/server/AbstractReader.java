/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/AbstractReader.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/09/06 11:54:25 $
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


/**********************************************************************
 * $Log: AbstractReader.java,v $
 * Revision 1.9  2011/09/06 11:54:25  willuhn
 * @C JavaReader in PCSCReader umbenannt - die PIN-Eingabe fehlt noch
 *
 * Revision 1.8  2011-09-01 12:16:08  willuhn
 * @N Kartenleser-Suche kann jetzt abgebrochen werden
 * @N Erster Code fuer javax.smartcardio basierend auf dem OCF-Code aus HBCI4Java 2.5.8
 *
 * Revision 1.7  2011-09-01 09:40:53  willuhn
 * @R Biometrie-Support bei Kartenlesern entfernt - wurde nie benutzt
 *
 * Revision 1.6  2011-05-03 16:43:50  willuhn
 * @B da fehlte eine equals()-Funktion
 *
 * Revision 1.5  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.4  2010-07-25 23:54:34  willuhn
 * @N Suchpfad fuer Windows 64Bit-CTAPI-Treiber (siehe Mail von Tobias vom 26.07.2010)
 *
 * Revision 1.3  2010-07-22 22:36:24  willuhn
 * @N Code-Cleanup
 *
 * Revision 1.2  2010-07-22 21:20:37  willuhn
 * @N FreeBSD64-Support - siehe Mak's Mail vom 22.07.2010
 *
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/