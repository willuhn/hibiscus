/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCI.java,v $
 * $Revision: 1.19 $
 * $Date: 2004/07/04 17:07:59 $
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.EmbeddedDatabase;
import de.willuhn.jameica.AbstractPlugin;
import de.willuhn.jameica.Application;
import de.willuhn.util.ApplicationException;
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
	 * Liefert die Programm-Version anhand des DB-Schemas.
   * @return Programm-Version.
   * @throws Exception
   */
  private double getDBVersion() throws Exception
	{
		EmbeddedDatabase db = this.getResources().getDatabase();

		String checkSum = db.getMD5Sum();
		if (checkSum.equals("KvynDJyxe6D1XUvSCkNAFA=="))
			return 1.0;
		throw new Exception("database checksum does not match any known version: " + checkSum);
	}

  /**
   * @see de.willuhn.jameica.AbstractPlugin#init()
   */
  public void init() throws ApplicationException
  {
  	Application.splash("init passport registry");
  	PassportRegistry.init();
		try {
			Application.splash("checking database integrity");
			getDBVersion();
		}
		catch (Exception e)
		{
			throw new ApplicationException(
				getResources().getI18N().tr("Fehler beim Prüfung der Datenbank-Integrität, " +					"Plugin wird aus Sicherheitsgründen deaktiviert"),e);
		}

		try {
			HBCIUtils.init(null,null,new HBCICallbackSWT());
			int logLevel = logMapping[Logger.getLevelByName(Application.getConfig().getLogLevel())][1];
			HBCIUtils.setParam("log.loglevel.default",""+logLevel);
		}
		catch (Exception e)
		{
			throw new ApplicationException(getResources().getI18N().tr("Fehler beim Initialisieren des HBCI-Subsystems"),e);
		}
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#install()
   */
  public void install() throws ApplicationException
  {
    try {
			EmbeddedDatabase db = getResources().getDatabase();
			db.executeSQLScript(new File(getResources().getPath() + "/sql/create.sql"));
    }
    catch (Exception e)
    {
			throw new ApplicationException(getResources().getI18N().tr("Fehler beim Erstellen der Datenbank"),e);
    }
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#update(double)
   */
  public void update(double oldVersion) throws ApplicationException
  {
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#shutDown()
   */
  public void shutDown()
  {
  }
}


/**********************************************************************
 * $Log: HBCI.java,v $
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