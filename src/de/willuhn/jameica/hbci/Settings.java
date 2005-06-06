/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/Settings.java,v $
 * $Revision: 1.33 $
 * $Date: 2005/06/06 09:54:39 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import java.rmi.RemoteException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;

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
	private static Wallet wallet = null;

	private static Color buchungSollForeground = null;
	private static Color buchungHabenForeground = null;
	private static Color ueberfaelligForeground = null;
	
  /**
   * Liefert den Datenbank-Service.
   * @return Datenbank.
   * @throws RemoteException
   */
  public static DBService getDBService() throws RemoteException
  {
    if (db != null)
      return db;
		try {
			db = (DBService) Application.getServiceFactory().lookup(HBCI.class,"database");
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
		libPath = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getPath() + "/lib";
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
		workPath = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath();
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
   * Prueft, ob die TAN waehrend der Eingabe angezeigt werden soll.
   * @return true, wenn die TANs angezeigt werden sollen.
   */
  public static boolean getShowTan()
  {
    return settings.getBoolean("showtan",false);
  }

  /**
   * Legt fest, ob die TANs bei der Eingabe angezeigt werden sollen.
   * @param show true, wenn sie angezeigt werden sollen.
   */
  public static void setShowTan(boolean show)
  {
    settings.setAttribute("showtan",show);
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
   * Liefert das von Hibiscus verwendete Wallet.
   * @return das Wallet.
   * @throws Exception
   */
  public static Wallet getWallet() throws Exception
  {
		if (wallet == null)
			wallet = Application.getSSLFactory().getWallet(HBCI.class);
		return wallet;
  }
}

/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.33  2005/06/06 09:54:39  web0
 * *** empty log message ***
 *
 * Revision 1.32  2005/05/02 11:54:09  web0
 * *** empty log message ***
 *
 * Revision 1.31  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.30  2005/02/08 22:28:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 * Revision 1.28  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2005/01/30 20:45:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2005/01/15 16:48:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2005/01/09 23:21:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/12/06 22:45:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.20  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
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