/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/Settings.java,v $
 * $Revision: 1.19 $
 * $Date: 2004/07/21 23:54:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import sun.misc.BASE64Encoder;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.util.Logger;

/**
 * Verwaltet die Einstellungen des Plugins.
 * @author willuhn
 */
public class Settings
{

  private static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(HBCI.class);
  private static DBService db = null;
	private static String workPath = null;
	private static String libPath = null;

	private static Color buchungSollForeground = null;
	private static Color buchungHabenForeground = null;
	private static Color ueberfaelligForeground = null;

  /**
   * Liefert den Datenbank-Service.
   * @return Datenbank.
   * @throws RemoteException
   */
  public static DBService getDatabase() throws RemoteException
  {
    if (db != null)
      return db;
		try {
			db = PluginLoader.getPlugin(HBCI.class).getResources().getDatabase().getDBService();
			return db;
		}
		catch (Exception e)
		{
			throw new RemoteException("unable to open/create database",e);
		}
  }

	/**
	 * Liefert die Vordergrundfarbe fuer Soll-Buchungen in Tabellen.
   * @return Farbe.
   */
  public static Color getBuchungSollForeground()
	{
		if (buchungSollForeground != null)
			return buchungSollForeground;

		buchungSollForeground = new Color(GUI.getDisplay(),settings.getRGB("buchung.soll.fg",new RGB(147,33,33)));
		return buchungSollForeground;
	}

	/**
	 * Liefert die Vordergrundfarbe fuer Haben-Buchungen in Tabellen.
	 * @return Farbe.
	 */
	public static Color getBuchungHabenForeground()
	{
		if (buchungHabenForeground != null)
			return buchungHabenForeground;

		buchungHabenForeground = new Color(GUI.getDisplay(),settings.getRGB("buchung.haben.fg",new RGB(4,13,169)));
		return buchungHabenForeground;
	}

	/**
	 * Liefert die Vordergrundfarbe fuer ueberfaellige Ueberweisungen.
	 * @return Farbe.
	 */
	public static Color getUeberfaelligForeground()
	{
		if (ueberfaelligForeground != null)
			return ueberfaelligForeground;

		ueberfaelligForeground = new Color(GUI.getDisplay(),settings.getRGB("ueberfaellig.fg",new RGB(140,0,0)));
		return ueberfaelligForeground;
	}

	/**
	 * Speichert die Farben fuer den Vordergrund von Soll-Buchungen.
   * @param rgb
   */
  public static void setBuchungSollForeground(RGB rgb)
	{
		settings.setAttribute("buchung.soll.fg",rgb);
		buchungSollForeground = null;
	}

	/**
	 * Speichert die Farben fuer den Vordergrund von Haben-Buchungen.
	 * @param rgb
	 */
	public static void setBuchungHabenForeground(RGB rgb)
	{
		settings.setAttribute("buchung.haben.fg",rgb);
		buchungHabenForeground = null;
	}

	/**
	 * Speichert die Farben fuer den Vordergrund von ueberfaelligen Ueberweisungen.
	 * @param rgb
	 */
	public static void setUeberfaelligForeground(RGB rgb)
	{
		settings.setAttribute("ueberfaellig.fg",rgb);
		ueberfaelligForeground = null;
	}

	/**
	 * Liefert den Verzeichnis-Pfad zu den nativen Libs.
	 * @return Pfad der Libs.
	 */
	public static String getLibPath()
	{
		if (libPath != null)
			return libPath;
		libPath = PluginLoader.getPlugin(HBCI.class).getResources().getPath() + "/lib";
		return libPath;
	}

	/**
	 * Liefert den Pfad zum Work-Verzeichnis.
	 * @return Pfad des Work-Verzeichnis.
	 */
	public static String getWorkPath()
	{
		if (workPath != null)
			return workPath;
		workPath = PluginLoader.getPlugin(HBCI.class).getResources().getWorkPath();
		return workPath;
	}

  /**
   * Legt fest, ob die PIN gehasht gespeichert werden soll, um sie
   * bei erneuter Eingabe pruefen zu koennen.
   * @param checkPin true, wenn die Pin geprueft werden soll.
   */
  public static void setCheckPin(boolean checkPin)
  {
    settings.setAttribute("checkpin", checkPin ? "true" : "false");
  }

  /**
   * Prueft, ob ein Hash der PIN gespeichert werden soll, um sie bei
   * erneuter Eingabe auf Richtigkeit pruefen zu koennen.
   * @return true, wenn die Pin gehasht gespeichert werden soll.
   */
  public static boolean getCheckPin()
  {
    return settings.getBoolean("checkpin",true);
  }

  /**
   * Liefert die Check-Summe der PIN oder <code>null</code> wenn sie nie
   * gespeichert wurde.
   * @return Check-Summe der Pin.
   */
  public static String getCheckSum()
  {
    return settings.getString("checksum",null);
  }

  /**
   * Speichert die Check-Summe der PIN.
   * @param checksum Check-Summe der Pin.
   */
  public static void setCheckSum(String checksum)
  {
    settings.setAttribute("checksum",checksum);
  }

  /**
   * Speichert, ob wir eine permanente Online-Verbindung haben und daher
   * vom HBCI-Kernel nicht dauernd gefragt werden muessen, ob wir eine
   * Internetverbindung haben wollen.
   * @param online true, wenn wir dauernd online sind.
   */
  public static void setOnlineMode(boolean online)
  {
    settings.setAttribute("online", online ? "true" : "false");
  }

  /**
   * Prueft, ob wir eine permanente Online-Verbindung haben und daher
   * vom HBCI-Kernel nicht dauernd gefragt werden muessen, ob wir eine
   * Internetverbindung haben wollen.
   * @return true, wenn wir dauernd online sind.
   */
  public static boolean getOnlineMode()
  {
    return settings.getBoolean("online",false);
  }

	/**
	 * Liefert das Limit bei Ueberweisungen.
	 * Soll den Benutzer davor schuetzen, versehentlich zu grosse Betraege bei
	 * einer Ueberweisung einzugeben.
   * @return Ueberweisungslimit.
   */
  public static double getUeberweisungLimit()
	{
		return settings.getDouble("ueberweisunglimit",1000.0);
	}
	
	/**
	 * Definiert ein Limit bei Ueberweisungen.
	 * Soll den Benutzer davor schuetzen, versehentlich zu grosse Betraege bei
	 * einer Ueberweisung einzugeben.
   * @param limit das Limit fuer Ueberweisungen.
   */
  public static void setUeberweisungLimit(double limit)
	{
		settings.setAttribute("ueberweisunglimit",limit);
	}
	
  /**
   * Liefert das Passwort mit die lokalen Daten verschluesselt werden.
   * @return Passphrase.
   */
  protected static String getPassphrase()
  {
    MessageDigest md = null;
    byte[] hashed = null;
    try {
      md = MessageDigest.getInstance("SHA1");
      hashed = md.digest(getWorkPath().getBytes());
    }
    catch (NoSuchAlgorithmException nsae)
    {
      Logger.warn("algorithm SHA1 not found, trying MD5");
      try {
        md = MessageDigest.getInstance("MD5");
        hashed = md.digest(getWorkPath().getBytes());
      }
      catch (NoSuchAlgorithmException nsae2)
      {
        Logger.error("no such algorithm SHA1/MD5",nsae2);
        hashed = getWorkPath().getBytes();
      }
    }
    BASE64Encoder encoder = new BASE64Encoder();
    return encoder.encode(hashed);
  }

}

/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.19  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.17  2004/07/20 00:11:07  willuhn
 * @C Code sharing zwischen Ueberweisung und Dauerauftrag
 *
 * Revision 1.16  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.14  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.13  2004/05/11 23:31:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/05/11 21:11:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/05/09 17:39:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/04/13 23:14:23  willuhn
 * @N datadir
 *
 * Revision 1.8  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/17 00:06:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/09 22:09:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 * Revision 1.7  2004/01/28 00:37:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/01/28 00:31:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/01/25 19:44:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/01/03 18:07:22  willuhn
 * @N Exception logging
 *
 * Revision 1.3  2003/12/15 19:08:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/12/11 21:00:35  willuhn
 * @C refactoring
 *
 * Revision 1.1  2003/11/24 23:02:11  willuhn
 * @N added settings
 *
 **********************************************************************/