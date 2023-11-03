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

import java.io.File;
import java.rmi.ConnectException;
import java.rmi.RemoteException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Verwaltet die Einstellungen des Plugins.
 */
public class Settings
{

  private static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private static HBCIDBService db = null;
	private static String workPath = null;
	private static String libPath = null;
	private static Wallet wallet = null;

	private static Color buchungSollForeground = null;
	private static Color buchungHabenForeground = null;

  /**
   * Liefert den Datenbank-Service.
   * @return Datenbank.
   * @throws RemoteException
   */
  public static HBCIDBService getDBService() throws RemoteException
  {
    if (db != null)
      return db;
		try {
			db = (HBCIDBService) Application.getServiceFactory().lookup(HBCI.class,"database");
			return db;
		}
    catch (ConnectException ce)
    {
      // Die Exception fliegt nur bei RMI-Kommunikation mit fehlendem RMI-Server
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      String host = Application.getServiceFactory().getLookupHost(HBCI.class,"database");
      int    port = Application.getServiceFactory().getLookupPort(HBCI.class,"database");
      String msg = i18n.tr("Hibiscus-Server \"{0}\" nicht erreichbar", (host + ":" + port));
      try
      {
        Application.getCallback().notifyUser(msg);
        throw new RemoteException(msg);
      }
      catch (Exception e)
      {
        Logger.error("error while notifying user",e);
        throw new RemoteException(msg);
      }
    }
    catch (ApplicationException ae)
    {
      // Da interessiert uns der Stacktrace nicht
      throw new RemoteException(ae.getMessage());
    }
    catch (RemoteException re)
    {
      throw re;
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

		buchungSollForeground = new Color(GUI.getDisplay(),settings.getRGB("buchung.soll.fg",new RGB(226,102,38)));
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

		buchungHabenForeground = new Color(GUI.getDisplay(),settings.getRGB("buchung.haben.fg",new RGB(29,158,33)));
		return buchungHabenForeground;
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
	 * Liefert den Verzeichnis-Pfad zu den nativen Libs.
	 * @return Pfad der Libs.
	 */
	public static String getLibPath()
	{
		if (libPath != null)
			return libPath;
		libPath = Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getPluginDir() + "/lib";
    try
    {
      libPath = new File(libPath).getCanonicalPath();
    }
    catch (Exception e)
    {
      Logger.error("error while determining canonical path",e);
    }
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
   * Legt fest, ob die PIN pro Session gecached werden soll.
   * @param cachePin true, wenn die Pin gecached werden soll.
   */
  public static void setCachePin(boolean cachePin)
  {
    settings.setAttribute("cachepin",cachePin);
  }

  /**
   * Prueft, ob die PIN-Eingaben pro Session zwischengespeichert werden sollen.
   * @return true, wenn die Pin gecached werden soll.
   */
  public static boolean getCachePin()
  {
    return settings.getBoolean("cachepin",true);
  }

  /**
   * Legt fest, ob die PIN permanent gespeichert werden soll.
   * Laesst sich nur aktivieren, wenn auch das Cachen der PINs aktiviert ist.
   * @param storePin true, wenn die Pin gespeichert werden soll.
   */
  public static void setStorePin(boolean storePin)
  {
    settings.setAttribute("storepin",getCachePin() && storePin);
  }

  /**
   * Prueft, ob die PIN-Eingaben permanent gespeichert werden sollen.
   * Liefert nur true, wenn auch das Cachen der PINs aktiviert ist
   * und wenn das Master-Passwort manuell eingegeben wurde. Wurde
   * das Master-Passwort via Parameter "-p" uebergeben, ist das
   * Speichern der PIN nicht zulaessig.
   * @return true, wenn die Pin gecached werden soll.
   */
  public static boolean getStorePin()
  {
    return Application.getStartupParams().getPassword() == null && getCachePin() && settings.getBoolean("storepin",false);
  }

  /**
   * Prueft, ob Tausender-Punkte bei Betraegen verwendet werden sollen.
   * @return true, wenn Tausender-Punkte verwendet werden sollen.
   */
  public static boolean getDecimalGrouping()
  {
    return settings.getBoolean("decimalgrouping",false);
  }

  /**
   * Legt fest, ob Tausender-Punkte bei Betraegen verwendet werden sollen.
   * @param grouping true, wenn Tausender-Punkte verwendet werden sollen.
   */
  public static void setDecimalGrouping(boolean grouping)
  {
    settings.setAttribute("decimalgrouping",grouping);
    HBCI.DECIMALFORMAT.setGroupingUsed(grouping);
  }

  /**
   * Prueft, ob Geldbetraege fett gedruckt angezeigt werden sollen.
   * @return true, wenn Geldbetraege fett gedruckt angezeigt werden sollen.
   */
  public static boolean getBoldValues()
  {
    return settings.getBoolean("boldvalues",true);
  }

  /**
   * Legt fest, ob Geldbetraege fett gedruckt angezeigt werden sollen.
   * @param bold  true, wenn Geldbetraege fett gedruckt angezeigt werden sollen.
   */
  public static void setBoldValues(boolean bold)
  {
    settings.setAttribute("boldvalues",bold);
  }

  /**
   * Prueft, ob nur Geldbetraege farbig angezeigt werden sollen.
   * @return true, wenn nur Geldbetraege farbig gedruckt angezeigt werden sollen.
   */
  public static boolean getColorValues()
  {
    return settings.getBoolean("colorvalues",true);
  }

  /**
   * Legt fest, ob nur Geldbetraege farbig angezeigt werden sollen.
   * @param color true, wenn nur Geldbetraege farbig angezeigt werden sollen.
   */
  public static void setColorValues(boolean color)
  {
    settings.setAttribute("colorvalues",color);
  }

  /**
   * Speichert, ob wir eine permanente Online-Verbindung haben und daher
   * vom HBCI-Kernel nicht dauernd gefragt werden muessen, ob wir eine
   * Internetverbindung haben wollen.
   * @param online true, wenn wir dauernd online sind.
   */
  public static void setOnlineMode(boolean online)
  {
    settings.setAttribute("online",online);
  }

  /**
   * Liefert true, wenn die Kontonummern via Pruefsumme gecheckt werden sollen.
   * @return true, wenn die Pruefziffern-Kontrolle aktiviert ist.
   */
  public static boolean getKontoCheck()
  {
    return settings.getBoolean("kontocheck",true);
  }

  /**
   * Legt fest, ob die Kontonummern via Pruefsumme gecheckt werden sollen.
   * @param check true, wenn gecheckt werden soll.
   */
  public static void setKontoCheck(boolean check)
  {
    settings.setAttribute("kontocheck",check);
  }

  /**
   * Liefert true, wenn Bankverbindungen aus dem Adressbuch aus der Pruefung ausgenommen werden sollen.
   * @return true, wenn Bankverbindungen aus dem Adressbuch aus der Pruefung ausgenommen werden sollen.
   */
  public static boolean getKontoCheckExcludeAddressbook()
  {
    return settings.getBoolean("kontocheck.addressbook.exclude",false);
  }

  /**
   * Legt fest, ob Bankverbindungen aus dem Adressbuch aus der Pruefung ausgenommen werden sollen.
   * @param check true, wenn Bankverbindungen aus dem Adressbuch aus der Pruefung ausgenommen werden sollen.
   */
  public static void setKontoCheckExcludeAddressbook(boolean check)
  {
    settings.setAttribute("kontocheck.addressbook.exclude",check);
  }

  /**
   * Liefert true, wenn die Ungelesen-Markierungen in der Datenbank gespeichert werden soll.
   * @return true, wenn die Ungelesen-Markierungen in der Datenbank gespeichert werden soll.
   */
  public static boolean getStoreUnreadFlag()
  {
    return settings.getBoolean("unread.store",true);
  }

  /**
   * Legt fest, ob die Ungelesen-Markierungen in der Datenbank gespeichert werden sollen.
   * @param store true, wenn die Ungelesen-Markierungen in der Datenbank gespeichert werden sollen.
   */
  public static void setStoreUnreadFlag(boolean store)
  {
    settings.setAttribute("unread.store",store);
  }

  /**
   * Liefert true, wenn die Ungelesen-Markierungen beim Beenden zurückgesetzt werden soll.
   * @return true, wenn die Ungelesen-Markierungen beim Beenden zurückgesetzt werden soll.
   */
  public static boolean getMarkReadOnExit()
  {
    return settings.getBoolean("unread.markreadonexit",true);
  }

  /**
   * Legt fest, ob die Ungelesen-Markierungen beim Beenden zurückgesetzt werden soll.
   * @param b true, wenn die Ungelesen-Markierungen beim Beenden zurückgesetzt werden soll.
   */
  public static void setMarkReadOnExit(boolean b)
  {
    settings.setAttribute("unread.markreadonexit",b);
  }

  /**
   * Prueft, ob wir eine permanente Online-Verbindung haben und daher
   * vom HBCI-Kernel nicht dauernd gefragt werden muessen, ob wir eine
   * Internetverbindung haben wollen.
   * @return true, wenn wir dauernd online sind.
   */
  public static boolean getOnlineMode()
  {
    return settings.getBoolean("online",true);
  }

  /**
	 * Liefert das Limit bei Ueberweisungen.
	 * Soll den Benutzer davor schuetzen, versehentlich zu grosse Betraege bei
	 * einer Ueberweisung einzugeben.
   * @return Ueberweisungslimit.
   */
  public static double getUeberweisungLimit()
	{
		return settings.getDouble("ueberweisunglimit",10000.0);
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
   * Prueft, ob der Saldo in die Berechnung der Umsatz-Checksumme einfliessen soll.
   * @return true, wenn er einfliessen soll (false ist der Default-Wert).
   * BUGZILLA 622
   */
  public static boolean getSaldoInChecksum()
  {
    return settings.getBoolean("umsatz.checksum.saldo",false);
  }

  /**
   * Liefert das von Hibiscus verwendete Wallet.
   * @return das Wallet.
   * @throws Exception
   */
  public static Wallet getWallet() throws Exception
  {
		if (wallet == null)
      wallet = new Wallet(HBCI.class);
		
		return wallet;
  }

  /**
   * Prueft, ob es der erste Hibiscus-Start ist bzw noch keine Konten existieren.
   * @return true, wenn noch keine Konten existieren.
   */
  public static boolean isFirstStart()
  {
    // Wir checken erstmal, ob das Plugin ueberhaupt geladen wurde
    if (!Application.getPluginLoader().getManifest(HBCI.class).isInstalled())
      return true;

    try
    {
      DBIterator konten = Settings.getDBService().createList(Konto.class);
      return konten.size() == 0;
    }
    catch (Exception e)
    {
      Logger.error("unable to load konto list",e);
      return true; // wir liefern hier true, damit die Boxen nicht angezeigt werden
    }
  }
}
