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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Properties;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.hbci.gui.CustomDateFormat;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.HBCIDBServiceImpl;
import de.willuhn.jameica.messaging.BootMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.CustomDecimalFormat;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Basis-Plugin-Klasse von Hibiscus.
 */
public class HBCI extends AbstractPlugin
{
  /**
   * Flag, mit dem das automatische Berechnen der IBAN aktiviert werden kann.
   */
  public final static boolean COMPLETE_IBAN = Boolean.FALSE.booleanValue(); // hab ich nur so umstaendlich geschrieben, damit die if's von Eclipse nicht als "dead code" erkannt werden
  
  /**
   * Datums-Format dd.MM.yyyy HH:mm.
   */
  public static DateFormat LONGDATEFORMAT   = new SimpleDateFormat("dd.MM.yyyy HH:mm");
  
  /**
   * Datums-Format dd.MM.yyyy HH:mm:ss.
   */
  public static DateFormat XTRALONGDATEFORMAT   = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  /**
   * Datums-Format dd.MM.yyyy.
   */
  public static DateFormat DATEFORMAT       = new CustomDateFormat("dd.MM.yyyy");

  /**
   * Datums-Format dd.MM.
   */
  public static DateFormat SHORTDATEFORMAT  = new CustomDateFormat("dd.MM.");

  /**
   * Datums-Format ddMMyyyy.
   */
  public static DateFormat FASTDATEFORMAT   = new SimpleDateFormat("ddMMyyyy");

  /**
   * DecimalFormat.
   */
  public static DecimalFormat DECIMALFORMAT = new CustomDecimalFormat();

  /**
   * Mapper von HBCI4Java nach jameica Loglevels
   */
  public final static HashMap LOGMAPPING = new HashMap();
  
  private final static String HBCI4JAVA_VERSION = "3.1.74";

  private HBCICallback callback = null;
  private Properties hbciProps  = null;
  
  /**
   * ct.
   */
  public HBCI()
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#init()
   */
  public void init() throws ApplicationException
  {
		Logger.info("starting init process for hibiscus");

    //  BUGZILLA 101 http://www.willuhn.de/bugzilla/show_bug.cgi?id=101
    DECIMALFORMAT.setGroupingUsed(Settings.getDecimalGrouping());

    LOGMAPPING.put(Level.ERROR, Integer.valueOf(HBCIUtils.LOG_ERR));
    LOGMAPPING.put(Level.WARN,  Integer.valueOf(HBCIUtils.LOG_WARN));
    LOGMAPPING.put(Level.INFO,  Integer.valueOf(HBCIUtils.LOG_INFO));
    LOGMAPPING.put(Level.DEBUG, Integer.valueOf(HBCIUtils.LOG_DEBUG));
    LOGMAPPING.put(Level.TRACE, Integer.valueOf(HBCIUtils.LOG_DEBUG2));

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
   * @see de.willuhn.jameica.plugin.AbstractPlugin#uninstall(boolean)
   */
  public void uninstall(boolean deleteUserData) throws ApplicationException
  {
    if (!deleteUserData)
      return;
    
    try
    {
      Logger.info("deleting hibiscus wallet");
      Settings.getWallet().deleteAll(null);
    }
    catch (Exception e)
    {
      Logger.error("unable to delete wallet",e);
    }
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
      
      final BeanService service = Application.getBootLoader().getBootable(BeanService.class);

      if (callbackClass != null && callbackClass.length() > 0)
      {
        try
        {
          Class c = Class.forName(callbackClass);
          this.callback = (HBCICallback) service.get(c);
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
          this.callback = service.get(HBCICallbackSWT.class);
      }
      //////////////////////////////////


      this.hbciProps = new Properties();
      
      Version v = getManifest().getVersion(); // client.product.name darf hoechstens 25 Zeichen lang sein
      this.hbciProps.put("client.product.name","A44C2953982351617D475443E"); // Das ist die offizielle Produktkennung von Hibiscus - siehe http://hbci-zka.de/register/register_faq.htm
      this.hbciProps.put("client.product.version",v.getMajor() + "." + v.getMinor()); // Maximal 5 Zeichen
      
      // Default-Passport-Format Legacy
      this.hbciProps.put("passport.format",                    "LegacyFormat");
      
      // Die Passports, die im Fehlerfall einfach neu erstellt werden koennen, konvetieren wir auf AESFormat
      this.hbciProps.put("passport.format.HBCIPassportPinTan", "AESFormat");
      this.hbciProps.put("passport.format.HBCIPassportDDV",    "AESFormat");
      this.hbciProps.put("passport.format.HBCIPassportDDVPCSC","AESFormat");
      this.hbciProps.put("passport.format.HBCIPassportDDVRSA", "AESFormat");
      this.hbciProps.put("passport.format.HBCIPassportRAH10",  "AESFormat");

      // Die Schluesseldateien lassen wir mal noch auf dem Legacy-Format. Denn wenn wir da einen Fehler haben, geht die kaputt
      this.hbciProps.put("passport.format.HBCIPassportRDHNew", "LegacyFormat");
      
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
      this.hbciProps.put("log.loglevel.default",""+logLevel);
      this.hbciProps.put("client.errors.ignoreWrongDataSyntaxErrors","yes"); // BUGZILLA 1129
      
      // Wenn das Jameica-Loglevel auf DEBUG oder hoeher steht, aktivieren wir per Default das SSL-Logging von HBCI4Java
      if (Logger.isLogging(Level.DEBUG))
        this.hbciProps.put("log.ssl.enable","1");
        
      //////////////////////////////////
      
      //////////////////////////////////
      // Generische Addon-Parameter
      File addonprops = new File(getResources().getWorkPath(),"hbci4java.properties");
      if (addonprops.exists())
      {
        InputStream is = null;
        try
        {
          is = new BufferedInputStream(new FileInputStream(addonprops));
          Properties p = new Properties();
          p.load(is);
          
          if (p.size() > 0)
          {
            Logger.info("applying hbci4java properties from " + addonprops + ": "+ p.toString());
            this.hbciProps.putAll(p);
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to load " + addonprops,e);
        }
        finally
        {
          if (is != null)
          {
            try {
              is.close();
            } catch (Exception e) {
              Logger.error("error while closing " + addonprops,e);
            }
          }
        }
      }
      //////////////////////////////////

      HBCIUtils.init(this.hbciProps,this.callback);
      
      final String version = HBCIUtils.version();
      if (version != null && !HBCI4JAVA_VERSION.equals(version))
      {
        final String s = "Die Version der Systembibliothek HBCI4Java \"{0}\" stimmt nicht mit der erwarteten Version \"{1}\" ¸berein. " +
                         "Das wird zu unerwarteten Fehlern f¸hren. Bitte kopiere eine neuere Version von Hibiscus nicht ¸ber " +
                         "die vorherige dr¸ber. Hierbei kˆnnen Fragmente der vorherigen Version erhalten bleiben. Installiere Updates " +
                         "stattdessen per \"Datei->Einstellungen->Plugins\".";
        Application.getMessagingFactory().getMessagingQueue("jameica.boot").queueMessage(new BootMessage(getResources().getI18N().tr(s,version,HBCI4JAVA_VERSION)));
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to init HBCI4Java",e);
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
   * Liefert die Properties, mit denen HBCI4Java initialisiert wurde.
   * @return die Properties, mit denen HBCI4Java initialisiert wurde.
   */
  public Properties getHBCIPropetries()
  {
    return this.hbciProps;
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
      Logger.error("unable to init db service",e);
      I18N i18n = getResources().getI18N();
      String msg = i18n.tr("Hibiscus-Datenbank konnte nicht initialisiert werden.\n\n{0} ", e.getMessage());
      
      // Wenn wir die H2-DB verwenden, koennte es sich um eine korrupte Datenbank handeln
      String driver = HBCIDBService.SETTINGS.getString("database.driver",null);
      if (driver != null && driver.equals(DBSupportH2Impl.class.getName()))
      {
        msg += "\n\nMˆglicherweise ist die Hibiscus-Datenbank defekt. Klicken Sie bitte auf \"Datei>Backups verwalten\", " +
        		   "w‰hlen Sie das Backup vom letzten Tag aus, an dem der Fehler noch nicht auftrat und klicken " +
        		   "Sie anschlieﬂend auf \"Ausgew‰hltes Backup wiederherstellen...\". Beim n‰chsten Start von Hibiscus " +
        		   "wird das Backup automatisch wiederhergestellt. Sollte sich das Problem hierdurch nicht beheben lassen, " +
        		   "besuchen Sie bitte http://www.willuhn.de/wiki/doku.php?id=support:fehlermelden";
      }

      throw new ApplicationException(msg,e);
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
