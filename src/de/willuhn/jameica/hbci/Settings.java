/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/Settings.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/04/05 23:28:46 $
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
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;

/**
 * Verwaltet die Einstellungen des Plugins.
 * @author willuhn
 */
public class Settings
{

  private static de.willuhn.jameica.Settings settings = new de.willuhn.jameica.Settings(HBCI.class);
  private static DBService db = null;
  private static String path = null;
  private static String passphrase = null;

	private static Color buchungSollForeground = null;
	private static Color buchungSollBackground = null;
	private static Color buchungHabenForeground = null;
	private static Color buchungHabenBackground = null;

  /**
   * Liefert den Datenbank-Service.
   * @return Datenbank.
   * @throws RemoteException
   */
  public static DBService getDatabase() throws RemoteException
  {
    if (db != null)
      return db;
    db = PluginLoader.getPlugin(HBCI.class).getResources().getDatabase().getDBService();
    return db;
  }

	/**
	 * Liefert die Vordergrundfarbe fuer Soll-Buchungen in Tabellen.
   * @return Farbe.
   */
  public static Color getBuchungSollForeground()
	{
		if (buchungSollForeground != null)
			return buchungSollForeground;

		buchungSollForeground = new Color(GUI.getDisplay(),readColors("buchung.soll.fg",0));
		return buchungSollForeground;
	}

	/**
	 * Liefert die Hintergrundfarbe fuer Soll-Buchungen in Tabellen.
	 * @return Farbe.
	 */
	public static Color getBuchungSollBackground()
	{
		if (buchungSollBackground != null)
			return buchungSollBackground;

		buchungSollBackground = new Color(GUI.getDisplay(),readColors("buchung.soll.bg",245));
		return buchungSollBackground;
	}

	/**
	 * Liefert die Vordergrundfarbe fuer Haben-Buchungen in Tabellen.
	 * @return Farbe.
	 */
	public static Color getBuchungHabenForeground()
	{
		if (buchungHabenForeground != null)
			return buchungHabenForeground;

		buchungHabenForeground = new Color(GUI.getDisplay(),readColors("buchung.haben.fg",0));
		return buchungHabenForeground;
	}

	/**
	 * Liefert die Hintergrundfarbe fuer Haben-Buchungen in Tabellen.
	 * @return Farbe.
	 */
	public static Color getBuchungHabenBackground()
	{
		if (buchungHabenBackground != null)
			return buchungHabenBackground;

		buchungHabenBackground = new Color(GUI.getDisplay(),readColors("buchung.haben.bg",255));
		return buchungHabenBackground;
	}

	/**
	 * Speichert die Farben fuer den Vordergrund von Soll-Buchungen.
   * @param rgb
   */
  public static void setBuchungSollForeground(RGB rgb)
	{
		storeColors("buchung.soll.fg",rgb);
		buchungSollForeground = null;
	}

	/**
	 * Speichert die Farben fuer den Hintergrund von Soll-Buchungen.
	 * @param rgb
	 */
	public static void setBuchungSollBackground(RGB rgb)
	{
		storeColors("buchung.soll.bg",rgb);
		buchungSollBackground = null;
	}

	/**
	 * Speichert die Farben fuer den Vordergrund von Haben-Buchungen.
	 * @param rgb
	 */
	public static void setBuchungHabenForeground(RGB rgb)
	{
		storeColors("buchung.haben.fg",rgb);
		buchungHabenForeground = null;
	}

	/**
	 * Speichert die Farben fuer den Hintergrund von Haben-Buchungen.
	 * @param rgb
	 */
	public static void setBuchungHabenBackground(RGB rgb)
	{
		storeColors("buchung.haben.bg",rgb);
		buchungHabenBackground = null;
	}

	/**
	 * Liefert die Farbwerte fuer den genannten Prefix.
   * @param prefix Name des Prefix.
   * @param default Default-Wert fuer R, G und B.
   * @return
   */
  private static RGB readColors(String prefix, int defaultValue)
	{
		return new RGB(
			settings.getInt(prefix + ".r",defaultValue),
			settings.getInt(prefix + ".g",defaultValue),
			settings.getInt(prefix + ".b",defaultValue)
		);
	}

	/**
	 * Speichert die Farbwerte unter dem genannten Prefix.
   * @param prefix Name des Prefix.
   * @param rgb Farbe.
   */
  private static void storeColors(String prefix, RGB rgb)
	{
		settings.setAttribute(prefix + ".r",rgb.red);
		settings.setAttribute(prefix + ".g",rgb.green);
		settings.setAttribute(prefix + ".b",rgb.blue);
	}

  /**
   * Liefert den Verzeichnis-Pfad in dem sich das Plugin befindet.
   * @return Pfad des Plugins.
   */
  public static String getPath()
  {
    if (path != null)
      return path;
    path = PluginLoader.getPlugin(HBCI.class).getResources().getPath();
    return path;
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
    return settings.getBoolean("checkpin",false);
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
   * Liefert das Passwort die lokalen Daten verschluesselt werden.
   * @return Passphrase.
   */
  protected static String getPassphrase()
  {
    if (passphrase != null)
      return passphrase;

    MessageDigest md = null;
    byte[] hashed = null;
    try {
      md = MessageDigest.getInstance("SHA1");
      hashed = md.digest(getPath().getBytes());
    }
    catch (NoSuchAlgorithmException nsae)
    {
      Application.getLog().warn("algorithm SHA1 not found, trying MD5");
      try {
        md = MessageDigest.getInstance("MD5");
        hashed = md.digest(getPath().getBytes());
      }
      catch (NoSuchAlgorithmException nsae2)
      {
        Application.getLog().error("no such algorithm SHA1/MD5",nsae2);
        hashed = getPath().getBytes();
      }
    }
    BASE64Encoder encoder = new BASE64Encoder();
    return encoder.encode(hashed);
  }

}

/*********************************************************************
 * $Log: Settings.java,v $
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