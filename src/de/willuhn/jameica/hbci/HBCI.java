/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCI.java,v $
 * $Revision: 1.104 $
 * $Date: 2007/11/27 16:41:48 $
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
import java.util.HashMap;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.hbci.rmi.HBCIDBService;
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
   * @see de.willuhn.jameica.plugin.AbstractPlugin#init()
   */
  public void init() throws ApplicationException
  {
		Logger.info("starting init process for hibiscus");

    //  BUGZILLA 101 http://www.willuhn.de/bugzilla/show_bug.cgi?id=101
    DECIMALFORMAT.applyPattern("###,###,##0.00");
    DECIMALFORMAT.setGroupingUsed(Settings.getDecimalGrouping());

    LOGMAPPING.put(Level.ERROR, new Integer(HBCIUtils.LOG_ERR));
    LOGMAPPING.put(Level.WARN,  new Integer(HBCIUtils.LOG_WARN));
    LOGMAPPING.put(Level.INFO,  new Integer(HBCIUtils.LOG_INFO));
    LOGMAPPING.put(Level.DEBUG, new Integer(HBCIUtils.LOG_DEBUG2));

    call(new ServiceCall()
    {
      public void call(HBCIDBService service) throws ApplicationException, RemoteException
      {
        service.checkConsistency();
      }
    });
    

    /////////////////////////////////////////////////////////////////
    // Passport-Verzeichnis ggf. automatisch anlegen
    String path = Settings.getWorkPath() + "/passports/";
    File f = new File(path);
    if (!f.exists()) f.mkdirs();
    /////////////////////////////////////////////////////////////////

    initHBCI(getResources().getSettings().getString("hbcicallback.class",HBCICallbackSWT.class.getName()));
    Application.getCallback().getStartupMonitor().addPercentComplete(5);
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#install()
   */
  public void install() throws ApplicationException
  {
    call(new ServiceCall() {
    
      public void call(HBCIDBService service) throws ApplicationException, RemoteException
      {
        service.install();
      }
    });
  }
  
  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#update(double)
   */
  public void update(final double oldVersion) throws ApplicationException
  {
    call(new ServiceCall() {
      
      public void call(HBCIDBService service) throws ApplicationException, RemoteException
      {
        service.update(oldVersion,getManifest().getVersion());
      }
    });
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#shutDown()
   */
  public void shutDown()
  {
  }

  /**
   * Initialisiert das HBCI4Java-Subsystem.
   * @param callbackClass der zu verwendende Callback.
   * @throws ApplicationException
   */
  public final synchronized void initHBCI(String callbackClass) throws ApplicationException
  {
    /////////////////////////////////////////////////////////////////
    // HBCI4Java laden
    Application.getCallback().getStartupMonitor().setStatusText("hibiscus: init hbci4java subsystem");
    try
    {
      if (this.callback != null)
      {
        try
        {
          // Wir wurden schonmal initialisiert
          HBCIUtils.done();
        }
        catch (Throwable t)
        {
          // ignore
        }
      }

      //////////////////////////////////
      // Callback erzeugen
      this.callback = null;
      
      if (callbackClass != null && callbackClass.length() > 0)
      {
        try
        {
          this.callback = (HBCICallback) Application.getClassLoader().load(callbackClass).newInstance();
          Logger.info("callback: " + this.callback.getClass().getName());
        }
        catch (Throwable t)
        {
          Logger.error("unable to load custom callback - fallback to default",t);
        }
      }
      
      if (this.callback == null)
      {
        if (Application.inServerMode())
          this.callback = new HBCICallbackConsole();
        else
          this.callback = new HBCICallbackSWT();
      }
      //////////////////////////////////

      HBCIUtils.init(null,null,this.callback);

      //////////////////////////////////
      // Log-Level
      int logLevel = HBCIUtils.LOG_INFO; // Default
      try
      {
        logLevel = ((Integer) LOGMAPPING.get(Logger.getLevel())).intValue();
      }
      catch (Exception e)
      {
        // Am wahrscheinlichsten ArrayIndexOutOfBoundsException
        // Dann eben nicht ;)
        Logger.warn("unable to map jameica log level into hbci4java log level. using default");
      }
      HBCIUtils.setParam("log.loglevel.default",""+logLevel);
      //////////////////////////////////
      
      HBCIUtils.setParam("client.product.name","HBCI4Java (Hibiscus " + getManifest().getVersion() + ")");

      
      //////////////////////////////////
      // Rewriter
      String rewriters = getResources().getSettings().getString("hbci4java.kernel.rewriters",null);
      if (rewriters != null && rewriters.length() > 0)
      {
        Logger.warn("user defined rewriters found: " + rewriters);
        HBCIUtils.setParam("kernel.rewriters",rewriters);
      }
      //////////////////////////////////
    }
    catch (Exception e)
    {
      throw new ApplicationException(getResources().getI18N().tr("Fehler beim Initialisieren des HBCI-Subsystems"),e);
    }
  }
  
  /**
   * Liefert den aktuellen HBCI-Callback.
   * @return liefert den verwendeten HBCICallback.
   */
  public HBCICallback getHBCICallback()
  {
    return this.callback;
  }
  
  /**
   * Hilfsmethode zum bequemen Ausfuehren von Aufrufen auf dem Service.
   */
  private interface ServiceCall
  {
    /**
     * @param service
     * @throws ApplicationException
     * @throws RemoteException
     */
    public void call(HBCIDBService service) throws ApplicationException, RemoteException;
  }
  
  /**
   * Hilfsmethode zum bequemen Ausfuehren von Methoden auf dem Service.
   * @param call der Call.
   * @throws ApplicationException
   */
  private void call(ServiceCall call) throws ApplicationException
  {
    if (Application.inClientMode())
      return; // als Client muessen wir die DB nicht installieren

    HBCIDBService service = null;
    try
    {
      // Da die Service-Factory zu diesem Zeitpunkt noch nicht da ist, erzeugen
      // wir uns eine lokale Instanz des Services.
      service = new HBCIDBServiceImpl();
      service.start();
      call.call(service);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      throw new ApplicationException(getResources().getI18N().tr("Fehler beim Initialisieren der Datenbank"),e);
    }
    finally
    {
      if (service != null)
      {
        try
        {
          service.stop(true);
        }
        catch (Exception e)
        {
          Logger.error("error while closing db service",e);
        }
      }
    }
  }
  
}


/**********************************************************************
 * $Log: HBCI.java,v $
 * Revision 1.104  2007/11/27 16:41:48  willuhn
 * @C MessageConsumers fuer Query-Lookups wurden zu frueh registriert
 *
 * Revision 1.103  2007/11/12 00:08:02  willuhn
 * @N Query-Messages fuer Bankname-Lookup und CRC-Account-Check fuer JVerein
 *
 * Revision 1.102  2007/06/21 11:33:13  willuhn
 * @N PassportRegistry erst bei Bedarf initialisieren
 *
 * Revision 1.101  2007/06/05 00:41:53  willuhn
 * @N send product identifier in HKVVB
 *
 * Revision 1.100  2007/05/30 09:34:55  willuhn
 * @B Seit Support fuer MySQL wurde die DB-Checksummen-Pruefung sowie das automatische SQL-Update bei Nightly-Builds vergessen
 *
 * Revision 1.99  2007/05/16 16:25:57  willuhn
 * @C Sauberes Re-Init des HBCI-Subsystem
 *
 * Revision 1.98  2007/05/16 13:59:53  willuhn
 * @N Bug 227 HBCI-Synchronisierung auch im Fehlerfall fortsetzen
 * @C Synchronizer ueberarbeitet
 * @B HBCIFactory hat globalen Status auch bei Abbruch auf Error gesetzt
 *
 * Revision 1.97  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.96  2007/03/10 07:16:10  jost
 * Neue Checksumme für die aktuelle DB-Version
 *
 * Revision 1.95  2006/11/23 23:24:17  willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 *
 * Revision 1.94  2006/11/17 00:06:48  willuhn
 * @N increased version number in sql update script
 *
 * Revision 1.93  2006/08/21 12:29:54  willuhn
 * @N HBCICallbackSWT.setCurrentHandle
 *
 * Revision 1.92  2006/08/05 22:00:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.91  2006/06/29 23:10:34  willuhn
 * @R Box-System aus Hibiscus in Jameica-Source verschoben
 * @C keine eigene Startseite mehr, jetzt alles ueber Jameica-Boxsystem geregelt
 *
 * Revision 1.90  2006/05/11 20:34:16  willuhn
 * @B fehleranfaellige SQL-Updates entfernt
 *
 * Revision 1.89  2006/04/27 22:26:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.88  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 * Revision 1.87  2006/04/03 20:37:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.86  2006/03/24 00:15:36  willuhn
 * @B Duplikate von Settings-Instanzen entfernt
 *
 * Revision 1.85  2006/02/27 16:54:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.84  2006/02/26 18:40:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.83  2006/02/26 18:15:39  willuhn
 * @N hbcicallback can be customized now
 *
 * Revision 1.82  2006/01/18 18:40:35  willuhn
 * @N Redesign des Background-Task-Handlings
 *
 * Revision 1.81  2006/01/17 00:22:37  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 * Revision 1.80  2005/12/30 00:27:52  willuhn
 * @N sql update in init()
 *
 * Revision 1.79  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.78  2005/12/29 01:22:12  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
 * Revision 1.77  2005/12/17 18:59:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.76  2005/12/13 00:06:31  willuhn
 * @N UmsatzTyp erweitert
 *
 * Revision 1.75  2005/12/12 18:51:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.74  2005/12/08 17:23:51  willuhn
 * @N Datenbank-Update wird jetzt nur noch durchgefuehrt,
 * wenn die aktuelle Datenbank-Version bekannt ist. Sprich:
 * Nichts an der Datenbank aendern, wenn ihr Zustand unklar ist
 *
 * Revision 1.73  2005/12/05 17:20:40  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.72  2005/11/28 11:15:49  willuhn
 * @C database check can be disabled
 *
 * Revision 1.71  2005/11/18 12:13:57  willuhn
 * @B fixed md5 checksum
 *
 * Revision 1.70  2005/11/18 11:58:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.69  2005/11/18 00:43:29  willuhn
 * @B bug 21
 *
 * Revision 1.68  2005/11/14 23:47:21  willuhn
 * @N added first code for umsatz categories
 *
 * Revision 1.67  2005/11/14 13:08:11  willuhn
 * @N Termin-Ueberweisungen
 *
 * Revision 1.66  2005/10/17 15:11:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.65  2005/10/17 14:15:01  willuhn
 * @N FirstStart
 *
 * Revision 1.64  2005/10/17 13:44:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.63  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
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