/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCI.java,v $
 * $Revision: 1.62 $
 * $Date: 2005/08/16 21:33:13 $
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.EmbeddedDatabase;
import de.willuhn.jameica.hbci.io.IORegistry;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 *
 */
public class HBCI extends AbstractPlugin
{

  /**
   * Datums-Format dd.MM.yyyy HH:mm:ss.
   */
  public static DateFormat EXTRALONGDATEFORMAT   = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  /**
   * Datums-Format dd.MM.yyyy HH:mm.
   */
  public static DateFormat LONGDATEFORMAT   = new SimpleDateFormat("dd.MM.yyyy HH:mm");

  /**
   * Datums-Format dd.MM.yyyy.
   */
  public static DateFormat DATEFORMAT       = new SimpleDateFormat("dd.MM.yyyy");

  /**
   * Datums-Format ddMMyyyy.
   */
  public static DateFormat FASTDATEFORMAT   = new SimpleDateFormat("ddMMyyyy");

  /**
   * DecimalFormat.
   */
  public static DecimalFormat DECIMALFORMAT = (DecimalFormat) DecimalFormat.getInstance(Application.getConfig().getLocale());

  // Mapper von HBCI4Java nach jameica Loglevels
  private static HashMap LOGMAPPING = new HashMap();

  static {
    //  BUGZILLA 101 http://www.willuhn.de/bugzilla/show_bug.cgi?id=101
    DECIMALFORMAT.applyPattern("###,###,##0.00");
    DECIMALFORMAT.setGroupingUsed(Settings.getDecimalGrouping());

    LOGMAPPING.put(Level.ERROR, new Integer(HBCIUtils.LOG_ERR));
    LOGMAPPING.put(Level.WARN,  new Integer(HBCIUtils.LOG_WARN));
    LOGMAPPING.put(Level.INFO,  new Integer(HBCIUtils.LOG_INFO));
    LOGMAPPING.put(Level.DEBUG, new Integer(HBCIUtils.LOG_DEBUG2));
  }

  private EmbeddedDatabase db = null;
  
  private HBCICallback callback;
  
  /**
   * ct.
   * @param file
   */
  public HBCI(File file)
  {
    super(file);
  }

  /**
   * Liefert die Datenbank des Plugins.
   * Lauft die Anwendung im Client-Mode, wird
   * immer <code>null</code> zurueckgegeben.
   * @return die Embedded Datenbank.
   * @throws Exception
   */
  private EmbeddedDatabase getDatabase() throws Exception
  {
    if (Application.inClientMode())
      return null;
    if (db != null)
      return db;
    db = new EmbeddedDatabase(getResources().getWorkPath() + "/db","hibiscus","hibiscus");
    return db;
  }

  /**
   * Prueft, ob sich die Datenbank der Anwendung im erwarteten
   * Zustand befindet (via MD5-Checksum). Entlarvt Manipulationen
   * des DB-Schemas durch Dritte.
   * @throws Exception
   */
  private void checkConsistency() throws Exception
	{
    
    if (Application.inClientMode())
    {
      // Wenn wir als Client laufen, muessen wir uns
      // nicht um die Datenbank kuemmern. Das macht
      // der Server schon
      return;
    }


		String checkSum = getDatabase().getMD5Sum();
		if (checkSum.equals("KvynDJyxe6D1XUvSCkNAFA==")) // 1.0
			return;

		if (checkSum.equals("Oj3JSimz84VKq44EEzQOZQ==")) // 1.1
			return;

		if (checkSum.equals("NhTl6Nt8RmaRNz49M/SGiA==")) // 1.2
			return;

		if (checkSum.equals("kwi5vy1fvgOOVtoTYJYjuA==")) // 1.3
			return;

    if (checkSum.equals("cAfcZCtiXAe/wNb2gFCH8A==")) // 1.4
      return;

		throw new Exception("database checksum does not match any known version: " + checkSum);
	}

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#init()
   */
  public void init() throws ApplicationException
  {
		Logger.info("starting init process for hibiscus");

    try {
			Application.getCallback().getStartupMonitor().setStatusText("hibiscus: checking database integrity");

      ////////////////////////////////////////////////////////////////////////////
      // TODO WIEDER ENTFERNEN, WENN RELEASED
      // Damit wir die Updates nicht immer haendisch nachziehen muessen, rufen wir
      // bei einem Fehler das letzte Update-Script nochmal auf.
			if (!Application.inClientMode())
      {
        try
        {
          de.willuhn.jameica.system.Settings s = new de.willuhn.jameica.system.Settings(HBCI.class);
          double size = s.getDouble("sql-update-size",-1);
          
          File f = new File(getResources().getPath() + "/sql/update_1.3-1.4.sql");
          
          if (f.length() != size)
          {
            getDatabase().executeSQLScript(f);
            s.setAttribute("sql-update-size",(double)f.length());
          }
        }
        catch (Exception e2)
        {
          e2.printStackTrace();
        }
      }
      ////////////////////////////////////////////////////////////////////////////

      checkConsistency();
		}
		catch (Exception e)
		{
      throw new ApplicationException(
          getResources().getI18N().tr("Fehler beim Prüfung der Datenbank-Integrität, " +
            "Plugin wird aus Sicherheitsgründen deaktiviert"),e);
		}

    Application.getCallback().getStartupMonitor().setStatusText("hibiscus: checking passport directory");
    String path = Settings.getWorkPath() + "/passports/";
    Logger.info("checking if " + path + " exists");
    File f = new File(path);
    if (!f.exists())
    {
      Logger.info("no, creating " + path);
      f.mkdirs();
    }


    if (!Application.inServerMode())
    {
      Application.getCallback().getStartupMonitor().setStatusText("hibiscus: init passport registry");
      PassportRegistry.init();
      Application.getCallback().getStartupMonitor().addPercentComplete(3);

    }

    Application.getCallback().getStartupMonitor().setStatusText("hibiscus: init hbci4java subsystem");

    try {
      int logLevel = HBCIUtils.LOG_INFO; // Default
      try
      {
        Level level = Logger.getLevel();
        Logger.info("current jameica log level: " + level.getName() + " [" + level.getValue() + "]");
        logLevel = ((Integer) LOGMAPPING.get(level)).intValue();
      }
      catch (Exception e)
      {
        Logger.warn("unable to map jameica log level into hbci4java log level. using default");
        // Am wahrscheinlichsten ArrayIndexOutOfBoundsException
        // Dann eben nicht ;)
      }
      Logger.info("HBCI4Java loglevel: " + logLevel);

      if (Application.inServerMode())
      {
        Logger.info("init HBCI4Java subsystem with console callback");
        this.callback = new HBCICallbackConsole();
      }
      else
      {
        Logger.info("init HBCI4Java subsystem with SWT callback");
        this.callback = new HBCICallbackSWT();
      }
      HBCIUtils.init(null,null,this.callback);
      HBCIUtils.setParam("log.loglevel.default",""+logLevel);

      de.willuhn.jameica.system.Settings s = new de.willuhn.jameica.system.Settings(HBCI.class);
      String rewriters = s.getString("hbci4java.kernel.rewriters",null);
      if (rewriters != null && rewriters.length() > 0)
      {
        Logger.warn("user defined rewriters found: " + rewriters);
        HBCIUtils.setParam("kernel.rewriters",rewriters);
      }
    }
    catch (Exception e)
    {
      throw new ApplicationException(getResources().getI18N().tr("Fehler beim Initialisieren des HBCI-Subsystems"),e);
    }
    Application.getCallback().getStartupMonitor().addPercentComplete(5);

    Application.getCallback().getStartupMonitor().setStatusText("hibiscus: init export filters");
    IORegistry.init();
    Application.getCallback().getStartupMonitor().addPercentComplete(3);
  
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#install()
   */
  public void install() throws ApplicationException
  {
    if (Application.inClientMode())
      return; // als Client muessen wir die DB nicht installieren

		Logger.info("starting install process for hibiscus");
    try {
			getDatabase().executeSQLScript(new File(getResources().getPath() + "/sql/create.sql"));
    }
    catch (Exception e)
    {
			throw new ApplicationException(getResources().getI18N().tr("Fehler beim Erstellen der Datenbank"),e);
    }
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#update(double)
   */
  public void update(double oldVersion) throws ApplicationException
  {
    if (Application.inClientMode())
      return; // Kein Update im Client-Mode noetig.

		Logger.info("starting update process for hibiscus");

		DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
		df.setMaximumFractionDigits(1);
		df.setMinimumFractionDigits(1);
		df.setGroupingUsed(false);

		double newVersion = oldVersion + 0.1d;

		File f = new File(getResources().getPath() + "/sql/update_" + 
											df.format(oldVersion) + "-" + 
											df.format(newVersion) + ".sql");

		try
		{
			Logger.info("checking sql file " + f.getAbsolutePath());
			while (f.exists())
			{
				Logger.info("  file exists, executing");
				getDatabase().executeSQLScript(f);
				oldVersion = newVersion;
				newVersion = oldVersion + 0.1d;
				f = new File(getResources().getPath() + "/sql/update_" + 
									   df.format(oldVersion) + "-" + 
									   df.format(newVersion) + ".sql");
			}
		}
		catch (Exception e)
		{
			throw new ApplicationException(getResources().getI18N().tr("Fehler beim Update der Datenbank"),e);
		}
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#shutDown()
   */
  public void shutDown()
  {
  }

  /**
   * Liefert den verwendeten HBCI-Callback.
   * Die Funktion ist ein Zugestaendnis an RMI.
   * Hintergrund: HBCI4Java speichert seine Config pro ThreadGroup. Laeuft Hibiscus
   * im Client/Server-Szenario und kommen von aussen (durch Clients) Anfragen rein,
   * dann treffen die beim Server in den Threads der RMI-Runtime ein. Und die hat
   * sich eine eigene Threadgroup geschaffen. Da wir RMI scheinbar nicht vorschreiben
   * koennen, dass es unsere Threadgroup verwenden soll, muessen wir HBCI4Java
   * pro ThreadGroup und damit ggf. mehrfach initialisieren.
   * @return liefert den verwendeten HBCICallback.
   */
  HBCICallback getHBCICallback()
  {
    return this.callback;
  }
}


/**********************************************************************
 * $Log: HBCI.java,v $
 * Revision 1.62  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 * Revision 1.61  2005/08/08 14:39:08  willuhn
 * @C user defined rewriter list
 *
 * Revision 1.60  2005/07/29 16:48:13  web0
 * @N Synchronize
 *
 * Revision 1.59  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 * Revision 1.58  2005/07/24 22:26:42  web0
 * @B bug 101
 *
 * Revision 1.57  2005/06/30 21:48:56  web0
 * @B bug 75
 *
 * Revision 1.56  2005/06/27 11:26:30  web0
 * @N neuer Test bei Dauerauftraegen (zum Monatsletzten)
 * @N neue DDV-Lib
 *
 * Revision 1.55  2005/06/15 17:51:09  web0
 * *** empty log message ***
 *
 * Revision 1.54  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.53  2005/06/13 23:11:01  web0
 * *** empty log message ***
 *
 * Revision 1.52  2005/06/08 16:49:00  web0
 * @N new Import/Export-System
 *
 * Revision 1.51  2005/06/02 22:57:34  web0
 * @N Export von Konto-Umsaetzen
 *
 * Revision 1.50  2005/05/30 12:01:03  web0
 * @R removed OP stuff
 *
 * Revision 1.49  2005/05/24 23:30:03  web0
 * @N Erster Code fuer OP-Verwaltung
 *
 * Revision 1.48  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.47  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 * Revision 1.46  2005/03/24 16:49:02  web0
 * @B error in log mapping
 *
 * Revision 1.45  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.44  2005/02/28 15:30:47  web0
 * @B Bugzilla #15
 *
 * Revision 1.43  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.42  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.41  2005/02/08 22:28:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.40  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.39  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.38  2005/01/30 20:45:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.37  2005/01/19 00:33:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2005/01/05 15:17:50  willuhn
 * @N Neues Service-System in Jameica
 *
 * Revision 1.35  2004/11/26 01:23:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.34  2004/11/17 19:02:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.33  2004/11/15 18:09:18  willuhn
 * @N Login fuer die gesamte Anwendung
 *
 * Revision 1.32  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.31  2004/11/04 17:31:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.30  2004/10/25 17:58:57  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.29  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.28  2004/10/14 23:14:20  willuhn
 * @N new hbci4java (2.5pre)
 * @B fixed locales
 *
 * Revision 1.27  2004/10/08 00:19:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.26  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.25  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.24  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/07/15 23:39:22  willuhn
 * @N TurnusImpl
 *
 * Revision 1.22  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.21  2004/07/13 23:26:14  willuhn
 * @N Views fuer Dauerauftrag
 *
 * Revision 1.20  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 * Revision 1.19  2004/07/04 17:07:59  willuhn
 * @B Umsaetze wurden teilweise nicht als bereits vorhanden erkannt und wurden somit doppelt angezeigt
 *
 * Revision 1.18  2004/07/01 19:46:27  willuhn
 * @N db integrity check
 *
 * Revision 1.17  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.15  2004/05/05 21:10:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.13  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.12  2004/04/04 18:30:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/04/01 22:06:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/19 01:44:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/17 00:06:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.7  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.6  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.4  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/09 22:09:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 **********************************************************************/