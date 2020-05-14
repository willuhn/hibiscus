/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassportChipcard;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader.Type;
import de.willuhn.jameica.hbci.passports.ddv.server.CustomReader;
import de.willuhn.jameica.hbci.passports.ddv.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Platform;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ClassFinder;
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
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      ClassFinder finder = Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getClassLoader().getClassFinder();
      Class<Reader>[] found = finder.findImplementors(Reader.class);
      for (Class<Reader> r:found)
      {
        try
        {
          presets.add(service.get(r));
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

    // Alphabetisch sortieren
    Collections.sort(presets,new Comparator<Reader>() {
      public int compare(Reader r1, Reader r2)
      {
        return r1.getName().compareTo(r2.getName());
      }
    });
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
   * @throws ApplicationException
   */
  public static void delete(DDVConfig config) throws ApplicationException
  {
    if (config == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu löschende Konfiguration aus"));

    // Loeschen der Einstellungen aus der Config
    config.deleteProperties();

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
   * @param task ueber den Task koennen wir erkennen, ob wir abbrechen sollen.
   * @return der gefundene Kartenleser oder NULL wenn keiner gefunden wurde.
   */
  public static DDVConfig scan(ProgressMonitor monitor, BackgroundTask task)
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
        if (task.isInterrupted())
          throw new OperationCanceledException();
        
        monitor.setStatusText(i18n.tr("Teste {0}",reader.getName()));

        // Testen, ob der Kartenleser ueberhaupt unterstuetzt wird
        if (!reader.isSupported())
        {
          monitor.log("  " + i18n.tr("überspringe Kartenleser, wird von Ihrem System nicht unterstützt"));
          continue;
        }

        // Checken, ob der CTAPI-Treiber existiert.
        String s = StringUtils.trimToNull(reader.getCTAPIDriver());
        Type type = reader.getType();
        if (type.isCTAPI())
        {
          if (s == null)
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
        }

        int ctNumber = reader.getCTNumber();
        temp.setCTNumber(ctNumber == -1 ? 0 : ctNumber);
        temp.setEntryIndex(1);
        temp.setReaderPreset(reader);
        temp.setSoftPin(reader.useSoftPin());
        temp.setCTAPIDriver(s);
        temp.setHBCIVersion(reader.getDefaultHBCIVersion());

        // PC/SC-Kartenleser suchen
        if (type.isPCSC())
        {
          try
          {
            CardTerminals terminals = TerminalFactory.getDefault().terminals();
            if (terminals != null)
            {
              List<CardTerminal> l = terminals.list();
              // Eigentlich koennen wir hier pauschal den ersten gefundenen nehmen
              if (l != null && l.size() > 0)
              {
                CardTerminal terminal = l.get(0);
                String name = terminal.getName();
                temp.setPCSCName(name);
                
                if (testConfig(monitor,temp))
                {
                  // Wir kopieren die temporaere Config noch in eine richtige
                  DDVConfig config = temp.copy();
                  config.setName(name);
                  return config;
                }
              }
            }
          }
          catch (Exception e)
          {
            Logger.error("unable to create ddv config",e);
          }
          finally
          {
            temp.setPCSCName(null); // muessen wir wieder zuruecksetzen
          }
          continue;
        }

        // Wir probieren alle Ports durch
        for (String port:DDVConfig.PORTS)
        {
          if (task.isInterrupted())
            throw new OperationCanceledException();
          
          monitor.addPercentComplete(factor);
          monitor.log("  " + i18n.tr("Port {0}",port));
                
          temp.setPort(port);

          if (testConfig(monitor,temp))
          {
            // Wir kopieren die temporaere Config noch in eine richtige
            DDVConfig config = temp.copy();
            config.setName(i18n.tr("Neue Kartenleser-Konfiguration"));
            return config;
          }
        }
      }
      monitor.setStatusText(i18n.tr("Kein Kartenleser gefunden. Bitte manuell konfigurieren"));
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      monitor.setPercentComplete(100);
      return null;
    }
    catch (OperationCanceledException oce)
    {
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
      monitor.setStatusText(i18n.tr("Abgebrochen"));
      monitor.setPercentComplete(100);
      return null;
    }
    finally
    {
      // temporaere Config wieder loeschen
      try
      {
        temp.delete();
      }
      catch (Exception e)
      {
        Logger.error("unable to delete temp-config",e);
      }
    }
  }
  
  /**
   * Testet eine Kartenleser-Konfiguration.
   * @param monitor der Progress-Monitor.
   * @param config die Config.
   * @return true, wenn sie erfolgreich getestet wurde.
   */
  private static boolean testConfig(ProgressMonitor monitor, DDVConfig config)
  {
    PassportHandle handle = null;
    try
    {
      handle = new PassportHandleImpl(config);
      handle.open();
      handle.close();
      handle = null;

      // Passport liess sich oeffnen und schliessen. Dann haben wir den Kartenleser gefunden.
      monitor.log("  " + i18n.tr("gefunden"));
      monitor.setStatusText(i18n.tr("OK. Kartenleser gefunden"));
      monitor.setStatus(ProgressMonitor.STATUS_DONE);
      monitor.setPercentComplete(100);
      return true;
    }
    catch (ApplicationException ae)
    {
      monitor.log("  " + ae.getMessage());
    }
    catch (Exception e)
    {
      monitor.log("  " + i18n.tr("  nicht gefunden"));
      Logger.info("exception: " + e.getMessage());
      Logger.write(Level.DEBUG,"stacktrace for debugging purpose",e);
    }
    finally
    {
      if (handle != null)
      {
        try
        {
          handle.close();
        }
        catch (Throwable t)
        {
          Logger.error("closing of passport handle failed",t);
        }
      }
    }
    
    // Wir warten noch kurz. Fuer den Fall, dass in den Layern darunter irgendwas
    // asynchron stattfindet, koennte es sonst eventuell passieren, dass wir
    // versuchen, die Verbindung neu zu oeffnen, bevor die andere sauber geschlossen
    // wurde. Hatte in einem Log "SCARD_E_SHARING_VIOLATION" gesehen. Konnte nicht
    // beurteilen, ob der Scan zu schnell probiert hat oder ob das close() im finally
    // fehlte. Schaden koennen ein paar Millisekunden Wartezeit aber nicht.
    try
    {
      Thread.sleep(250L);
    } catch (Exception e) { /* ignore */}
    
    return false;
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
  public static HBCIPassportChipcard createPassport(DDVConfig config) throws ApplicationException, RemoteException
  {
    if (config == null)
      throw new ApplicationException(i18n.tr("Keine Konfiguration ausgewählt"));

    Type type = config.getReaderPreset().getType();
    
    if (type.isPCSC())
    {
      String pcscName = config.getPCSCName();
      Logger.info("  pcsc name: " + pcscName);
      if (StringUtils.trimToNull(pcscName) != null)
      {
        HBCIUtils.setParam(PassportParameter.get(type,PassportParameter.NAME),pcscName);
      }
    }
    else
    {
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
      HBCIUtils.setParam(PassportParameter.get(type,PassportParameter.CTAPI), ctapiDriver);
      //
      //////////////////////////////////////////////////////////////////////////
    }

    //////////////////////////////////////////////////////////////////////////
    // Passport-Verzeichnis
    File f = new File(de.willuhn.jameica.hbci.Settings.getWorkPath() + "/passports/");
    if (!f.exists())
      f.mkdirs();
    
    String headerName = type == Type.RDH_PCSC ? "RSA" : "DDV"; // siehe HBCIPassport[RSA/DDV], Konstruktor, "setParamHeader"
    HBCIUtils.setParam("client.passport." + headerName + ".path",de.willuhn.jameica.hbci.Settings.getWorkPath() + "/passports/");
    //
    //////////////////////////////////////////////////////////////////////////


    if (type.isCTAPI())
    {
      String port = Integer.toString(DDVConfig.getPortForName(config.getPort()));
      Logger.info("  port: " + config.getPort() + " [ID: " + port + "]");
      HBCIUtils.setParam(PassportParameter.get(type,PassportParameter.PORT), port);

      Logger.info("  ctnumber: " + config.getCTNumber());
      HBCIUtils.setParam(PassportParameter.get(type,PassportParameter.CTNUMBER), Integer.toString(config.getCTNumber()));
    }
    
    Logger.info("  soft pin: " + config.useSoftPin());
    HBCIUtils.setParam(PassportParameter.get(type,PassportParameter.SOFTPIN), config.useSoftPin() ? "1" : "0");

    Logger.info("  entry index: " + config.getEntryIndex());
    HBCIUtils.setParam(PassportParameter.get(type,PassportParameter.ENTRYIDX), Integer.toString(config.getEntryIndex()));

    Logger.info("  hbci version: " + config.getHBCIVersion());

    String id = type.getIdentifier();
    Logger.info("  passport type: " + id);
    HBCIPassportChipcard passport = (HBCIPassportChipcard) AbstractHBCIPassport.getInstance(id);
    
    Logger.info("  host: " + passport.getHost());
    Logger.info("  blz: " + passport.getBLZ());

    return passport;
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
        String arch = System.getProperty("os.arch");
        if (arch != null && arch.contains("64"))
          file = "libhbci4java-card-mac-os-x-10.6.jnilib"; // BUGZILLA 965
        else
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
 * Revision 1.9  2011/09/06 11:54:25  willuhn
 * @C JavaReader in PCSCReader umbenannt - die PIN-Eingabe fehlt noch
 *
 * Revision 1.8  2011-09-01 12:16:08  willuhn
 * @N Kartenleser-Suche kann jetzt abgebrochen werden
 * @N Erster Code fuer javax.smartcardio basierend auf dem OCF-Code aus HBCI4Java 2.5.8
 *
 * Revision 1.7  2011-09-01 09:40:53  willuhn
 * @R Biometrie-Support bei Kartenlesern entfernt - wurde nie benutzt
 *
 * Revision 1.6  2011-06-17 08:49:19  willuhn
 * @N Contextmenu im Tree mit den Bank-Zugaengen
 * @N Loeschen von Bank-Zugaengen direkt im Tree
 *
 * Revision 1.5  2011-02-06 23:34:21  willuhn
 * @N BUGZILLA 965
 *
 * Revision 1.4  2010-10-17 21:58:56  willuhn
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