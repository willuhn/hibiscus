/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.hbci4java.hbci.manager.BankInfo;
import org.hbci4java.hbci.manager.HBCIUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Service, mit der die URLs von PIN/TAN-Bankzugängen auf Korrektheit geprüft und bei Bedarf automatisch korrigiert werden können.
 */
@Lifecycle(Type.CONTEXT)
public class PinTanMigrationService
{
  private List<VerificationEntry> entries = new ArrayList<>();
  
  /**
   * Initialisiert den Service.
   */
  @PostConstruct
  private void init()
  {
    this.refresh();
  }
  
  /**
   * Liefert eine Liste der zu migrierenden Bankzugänge.
   * @return die Liste der zu migrierenden Bankzugänge.
   */
  public List<VerificationEntry> getConfigs()
  {
    return this.entries;
  }
  
  /**
   * Aktualisiert die Liste der zu migrierenden Bankzugänge.
   */
  public void refresh()
  {
    this.entries.clear();
    
    final long started = System.currentTimeMillis();
    
    try
    {
      final GenericIterator<PinTanConfig> it = PinTanConfigFactory.getConfigs();
      
      while (it.hasNext())
      {
        final PinTanConfig conf = it.next();
        
        try
        {
          final String url = prepareUrl(conf.getURL());
          final String blz = conf.getBLZ();
          if (StringUtils.trimToNull(url) == null || StringUtils.trimToNull(blz) == null)
          {
            Logger.warn("missing url/blz in pin/tan config - skipping " + conf.getFilename());
            continue;
          }
          
          // Checken, wie die URL lauten sollte
          final BankInfo info = HBCIUtils.getBankInfo(blz);
          if (info == null)
            continue;
          
          final String newUrl = prepareUrl(info.getPinTanAddress());
          if (StringUtils.trimToNull(newUrl) == null)
            continue; // Wir haben keine URL für die Bank
          
          if (!url.equals(newUrl))
          {
            final VerificationEntry e = new VerificationEntry();
            e.config = conf;
            e.newUrl = newUrl;
            this.entries.add(e);
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to load passport",e);
        }
      }
    }
    catch (Exception e2)
    {
      Logger.error("unable to load passport migration list",e2);
    }
    
    final long used = System.currentTimeMillis() - started;
    Logger.write(entries.isEmpty() ? Level.DEBUG : Level.INFO,"found " + entries.size() + " pin/tan configs to be migrated in " + used + " ms");
  }
  
  /**
   * Migriert die angegebene Liste der Einträge auf die neue URL.
   * @param list die Liste der Einträge.
   * @return die Anzahl der Einträge, die aktualisiert werden konnten.
   */
  public int migrate(List<VerificationEntry> list)
  {
    int count = 0;
    if (list == null || list.isEmpty())
      return count;
    
    Logger.info("BEGIN migration of " + list.size() + " pin/tan passports");
    for (VerificationEntry e:list)
    {
      try
      {
        final PinTanConfig conf = e.getConfig();
        Logger.info(String.format("migrating pin/tan passport '%s' from url '%s' to '%s'",conf.getDescription(),conf.getURL(),e.getNewUrl()));
        conf.setURL(e.getNewUrl());
        PinTanConfigFactory.store(conf);
        count++;
      }
      catch (Exception ex)
      {
        Logger.error("unable to migrate pin/tan passport",ex);
      }
    }
    Logger.info("END migration of pin/tan passports: " + count);
    
    refresh();
    return count;
  }
  
  /**
   * Bereitet die URL für den toleranten Vergleich vor.
   * "https://" am Anfang und "/" am Ende werden ignoriert.
   * @param url die URL.
   * @return die aufbereitete URL.
   */
  private String prepareUrl(String url)
  {
    if (url == null)
      return url;
    
    if (url.startsWith("https://"))
      url = url.substring(8);
    
    return url;
  }
  
  /**
   * Ein Bankzugang, bei dem die URL abweicht.
   */
  public static class VerificationEntry
  {
    private PinTanConfig config = null;
    private String newUrl = null;
    
    /**
     * Liefert die Config.
     * @return config die Config.
     */
    public PinTanConfig getConfig()
    {
      return config;
    }
    
    /**
     * Liefert die bisherige URL.
     * @return die bisherige URL.
     */
    public String getOldUrl()
    {
      try
      {
        return this.getConfig().getURL();
      }
      catch (Exception e)
      {
        Logger.error("unable to get url",e);
        return null;
      }
    }
    
    /**
     * Liefert die neue URL.
     * @return die neue URL.
     */
    public String getNewUrl()
    {
      return newUrl;
    }
  }
}
