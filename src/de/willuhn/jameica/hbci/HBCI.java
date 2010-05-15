/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCI.java,v $
 * $Revision: 1.120 $
 * $Date: 2010/05/15 19:05:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
import de.willuhn.jameica.hbci.messaging.InfoPointMessageConsumer;
import de.willuhn.jameica.hbci.messaging.QueryAccountCRCMessageConsumer;
import de.willuhn.jameica.hbci.messaging.QueryBanknameMessageConsumer;
import de.willuhn.jameica.hbci.messaging.QueryHBCIVersionMessageConsumer;
import de.willuhn.jameica.hbci.messaging.QueryIBANCRCMessageConsumer;
import de.willuhn.jameica.hbci.messaging.TransferLastschriftMessageConsumer;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.HBCIDBServiceImpl;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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
  public static DateFormat DATEFORMAT       = new CustomDateFormat("dd.MM.yyyy");

  /**
   * Datums-Format ddMMyyyy.
   */
  public static DateFormat FASTDATEFORMAT   = new SimpleDateFormat("ddMMyyyy");

  /**
   * DecimalFormat.
   */
  public static DecimalFormat DECIMALFORMAT = (DecimalFormat) DecimalFormat.getInstance(Application.getConfig().getLocale());

  /**
   * Mapper von HBCI4Java nach jameica Loglevels
   */
  public final static HashMap LOGMAPPING = new HashMap();

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

    Logger.info("register message consumers for query lookups");
    Application.getMessagingFactory().getMessagingQueue("hibiscus.passport.rdh.hbciversion").registerMessageConsumer(new QueryHBCIVersionMessageConsumer());
    Application.getMessagingFactory().getMessagingQueue("hibiscus.query.bankname").registerMessageConsumer(new QueryBanknameMessageConsumer());
    Application.getMessagingFactory().getMessagingQueue("hibiscus.query.accountcrc").registerMessageConsumer(new QueryAccountCRCMessageConsumer());
    Application.getMessagingFactory().getMessagingQueue("hibiscus.query.ibancrc").registerMessageConsumer(new QueryIBANCRCMessageConsumer());
    Application.getMessagingFactory().getMessagingQueue("hibiscus.transfer.lastschrift").registerMessageConsumer(new TransferLastschriftMessageConsumer());
    Application.getMessagingFactory().getMessagingQueue("hibiscus.infopoint").registerMessageConsumer(new InfoPointMessageConsumer());
    
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
   * @see de.willuhn.jameica.plugin.AbstractPlugin#update(de.willuhn.jameica.plugin.Version)
   */
  public void update(final Version oldVersion) throws ApplicationException
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


      this.hbciProps = new Properties();
      
      Version v = getManifest().getVersion(); // client.product.name darf hoechstens 25 Zeichen lang sein
      this.hbciProps.put("client.product.name","HBCI4Java (Hibiscus " + v.getMajor() + "." + v.getMinor() + ")");
      
      // Wir aktivieren das Infopoint-Feature erstmal. Ob wir das Senden
      // dann zulassen entscheiden wir erst, wenn der Callback aufgerufen
      // wird. Wir schicken in dem Fall eine QueryMessage an den Channel
      // "hibiscus.infopoint".
      this.hbciProps.put("infoPoint.enabled","1");

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
      
      // Wenn das Jameica-Loglevel auf DEBUG steht, aktivieren wir per Default das SSL-Logging von HBCI4Java
      if (Logger.getLevel().getValue() == Level.DEBUG.getValue())
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
        		   "besuchen Sie bitte http://hibiscus.berlios.de/doku.php?id=support:fehlermelden";
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


/**********************************************************************
 * $Log: HBCI.java,v $
 * Revision 1.120  2010/05/15 19:05:56  willuhn
 * @N BUGZILLA 865
 *
 * Revision 1.119  2010/03/18 11:37:59  willuhn
 * @N Ausfuehrlichere und hilfreichere Fehlermeldung, wenn Hibiscus-Datenbank defekt ist oder nicht geoeffnet werden konnte.
 *
 * Revision 1.118  2009/10/14 14:29:35  willuhn
 * @N Neuer HBCI4Java-Snapshot (2.5.11) - das SSL-Logging kann nun auch via HBCICallback in das jameica.log geleitet werden (wenn kein log.ssl.filename angegeben ist). Damit kann das Flag "log.ssl.enable" automatisch von Hibiscus aktiviert/deaktiviert werden, wenn das Jameica-Loglevel auf DEBUG oder !DEBUG steht
 *
 * Revision 1.117  2009/10/14 11:11:49  willuhn
 * @N neuer HBCI4Java-Snapshot (2.5.11), der die neuen Parameter "log.ssl.enable" und "log.ssl.filename" mitbringt, um die PIN/TAN-Kommunikation auf HTTP-Ebene zu Debugging-Zwecken mitschneiden zu koennen
 * @N Moeglichkeit, HBCI4Java mit zusaetzlichen eigenen Parametern aus ~/.jameica/hibiscus/hbci4java.properties initialisieren zu koennen
 *
 * Revision 1.116  2009/03/18 22:08:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.115  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 * Revision 1.114  2009/01/04 17:22:14  willuhn
 * @B service.checkConsistency versehentlich entfernt - dadurch wurden keine Datenbank-Updates mehr durchgefuehrt.
 *
 * Revision 1.113  2008/12/31 12:17:37  willuhn
 * @B client.product.name darf hoechstens 25 Zeichen lang sein
 *
 * Revision 1.112  2008/12/30 15:21:40  willuhn
 * @N Umstellung auf neue Versionierung
 *
 * Revision 1.111  2008/11/04 11:55:17  willuhn
 * @N Update auf HBCI4Java 2.5.9
 *
 * Revision 1.110  2008/09/26 15:37:47  willuhn
 * @N Da das Messaging-System inzwischen Consumer solange sammeln kann, bis sie initialisiert ist, besteht kein Bedarf mehr, das explizite Registrieren von Consumern bis zum Versand der SystemMessage.SYSTEM_STARTED zu verzoegern
 *
 * Revision 1.109  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.108  2008/01/25 12:24:05  willuhn
 * @B Messaging-Consumer zu frueh registriert
 *
 * Revision 1.107  2008/01/03 18:20:31  willuhn
 * @N geaendertes Jameica-Loglevel live in HBCI4Java uebernehmen
 **********************************************************************/