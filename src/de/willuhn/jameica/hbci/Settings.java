/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/Settings.java,v $
 * $Revision: 1.68 $
 * $Date: 2011/06/30 16:29:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import java.io.File;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
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
 * @author willuhn
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
   * BUGZILLA 227
   * Prueft, ob die HBCI-Synchronisierung im Fehlerfall abgebrochen werden soll.
   * @return true, wenn die Synchronisierung im Fehlerfall abbrechen soll.
   */
  public static boolean getCancelSyncOnError()
  {
    return settings.getBoolean("sync.cancelonerror",true);
  }

  /**
   * Prueft, ob die HBCI-Synchronisierung im Fehlerfall abgebrochen werden soll.
   * @param cancel true wenn die Synchronisierung im Fehlerfall abbrechen soll.
   */
  public static void setCancelSyncOnError(boolean cancel)
  {
    settings.setAttribute("sync.cancelonerror",cancel);
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
		{
      wallet = new Wallet(HBCI.class);
      
      // Migration BUGZILLA 62 - Loeschen der Liste der verbrauchten TANs
      String date = settings.getString("migration.tancache.cleared",null);
      if (date == null)
      {
        Logger.info("migration: removing cache of used tans");
        wallet.deleteAll("tan.");
        settings.setAttribute("migration.tancache.cleared",HBCI.LONGDATEFORMAT.format(new Date()));
      }
		}
		return wallet;
  }
  
  /**
   * Liefert das Default-Konto.
   * @return das Default-Konto.
   */
  public static Konto getDefaultKonto()
  {
    String id = settings.getString("defaultkonto.id",null);
    if (id == null || id.length() == 0)
      return null;
    try
    {
      return (Konto) getDBService().createObject(Konto.class,id);
    }
    catch (ObjectNotFoundException nfe)
    {
      // Konto existiert nicht mehr, resetten
      settings.setAttribute("defaultkonto.id",(String)null);
    }
    catch (Exception e)
    {
      Logger.error("unable to determine default account",e);
    }
    return null;
  }
  
  /**
   * Speichert das Default-Konto.
   * @param konto das Default-Konto oder NULL, wenn keines das Default-Konto sein soll.
   */
  public static void setDefaultKonto(Konto konto)
  {
    try
    {
      String id = konto == null ? null : konto.getID();
      settings.setAttribute("defaultkonto.id",id);
    }
    catch (Exception e)
    {
      Logger.error("unable to apply default account",e);
      // Einstellung loeschen
      settings.setAttribute("defaultkonto.id",(String) null);
    }
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

/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.68  2011/06/30 16:29:42  willuhn
 * @N Unterstuetzung fuer neues UnreadCount-Feature
 *
 * Revision 1.67  2011-06-09 10:14:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.66  2011-05-23 14:53:26  willuhn
 * @R wieder deaktiviert - wegen diesem arroganten Schnoesel hier: http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=75512#75512
 *
 * Revision 1.65  2011-05-23 12:57:38  willuhn
 * @N optionales Speichern der PINs im Wallet. Ich announce das aber nicht. Ich hab das nur eingebaut, weil mir das Gejammer der User auf den Nerv ging und ich nicht will, dass sich User hier selbst irgendwelche Makros basteln, um die PIN dennoch zu speichern
 *
 * Revision 1.64  2011-05-23 10:54:26  willuhn
 * @R BUGZILLA 62 - Liste der bisher gespeicherten TANs loeschen
 *
 * Revision 1.63  2010-09-16 09:54:05  willuhn
 * @B BUGZILLA #904
 *
 * Revision 1.62  2010/06/17 15:31:28  willuhn
 * @C BUGZILLA 622 - Defaultwert des checksum.saldo-Parameters geaendert - steht jetzt per Default auf false, sodass der Saldo NICHT mit in die Checksumme einfliesst
 * @B BUGZILLA 709 - Konto ist nun ENDLICH nicht mehr Bestandteil der Checksumme, dafuer sind jetzt alle Verwendungszweck-Zeilen drin
 *
 * Revision 1.61  2010/05/06 22:08:45  willuhn
 * @N BUGZILLA 622
 *
 * Revision 1.60  2009/03/31 11:01:40  willuhn
 * @R Speichern des PIN-Hashes komplett entfernt
 *
 * Revision 1.59  2009/03/18 22:10:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.58  2009/03/10 23:51:31  willuhn
 * @C PluginResources#getPath als deprecated markiert - stattdessen sollte jetzt Manifest#getPluginDir() verwendet werden
 *
 * Revision 1.57  2009/03/05 13:41:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.56  2009/02/19 10:24:51  willuhn
 * @C Default-Werte fuer System-Parameter geaendert
 *
 * Revision 1.55  2009/01/04 16:38:55  willuhn
 * @N BUGZILLA 523 - ein Konto kann jetzt als Default markiert werden. Das wird bei Auftraegen vorausgewaehlt und ist fett markiert
 *
 * Revision 1.54  2008/12/17 22:53:22  willuhn
 * @R steinalten Migrationscode entfernt
 *
 * Revision 1.53  2008/07/24 09:59:37  willuhn
 * @C Default-Wert des Auftragslimits erhoeht. 1.000,- waren in der Tat etwas wenig ;)
 **********************************************************************/