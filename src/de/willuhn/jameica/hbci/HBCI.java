/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCI.java,v $
 * $Revision: 1.33 $
 * $Date: 2004/11/15 18:09:18 $
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
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.db.EmbeddedDatabase;
import de.willuhn.jameica.hbci.gui.dialogs.LoginDialog;
import de.willuhn.jameica.hbci.rmi.Login;
import de.willuhn.jameica.hbci.server.HBCIDBServiceImpl;
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

  private Login currentLogin = null;

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
  private static int[][] logMapping = new int[][]
  {
    {Level.ERROR.getValue(), 1},
    {Level.WARN.getValue(),  2},
    {Level.INFO.getValue(),  3},
		{Level.DEBUG.getValue(), 5}
  };

  static {
    DECIMALFORMAT.applyPattern("#0.00");
    DECIMALFORMAT.setGroupingUsed(false);
    DECIMALFORMAT.setMinimumFractionDigits(2);
  }

  private EmbeddedDatabase db = null;

  /**
   * ct.
   * @param file
   */
  public HBCI(File file)
  {
    super(file);
    Locale l = Application.getConfig().getLocale();
  }

  /**
   * Liefert den gerade eingeloggten Benutzer.
   * @return Benutzer.
   * @throws RemoteException
   */
  public Login getCurrentLogin() throws RemoteException
  {
    return currentLogin;
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

		if (checkSum.equals("0619/tBF02SlEZ0spWjWHA==")) // 1.1
			return;

		throw new Exception("database checksum does not match any known version: " + checkSum);
	}

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#init()
   */
  public void init() throws ApplicationException
  {
		try {
			Application.getStartupMonitor().setStatusText("hibiscus: checking database integrity");
			checkConsistency();
		}
		catch (Exception e)
		{
			throw new ApplicationException(
				getResources().getI18N().tr("Fehler beim Prüfung der Datenbank-Integrität, " +					"Plugin wird aus Sicherheitsgründen deaktiviert"),e);
		}

    if (!Application.inServerMode())
    {
      Application.getStartupMonitor().setStatusText("hibiscus: starting login");
      try
      {
        LoginDialog login = new LoginDialog(LoginDialog.POSITION_CENTER);
        this.currentLogin = (Login) login.open();
      }
      catch (Exception e)
      {
        Logger.error("error while performing login, hibiscus will be disabled",e);
        throw new ApplicationException("login failed, disable hibiscus");
      }
      
    }

    Application.getStartupMonitor().setStatusText("hibiscus: init passport registry");
		PassportRegistry.init();

		try {
			HBCIUtils.init(null,null,new HBCICallbackSWT(Settings.getHBCIProgressBar()));
			int logLevel = 3;
			try
			{
				logLevel = logMapping[Logger.getLevel().getValue()][1];
			}
			catch (Exception e)
			{
				// Am wahrscheinlichsten ArrayIndexOutOfBoundsException
				// Dann eben nicht ;)
			}

			HBCIUtils.setParam("log.loglevel.default",""+logLevel);
		}
		catch (Exception e)
		{
			throw new ApplicationException(getResources().getI18N().tr("Fehler beim Initialisieren des HBCI-Subsystems"),e);
		}
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#install()
   */
  public void install() throws ApplicationException
  {
    if (Application.inClientMode())
      return; // als Client muessen wir die DB nicht installieren

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

  	if (oldVersion == 1.0)
  	{
			try {
				getDatabase().executeSQLScript(new File(getResources().getPath() + "/sql/update_1.0-1.1.sql"));
			}
			catch (Exception e)
			{
				throw new ApplicationException(getResources().getI18N().tr("Fehler beim Update der Datenbank"),e);
			}
  	}
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#shutDown()
   */
  public void shutDown()
  {
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#getServiceNames()
   */
  public String[] getServiceNames()
  {
    // Wir haben derzeit nur die eine Datenquelle 
    return new String[] {"database"};
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#getService(java.lang.String)
   */
  public Class getService(String serviceName)
  {
    if ("database".equals(serviceName))
      return HBCIDBServiceImpl.class;

    return null;
  }
}


/**********************************************************************
 * $Log: HBCI.java,v $
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