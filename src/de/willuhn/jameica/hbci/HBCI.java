/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCI.java,v $
 * $Revision: 1.13 $
 * $Date: 2004/04/27 22:23:56 $
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
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.EmbeddedDatabase;
import de.willuhn.jameica.AbstractPlugin;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.Logger;

/**
 *
 */
public class HBCI extends AbstractPlugin
{

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
  public static DecimalFormat DECIMALFORMAT = (DecimalFormat) NumberFormat.getNumberInstance(Application.getConfig().getLocale());

  // Mapper von HBCI4Java nach jameica Loglevels
  private static int[][] logMapping = new int[][]
  {
    {Logger.LEVEL_DEBUG, 5},
    {Logger.LEVEL_ERROR, 1},
    {Logger.LEVEL_WARN,  2},
    {Logger.LEVEL_INFO,  3}
  };

  static {
    DECIMALFORMAT.applyPattern("#0.00");
  }

  /**
   * ct.
   * @param file
   */
  public HBCI(File file)
  {
    super(file);
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#init()
   */
  public boolean init()
  {
		// Wir oeffnen mal die Datenbank.
		// Grund: Beim ersten DB-Connect kommt es immer zu etwas Verzoegerung,
		// weil McKOI gestartet werden muss. Wir machen das waehrend des Splash-Screens
		// damit es sybjektiv nicht so lange dauert
		try {
			getResources().getDatabase().getDBService().createObject(Konto.class,null);
		}
		catch (RemoteException e)
		{
			// Uuuh? Wenn das fehlschlaegt, ist etwas mit unserer DB faul und
			// wir verweigern lieber die Initialisierung
			Application.getLog().error("error while initializing HBCI plugin",e);
		}

    HBCIUtils.init(null,null,new HBCICallbackSWT());
    int logLevel = logMapping[Application.getLog().getLevelByName(Application.getConfig().getLogLevel())][1];
    HBCIUtils.setParam("log.loglevel.default",""+logLevel);
    return true;
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#install()
   */
  public boolean install()
  {
    EmbeddedDatabase db = getResources().getDatabase();
    if (!db.exists())
    {
      try {
        db.create();
      }
      catch (IOException e)
      {
        Application.getLog().error("unable to create database",e);
        return false;
      }
      try
      {
        db.executeSQLScript(new File(getResources().getPath() + "/sql/create.sql"));
      }
      catch (Exception e)
      {
        Application.getLog().error("unable to create sql tables",e);
        return false;
      }
      try
      {
        db.executeSQLScript(new File(getResources().getPath() + "/sql/init.sql"));
      }
      catch (Exception e)
      {
        Application.getLog().error("unable to insert init data",e);
        return false;
      }
    }
    return true;
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#update(double)
   */
  public boolean update(double oldVersion)
  {
		// Mal schauen, ob fuer diesen Versionswechsel ein Update-Script vorliegt.
		File update = new File(getResources().getPath() + "/sql/update_" + oldVersion + "-" + getVersion() + ".sql");
		if (!update.exists())
			return true;

		try
		{
			EmbeddedDatabase db = getResources().getDatabase();
			db.executeSQLScript(update);
			return true;
		}
		catch (Exception e)
		{
			Application.getLog().error("unable to execute update sql script " + update.getAbsolutePath(),e);
			return false;
		}
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#shutDown()
   */
  public void shutDown()
  {
  	getResources().getI18N().flush();
  }
}


/**********************************************************************
 * $Log: HBCI.java,v $
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