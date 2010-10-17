/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/DDVConfigFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/10/17 21:58:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassportDDV;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Passport;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.hbci.passports.ddv.server.CustomReader;
import de.willuhn.jameica.hbci.passports.ddv.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.passports.ddv.server.PassportImpl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Platform;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Eine Factory zum Laden, Erstellen und Aendern von Kartenleser-Konfigurationen.
 */
public class DDVConfigFactory
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static Settings settings = new Settings(DDVConfigFactory.class);
  
  private static List<Reader> presets = null;
  
  /**
   * Liefert eine Liste der vorhandenen Kartenleser-Konfigurationen.
   * @return eine Liste der vorhandenen Kartenleser-Konfigurationen.
   */
  public static List<DDVConfig> getConfigs()
  {
    migrate();
    String[] ids = settings.getList("config",new String[0]);
    List<DDVConfig> configs = new ArrayList<DDVConfig>();
    for (String id:ids)
    {
      configs.add(new DDVConfig(id));
    }
    return configs;
  }
  
  /**
   * Liefert eine Liste mit bekannten Reader-Presets.
   * @return Liste mit bekannten Reader-Presets.
   */
  public static synchronized List<Reader> getReaderPresets()
  {
    if (presets != null)
      return presets;

    presets = new ArrayList<Reader>();
    try
    {
      Logger.info("searching for reader presets");
      Class<Reader>[] found = Application.getClassLoader().getClassFinder().findImplementors(Reader.class);
      for (Class<Reader> r:found)
      {
        try
        {
          presets.add(r.newInstance());
        }
        catch (Exception e)
        {
          Logger.error("unable to load reader preset " + r + " - skipping",e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.error("no reader presets found");
      // Dann nehmen wir wenigstens den "Benutzerdefinierten Leser" in die Liste
      presets.add(new CustomReader());
    }
    return presets;
  }

  /**
   * Speichert die Config.
   * @param config die zu speichernde Config.
   */
  public static void store(DDVConfig config)
  {
    if (config == null)
      return;
    
    // Wir holen uns erstmal die Liste der Konfigurationen
    String[] ids = settings.getList("config",new String[0]);
    List<String> newIds = new ArrayList<String>();

    // Jetzt checken wir, ob wir die ID schon haben
    boolean found = false;
    for (String id:ids)
    {
      if (id == null || id.length() == 0)
        continue; // ignorieren
      found |= id.equals(config.getId());
      newIds.add(id);
    }
    
    if (!found)
      newIds.add(config.getId());
    
    // Speichern der aktualisierten Liste
    settings.setAttribute("config",newIds.toArray(new String[newIds.size()]));
  }
  
  /**
   * Loescht die angegebene Config.
   * @param config die zu loeschende Config.
   */
  public static void delete(DDVConfig config)
  {
    if (config == null)
      return;

    // Loeschen der Einstellungen aus der Config
    config.delete();

    // Aus der Liste der Konfigurationen entfernen
    String[] ids = settings.getList("config",new String[0]);
    List<String> newIds = new ArrayList<String>();

    for (String id:ids)
    {
      if (id == null || id.length() == 0)
        continue; // ignorieren
      if (!id.equals(config.getId())) // wir ueberspringen die zu loeschende
        newIds.add(id);
    }
    
    // Speichern der aktualisierten Liste
    settings.setAttribute("config",newIds.toArray(new String[newIds.size()]));
  }
  
  /**
   * Startet eine automatische Suche nach einem Kartenleser.
   * @param monitor ein Monitor, mit dem der Scan-Fortschritt verfolgt werden kann.
   * @return der gefundene Kartenleser oder NULL wenn keiner gefunden wurde.
   */
  public static DDVConfig scan(ProgressMonitor monitor)
  {
    // wir nehmen hier nicht die Create-Funktion, weil wir
    // sonst (wegen der UUID) mit den Scans die DDVConfig.properties
    // mit Testparametern zumuellen wuerden. Auf diese Weise
    // ist es immer nur die eine.
    DDVConfig temp = new DDVConfig("__scan__");
    
    try
    {
      List<Reader> list = DDVConfigFactory.getReaderPresets();
      int factor = 100 / (list.size() * DDVConfig.PORTS.length);
            
      for (Reader reader:list)
      {
        monitor.setStatusText(i18n.tr("Teste {0}",reader.getName()));

        // Testen, ob der Kartenleser ueberhaupt unterstuetzt wird
        if (!reader.isSupported())
        {
          monitor.log("  " + i18n.tr("überspringe Kartenleser, wird von Ihrem System nicht unterstützt"));
          continue;
        }

        // Checken, ob der CTAPI-Treiber existiert
        String s = reader.getCTAPIDriver();
        if (s == null || s.length() == 0)
        {
          monitor.log("  " + i18n.tr("überspringe Kartenleser, kein CTAPI-Treiber definiert."));
          continue;
        }
        File f = new File(s);
        if (!f.exists())
        {
          monitor.log("  " + i18n.tr("überspringe Kartenleser, CTAPI-Treiber {0} existiert nicht.",f.getAbsolutePath()));
          continue;
        }

        int ctNumber = reader.getCTNumber();
        temp.setCTNumber(ctNumber == -1 ? 0 : ctNumber);
        temp.setEntryIndex(1);
        temp.setReaderPreset(reader);
        temp.setBIO(reader.useBIO());
        temp.setSoftPin(reader.useSoftPin());
        temp.setCTAPIDriver(s);
        temp.setHBCIVersion("210");

        // Wir probieren alle Ports durch
        for (String port:DDVConfig.PORTS)
        {
          monitor.addPercentComplete(factor);
          monitor.log("  " + i18n.tr("Port {0}",port));
                
          temp.setPort(port);

          try
          {
            PassportHandle handle = new PassportHandleImpl(temp);
            handle.open();
            handle.close(); // nein, nicht im finally, denn wenn das Oeffnen
                            // fehlschlaegt, ist nichts zum Schliessen da ;)

            // Passport liess sich oeffnen und schliessen. Dann haben
            // wir den Kartenleser gefunden.
            monitor.log("  " + i18n.tr("gefunden"));
            monitor.setStatusText(i18n.tr("OK. Kartenleser gefunden"));
            monitor.setStatus(ProgressMonitor.STATUS_DONE);
            monitor.setPercentComplete(100);
            
            // Wir kopieren die temporaere Config noch in eine richtige
            DDVConfig config = temp.copy();
            config.setName(i18n.tr("Neue Kartenleser-Konfiguration"));
            return config;
          }
          catch (ApplicationException ae)
          {
            monitor.log("  " + ae.getMessage());
          }
          catch (Exception e)
          {
            monitor.log("  " + i18n.tr("  nicht gefunden"));
          }
        }
      }
      monitor.setStatusText(i18n.tr("Kein Kartenleser gefunden. Bitte manuell konfigurieren"));
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      monitor.setPercentComplete(100);
      return null;
    }
    finally
    {
      // temporaere Config wieder loeschen
      temp.delete();
    }
  }
  
  /**
   * Liefert die zum uebergebenen Konto gehoerende PIN/Tan-Config oder <code>null</code> wenn keine gefunden wurde.
   * @param konto Konto, fuer das die Config gesucht wird.
   * @return Pin/Tan-config des Kontos oder null wenn keine gefunden wurde.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static synchronized DDVConfig findByKonto(Konto konto) throws RemoteException, ApplicationException
  {

    List<DDVConfig> list = getConfigs();
    if (list.size() == 0)
      throw new ApplicationException(i18n.tr("Bitte legen Sie zuerst eine Kartenleser-Konfiguration an"));

    Logger.info("searching config for konto " + konto.getKontonummer() + ", blz: " + konto.getBLZ());

    for (DDVConfig c:list)
    {
      List<Konto> verdrahtet = c.getKonten();
      if (konto != null && verdrahtet != null && verdrahtet.size() > 0)
      {
        for (Konto k:verdrahtet)
        {
          if (konto.equals(k))
          {
            Logger.info("found config via account. name: " + c.getName());
            return c;
          }
        }
      }
    }

    // Wir haben nur eine Config, dann nehmen wir gleich die
    if (list.size() == 1)
    {
      DDVConfig config = (DDVConfig) list.get(0);
      Logger.info("using config : " + config.getName());
      return config;
    }
    
    // Wir haben mehrere zur Auswahl. Lassen wir den User entscheiden.
    SelectConfigDialog d = new SelectConfigDialog(SelectConfigDialog.POSITION_CENTER);
    try
    {
      return (DDVConfig) d.open();
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error while choosing config",e);
      throw new ApplicationException(i18n.tr("Fehler bei der Auswahl der Kartenleser-Konfiguration: {0}",e.getMessage()));
    }
  }

  /**
   * Migriert die alte Kartenleser-Config von Hibiscus 1.11 (da war nur
   * ein Kartenleser pro Installation moeglich) auf das neue Format.
   */
  private static synchronized void migrate()
  {
    // Migration lief bereits
    if (settings.getString("migration.multiconfig",null) != null)
      return;

    // Checken, ob wir eine Kartenleser-Config haben
    Settings oldConfig = new Settings(PassportImpl.class);

    // Wenn ein CTAPI-Treiber angegeben ist, haben wir eine
    if (oldConfig.getString(Passport.CTAPI,null) != null)
    {
      Logger.info("migrating ddv config");
      // Neue Config anlegen
      DDVConfig config = create();
      config.setName("default");
      
      // Wir kopieren die Parameter in die neue Config
      config.setBIO(oldConfig.getBoolean(Passport.USEBIO,false));
      config.setCTAPIDriver(oldConfig.getString(Passport.CTAPI,""));
      config.setCTNumber(oldConfig.getInt(Passport.CTNUMBER,0));
      config.setEntryIndex(oldConfig.getInt(Passport.ENTRYIDX,1));
      config.setHBCIVersion(oldConfig.getString("hbciversion","210"));
      config.setPort(oldConfig.getString(Passport.PORT,DDVConfig.PORTS[0]));
      config.setSoftPin(oldConfig.getBoolean(Passport.SOFTPIN,true));
      
      String s = oldConfig.getString("readerpreset",CustomReader.class.getName());
      if (s != null && s.length() > 0)
      {
        try
        {
          config.setReaderPreset((Reader) Application.getClassLoader().load(s).newInstance());
        }
        catch (Throwable t) {/* ignore */}
      }
      
      store(config); // Neue Config speichern
    }

    // Migration erledigt. Wir loeschen auch noch die alte Config
    String[] keys = oldConfig.getAttributes();
    for (String key:keys)
      oldConfig.setAttribute(key,(String)null);
    settings.setAttribute("migration.multiconfig",HBCI.DATEFORMAT.format(new Date()));
  }
  
  /**
   * Erzeugt eine neue DDV-Config.
   * @return die neue DDV-Config.
   */
  public static DDVConfig create()
  {
    return new DDVConfig(UUID.randomUUID().toString());
  }
  
  /**
   * Erstellt ein Passport-Objekt aus der Config.
   * @param config die Config.
   * @return das Passport-Objekt.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public static HBCIPassportDDV createPassport(DDVConfig config) throws ApplicationException, RemoteException
  {
    if (config == null)
      throw new ApplicationException(i18n.tr("Keine Konfiguration ausgewählt"));

    //////////////////////////////////////////////////////////////////////////
    // JNI-Treiber
    String jni = getJNILib().getAbsolutePath();
    Logger.info("  jni lib: " + jni);
    HBCIUtils.setParam("client.passport.DDV.libname.ddv", jni);
    //
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    // CTAPI-Treiber
    String ctapiDriver = config.getCTAPIDriver();
    if (ctapiDriver == null || ctapiDriver.length() == 0)
      throw new ApplicationException(i18n.tr("Kein CTAPI-Treiber in der Kartenleser-Konfiguration angegeben"));

    File ctapi = new File(ctapiDriver);
    if (!ctapi.exists() || !ctapi.isFile() || !ctapi.canRead())
      throw new ApplicationException(i18n.tr("CTAPI-Treiber-Datei \"{0}\" nicht gefunden oder nicht lesbar",ctapiDriver)); 

    Logger.info("  ctapi driver: " + ctapiDriver);
    HBCIUtils.setParam(Passport.CTAPI, ctapiDriver);
    //
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    // Passport-Verzeichnis
    File f = new File(de.willuhn.jameica.hbci.Settings.getWorkPath() + "/passports/");
    if (!f.exists())
      f.mkdirs();
    HBCIUtils.setParam("client.passport.DDV.path",de.willuhn.jameica.hbci.Settings.getWorkPath() + "/passports/");
    //
    //////////////////////////////////////////////////////////////////////////

    
    String port = Integer.toString(DDVConfig.getPortForName(config.getPort()));
    Logger.info("  port: " + config.getPort() + " [ID: " + port + "]");
    HBCIUtils.setParam(Passport.PORT,port);

    Logger.info("  ctnumber: " + config.getCTNumber());
    HBCIUtils.setParam(Passport.CTNUMBER,Integer.toString(config.getCTNumber()));

    Logger.info("  biometrics: " + config.useBIO());
    HBCIUtils.setParam(Passport.USEBIO, config.useBIO() ? "1" : "0");

    Logger.info("  soft pin: " + config.useSoftPin());
    HBCIUtils.setParam(Passport.SOFTPIN,  config.useSoftPin() ? "1" : "0");

    Logger.info("  entry index: " + config.getEntryIndex());
    HBCIUtils.setParam(Passport.ENTRYIDX,Integer.toString(config.getEntryIndex()));

    return (HBCIPassportDDV) AbstractHBCIPassport.getInstance("DDV");
  }

  
  /**
   * Liefert die zu verwendende JNI-Lib.
   * @return die zu verwendende JNI-Lib.
   * @throws ApplicationException
   */
  private static File getJNILib() throws ApplicationException
  {
    String file = null;
    
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_LINUX:
        file = "libhbci4java-card-linux-32.so";
        break;
        
      case Platform.OS_LINUX_64:
        file = "libhbci4java-card-linux-64.so";
        break;
        
      case Platform.OS_WINDOWS:
        file = "hbci4java-card-win32.dll";
        break;

      case Platform.OS_WINDOWS_64:
        file = "hbci4java-card-win32_x86-64.dll";
        break;
        
      case Platform.OS_MAC:
        file = "libhbci4java-card-mac.jnilib";
        break;

      case Platform.OS_FREEBSD_64:
        file = "libhbci4java-card-freebsd-64.so";
        break;
    }
    
    if (file == null)
      throw new ApplicationException(i18n.tr("Hibiscus unterstützt leider keine Chipkartenleser für Ihr Betriebssystem"));

    File f = new File(de.willuhn.jameica.hbci.Settings.getLibPath(),file);
    if (!f.exists())
      throw new ApplicationException(i18n.tr("Treiber {0} nicht gefunden",f.getAbsolutePath()));

    if (!f.isFile() || !f.canRead())
      throw new ApplicationException(i18n.tr("Treiber {0} nicht lesbar",f.getAbsolutePath()));

    return f;
  }

  
}



/**********************************************************************
 * $Log: DDVConfigFactory.java,v $
 * Revision 1.4  2010/10/17 21:58:56  willuhn
 * @C Aendern der Bankdaten auf der Karte auch dann moeglich, wenn auf dem Slot ungueltige Daten stehen
 *
 * Revision 1.3  2010-09-08 10:16:00  willuhn
 * @N Wenn nur eine DDV-Config vorhanden ist, dann die automatisch nehmen
 *
 * Revision 1.2  2010-09-08 10:08:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 **********************************************************************/