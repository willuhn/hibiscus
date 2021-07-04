/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Set;

import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.PassportLoader;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.passports.pintan.server.PinTanConfigImpl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.DBPropertyUtil;
import de.willuhn.jameica.hbci.server.VersionUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Factory zum Laden, Erzeugen und Finden von PIN/TAN-Konfigurationen. 
 * @author willuhn
 */
public class PinTanConfigFactory
{
  private final static Settings settings = new Settings(PinTanConfigFactory.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Erstellt eine neue PIN/Tan-Config.
   * @return neue Config.
   * @throws Exception
   */
  public static synchronized PinTanConfig create() throws Exception
  {
    File f = createFilename();
    PassportLoader l = load(f);
    return new PinTanConfigImpl(l,f);
  }

  /**
   * Speichert die Konfiguration.
   * @param config
   * @throws Exception
   */
  public static synchronized void store(PinTanConfig config) throws Exception
  {
    if (config == null || config.getID() == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu speichernde Konfiguration aus"));

    Logger.info("storing pin/tan config");

    String[] existing = settings.getList("config",new String[0]);

    boolean found = false;
    if (existing != null && existing.length > 0)
    {
      for (String configID : existing)
      {
        if (configID.equals(config.getID()))
        {
          Logger.info("updating existing config");
          found = true;
          break;
        }
      }
    }

    if (!found)
    {
      Logger.info("adding new pin/tan config");
      String[] newList = new String[existing.length+1];
      System.arraycopy(existing,0,newList,0,existing.length);
      newList[existing.length] = config.getID();
      settings.setAttribute("config",newList);
    }

    HBCIPassport p = config.getPassport();
    Logger.info("saving passport config");
    p.saveChanges();
  }

  /**
   * Loescht die genannte Config.
   * @param config die zu loeschende Config.
   * @throws ApplicationException
   */
  public static synchronized void delete(PinTanConfig config) throws ApplicationException
  {
    try
    {
      if (config == null || config.getID() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu löschende Konfiguration aus"));

      String[] existing = settings.getList("config",new String[0]);

      if (existing.length == 0)
      {
        Logger.info("no configs found, nothing to delete");
        return;
      }

      Logger.debug("number of configs: " + existing.length);
      ArrayList<String> newList = new ArrayList<>();
      String id = config.getID();

      for (String existingID : existing) {
        if (id.equals(existingID)) {
          Logger.info("deleting config for file " + id);
          continue;
        }
        newList.add(existingID);
      }
      
      Logger.debug("new number of configs: " + newList.size());
      settings.setAttribute("config",(String[]) newList.toArray(new String[newList.size()]));
      
      // Jetzt noch die Datei loeschen
      File f = new File(config.getFilename());
      if (f.exists() && f.isFile() && f.canWrite())
      {
        f.delete();
        Logger.info("deleted passport file " + f);
      }
      
      // sowie die Caches
      Logger.info("deleting BPD/UPD caches");
      try
      {
        HBCIPassport passport = config.getPassport();
        Set<String> customerIds = HBCIProperties.getCustomerIDs(passport);
        for (String customerId:customerIds)
        {
          try
          {
            DBPropertyUtil.deleteScope(DBPropertyUtil.Prefix.BPD,customerId);
            DBPropertyUtil.deleteScope(DBPropertyUtil.Prefix.UPD,customerId);
          }
          catch (Exception e)
          {
            // Auch wenn das fehlschlaegt, soll der Rest trotzdem durchgefuehrt werden
            Logger.error("error while clearing BPD/UPD cache",e);
          }
        }
        
        // Versionsnummer der Caches loeschen, um das Neubefuellen des Cache zu forcieren
        String user = passport.getUserId();
        if (user != null && user.length() > 0)
        {
          Logger.info("deleting BPD/UPD cache versions");
          VersionUtil.delete(de.willuhn.jameica.hbci.Settings.getDBService(),DBPropertyUtil.Prefix.BPD.value() + "." + user);
          VersionUtil.delete(de.willuhn.jameica.hbci.Settings.getDBService(),DBPropertyUtil.Prefix.UPD.value() + "." + user);
        }
      }
      catch (Exception e)
      {
        // Das kann passieren, wenn der Passport unvollstaendig ist
      }
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("unable to delete pin/tan config",e);
      throw new ApplicationException(i18n.tr("Löschen fehlgeschlagen: {0}",e.getMessage()));
    }
  }

  /**
   * Erzeugt ein Passport-Objekt basierend auf der uebergebenen Config.
   * @param f das HBCI4Java-Config-File.
   * @return Passport. Wir liefern hier ein Future zurueck, damit das eigentliche Laden erst bei Bedarf stattfinden muss.
   */
  public static PassportLoader load(final File f)
  {
    HBCIUtils.setParam("client.passport.default","PinTan");
    HBCIUtils.setParam("client.passport.PinTan.init","1");

    HBCIUtils.setParam("client.passport.PinTan.checkcert","1");

    return new PassportLoader() {

      private HBCIPassport p = null;

      @Override
      public void reload()
      {
        try
        {
          if (this.p != null)
            this.p.close();
        }
        catch (Exception e)
        {
          Logger.error("error while closing passport",e);
        }
        finally
        {
          this.p = null;
        }
      }
      
      @Override
      public HBCIPassport load()
      {
        if (this.p == null)
          this.p = AbstractHBCIPassport.getInstance("PinTan",f);
        return this.p;
      }
    };
  }

  /**
   * Liefert die zum uebergebenen Konto gehoerende PIN/Tan-Config oder <code>null</code> wenn keine gefunden wurde.
   * @param konto Konto, fuer das die Config gesucht wird.
   * @return Pin/Tan-config des Kontos oder null wenn keine gefunden wurde.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static synchronized PinTanConfig findByKonto(Konto konto) throws RemoteException, ApplicationException
  {

    GenericIterator i = getConfigs();
    if (!i.hasNext())
      throw new ApplicationException(i18n.tr("Bitte legen Sie zuerst eine PIN/TAN-Konfiguration an"));

    Logger.info("searching config for konto " + konto.getKontonummer() + ", blz: " + konto.getBLZ());
    PinTanConfig config = null;

    ArrayList found = new ArrayList();
    while (i.hasNext())
    {
      config = (PinTanConfig) i.next();

      // BUGZILLA 173
      Konto[] verdrahtet = config.getKonten();
      if (konto != null && verdrahtet != null && verdrahtet.length > 0)
      {
        for (Konto k : verdrahtet)
        {
          if (konto.equals(k))
          {
            Logger.info("found config via account. url: " + config.getURL());
            return config;
          }
        }
      }
      String blz = config.getBLZ();
      if (blz != null && blz.equals(konto.getBLZ()))
      {
        Logger.info("found config. url: " + config.getURL());
        found.add(config);
      }
    }

    if (found.size() == 1)
    {
      config = (PinTanConfig) found.get(0);
      Logger.info("using config. url: " + config.getURL());
      return config;
    }


    String text = i18n.tr("Mehrere zutreffende Konfigurationen gefunden. Bitte wählen Sie eine manuell aus.");
    
    if (found.size() == 0)
    {
      Logger.warn("no config found for this konto. Asking user");
      text = i18n.tr("Keine zutreffende Konfigurationen gefunden. Bitte wählen Sie eine manuell aus.");
    }
    
    // Wir haben mehrere zur Auswahl. Lassen wir den User entscheiden.
    GenericIterator list = PseudoIterator.fromArray((PinTanConfig[]) found.toArray(new PinTanConfig[found.size()]));
    SelectConfigDialog d = new SelectConfigDialog(SelectConfigDialog.POSITION_CENTER,list);
    d.setText(text);
    try
    {
      config = (PinTanConfig) d.open();
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error while choosing config",e);
      throw new ApplicationException(i18n.tr("Fehler bei der Auswahl der PIN/TAN-Konfiguration"));
    }
    return config;
  }

  /**
   * Liefert die Liste der existierenden Pin/Tan-Konfigurationen.
   * @return Liste der Konfigurationen.
   * @throws RemoteException
   */
  public static synchronized GenericIterator getConfigs() throws RemoteException
  {
    String[] found = settings.getList("config",new String[0]);

    ArrayList<PinTanConfig> configs = new ArrayList<>();
    for (String configPathAsString : found)
    {
      if (configPathAsString != null && configPathAsString.length() > 0)
      {
        File f = toAbsolutePath(configPathAsString);
        if (!f.exists())
          continue;
        
        try
        {
          PassportLoader l = load(f);
          configs.add(new PinTanConfigImpl(l,f));
        }
        catch (Exception e)
        {
          Logger.error("unable to load config " + f.getAbsolutePath() + " - skipping",e);
        }
      }
    }
    return PseudoIterator.fromArray(configs.toArray(new PinTanConfig[configs.size()]));
  }

  /**
   * Erzeugt eine neue Config-Datei.
   * @return Passport-File.
   * @throws ApplicationException
   */
  public static File createFilename() throws ApplicationException
  {
    String wp = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath();
    File f = new File(wp + File.separator + "passports",System.currentTimeMillis() + ".pt");
    
    int retry = 0;
    while (f.exists())
    {
      if (retry > 20)
        throw new ApplicationException(i18n.tr("Configdatei {0} existiert bereits",f.getAbsolutePath()));
      f = new File(wp + File.separator + "passports",System.currentTimeMillis() + "-" + (++retry) + ".pt");
    }
    return f;
  }
  
  /**
   * Macht aus dem Dateinamen einer Passport-Datei eine absolute Pfadangabe.
   * Die Funktion erkennt selbst, ob es sich bereits um eine absolute Pfadangabe
   * handelt und liefert den Pfad in dem Fall unveraendert zurueck.
   * BUGZILLA 276
   * @param filename Dateiname.
   * @return Absolute Pfadangabe
   */
  public static File toAbsolutePath(String filename)
  {
    File f = new File(filename);
    if (f.canRead() && f.isFile()) // Ist bereits eine absolute Pfadangabe
      return f;
    
    String wp = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath();
    return new File(wp + File.separator + "passports",filename);
  }
  
  /**
   * Macht aus der Pfadangabe eine relative Angabe - enthaelt dann also nur noch den Dateinamen.
   * Die Funktion erkennt selbst, ob es sich bereits um einen Dateinamen ohne
   * Pfadangabe handelt und gibt den Namen in dem Fall unveraendert zurueck.
   * BUGZILLA 276
   * @param file Pfadangabe.
   * @return Dateiname.
   */
  public static String toRelativePath(String file)
  {
    return new File(file).getName();
  }
  
}
