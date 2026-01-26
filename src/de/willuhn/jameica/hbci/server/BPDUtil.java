/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.hbci.server.DBPropertyUtil.HBCITypedProperties;
import de.willuhn.jameica.hbci.server.DBPropertyUtil.Prefix;
import de.willuhn.jameica.hbci.server.DBPropertyUtil.Update;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;
import de.willuhn.util.TypedProperties;

/**
 * Hilfsklasse zum Durchsuchen der BPD.
 */
public class BPDUtil
{
  private final static long CACHE_MAX_AGE = 7 * 24 * 60 * 60 * 1000L;
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Enum fuer vordefinierte Queries von BPD.
   */
  public enum Query
  {
    /**
     * Query fuer die Suche nach den BPD-Parametern fuer die Bearbeitung von Dauerauftraegen.
     */
    DauerEdit("DauerSEPAEdit","HKCDN"),
    
    /**
     * Query fuer die Suche nach den BPD-Parametern fuer den Abruf der Kontoauszuege.
     */
    Kontoauszug("Kontoauszug","HKEKA"),

    /**
     * Query fuer die Suche nach den BPD-Parametern fuer den Abruf der Kontoauszuege im PDF-Format.
     */
    KontoauszugPdf("KontoauszugPdf","HKEKP"),
    
    /**
     * Query fuer die Suche nach den BPD-Parametern fuer den Abruf der Umsaetze.
     */
    Umsatz("KUmsZeit","HKKAZ"),

    /**
     * Query fuer Abruf der Umsaetze im CAMT-Format.
     */
    UmsatzCamt("KUmsZeitCamt","HKCAZ")
    
    ;
    
    private String query  = null;
    private String gvcode = null;
    
    /**
     * ct.
     * @param query das Query.
     * @param gvcode der Geschaeftsvorfallcode.
     * 
     */
    private Query(String query, String gvcode)
    {
      this.query = query;
      this.gvcode = gvcode;
    }
  }
  
  /**
   * Enthaelt die Support-Informationen fuer einen Geschaeftsvorfall.
   */
  public static class Support
  {
    private Konto konto = null;
    private Query query = null;
    private Integer maxVersion = null;
    private boolean bpdSupport = false;
    private boolean updSupport = false;
    private TypedProperties bpd = null;
    
    /**
     * ct.
     */
    private Support()
    {
    }
    
    /**
     * Liefert das Konto, fuer das die Abfrage durchgefuehrt wurde.
     * @return das Konto, fuer das die Abfrage durchgefuehrt wurde.
     */
    public Konto getKonto()
    {
      return konto;
    }
    
    /**
     * Liefert das Query zu den Support-Informationen.
     * @return das Query zu den Support-Informationen.
     */
    public Query getQuery()
    {
      return query;
    }
    
    /**
     * Liefert die hoechste unterstuetzte Versionsnummer laut BPD. 
     * @return die hoechste unterstuetzte Versionsnummer laut BPD.
     */
    public Integer getMaxVersion()
    {
      return maxVersion;
    }
    
    /**
     * Liefert true, wenn der Geschaeftsvorfall laut BPD unterstuetzt wird.
     * @return true, wenn der Geschaeftsvorfall laut BPD unterstuetzt wird.
     */
    public boolean getBpdSupport()
    {
      return bpdSupport;
    }
    
    /**
     * Liefert true, wenn der Geschaeftsvorfall laut UPD unterstuetzt wird.
     * @return true, wenn der Geschaeftsvorfall laut UPD unterstuetzt wird.
     */
    public boolean getUpdSupport()
    {
      return updSupport;
    }
    
    /**
     * Liefert true, wenn der Geschaeftsvorfall laut BPD und UPD unterstuetzt wird.
     * @return true, wenn der Geschaeftsvorfall laut BPD und UPD unterstuetzt wird.
     */
    public boolean isSupported()
    {
      return this.updSupport && this.bpdSupport;
    }
    
    /**
     * Liefert die BPD-Parameter fuer den Geschaeftsvorfall.
     * @return die BPD-Parameter fuer den Geschaeftsvorfall.
     */
    public TypedProperties getBpd()
    {
      return bpd;
    }
  }
  
  /**
   * Liefert die Support-Informationen zu einem Geschaeftsvorfall fuer ein Konto.
   * @param konto das Konto.
   * @param query das Query.
   * @return die Support-Informationen oder NULL, wenn kein Konto oder kein Query angegeben wurde
   * oder das Konto keine gueltige Kundennummer besitzt.
   */
  public static Support getSupport(final Konto konto, final Query query)
  {
    // Konto und Query angegeben?
    if (konto == null || query == null)
      return null;
    
    try
    {
      // Kundennummer vorhanden?
      String kd = konto.getKundennummer();
      if (kd == null || kd.isBlank())
        return null;
      
      // BLZ vorhanden?
      String blz = konto.getBLZ();
      if (blz == null || blz.isBlank())
        return null;
      
      migrateCache(blz,kd);

      Support support = new Support();
      support.konto = konto;
      support.query = query;

      support.maxVersion = getMaxVersion(konto,query);
      
      // Wenn keine maxVersion ermittelbar ist, dann wird der Job per BPD gar nicht
      // unterstuetzt. Die restlichen Abfragen koennen wir uns dann schenken.
      if (support.maxVersion == null)
        return support;

      // Wird per BPD unterstuetzt - sonst haetten wir keine maxVersion
      support.bpdSupport = true;

      // Die BPD selbst ermitteln
      support.bpd = getBPD(konto,query,support.maxVersion);
      
      // Support laut UPD pruefen
      support.updSupport = getUPDSupport(konto,query);
      
      return support;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine support information of " + query.query,re);
    }
    return null;
  }
  
  /**
   * Liefert die hoechste verfuegbare Version des Geschaeftsvorfalls.
   * @param k das Konto.
   * @param query die Abfrage.
   * @return die Versionsnummer oder NULL, wenn der Geschaeftsvorfall fuer das Konto nicht unterstuetzt wird.
   * @throws RemoteException
   */
  private static Integer getMaxVersion(final Konto k, final Query query) throws RemoteException
  {
    final HBCIDBService service = Settings.getDBService();
    
    // Wir haengen noch unseren Prefix mit BPD und BLZ/Kundennummer vorn dran. Das wurde vom Callback so erfasst
    final String prefix = Prefix.BPD.value() + DBPropertyUtil.SEP + createScope(k.getBLZ(),k.getKundennummer()) + DBPropertyUtil.SEP;
    
    // Wir ermitteln die hoechste Segment-Version des Geschaeftsvorfalls
    final String q = prefix + "Params%." + query.query + "Par%.SegHead.version";
    final String version = (String) service.execute("select max(content) from property where name like ?",new String[] {q},new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        if (rs.next())
          return rs.getString(1);
        
        return null;
      }
    });
    
    if (version == null || !version.matches("^[0-9]{1,2}$"))
      return null;
    
    return Integer.parseInt(version);
  }
  
  /**
   * Liefert true, wenn der Geschaeftsvorfall gemaess UPD unterstuetzt wird.
   * @param k das Konto.
   * @param query die Abfrage.
   * @return true, wenn der Geschaeftsvorfall gemaess UPD unterstuetzt wird.
   * @throws RemoteException
   */
  private static boolean getUPDSupport(final Konto k, final Query query) throws RemoteException
  {
    // Bei den UPD haben wir die Liste der unterstuetzten Geschaeftsvorfaelle konkret fuer die einzelnen Konten
    // Daher koennen wir hier auch nach der Kontonummer/Kundennummer und IBAN suchen.
    // Wir ermitteln erstmal das passende KInfo-Segment.
    
    final HBCIDBService service = Settings.getDBService();

    // Checken, ob wir fuer die UPD ueberhaupt eine Aussage treffen koennen
    final String prefix = Prefix.UPD.value() + DBPropertyUtil.SEP + createScope(k.getBLZ(),k.getKundennummer()) + DBPropertyUtil.SEP;

    {
      final String q = prefix + "UPA.usage";
      final Boolean ignoreUpd = (Boolean) service.execute("select name,content from property where name = ?",new String[] {q},new ResultSetExtractor()
      {
        public Object extract(ResultSet rs) throws RemoteException, SQLException
        {
          if (rs.next())
            return Boolean.valueOf(Objects.equals(rs.getString("content"),"1"));

          return Boolean.FALSE;
        }
      });
      
      // 2020-06-08: In den UPD steht "UPA.usage = 1". Heisst. Anhand der UPD kann keine Aussage darueber getroffen werden, ob der GV unterstuetzt
      // wird. Daher zaehlen dann nur die BPD und updSupport liefert immer true.
      // Noetig fuer die Apo-Bank - siehe https://homebanking-hilfe.de/forum/topic.php?t=24018&page=8
      if (ignoreUpd.booleanValue())
        return true;
    }

    String segment = null;
    
    {
      final String q = prefix + "KInfo%";
      segment = (String) service.execute("select name,content from property where name like ?",new String[] {q},new ResultSetExtractor()
      {
        public Object extract(ResultSet rs) throws RemoteException, SQLException
        {
          while (rs.next())
          {
            String name  = rs.getString("name");
            String value = rs.getString("content");

            if (name == null || name.length() == 0 || value == null || value.length() == 0)
              continue;

            if (name.endsWith(".iban") && value.equals(k.getIban()))
              return name;

            if (name.endsWith(".KTV.number") && value.equals(k.getKontonummer()))
              return name;
          }
          
          return null;
        }
      });
      
      if (segment == null || segment.isBlank())
        return false;
    }
    
    // Den Namen des KInfo-Elements ermitteln
    // Das Format des Segments ist ungefaehr so: "upd.<customernumber>.KInfo_<Nr>....
    // Wir wollen alles bis incl. "KInfo_<Nr>" haben
    int pos    = segment.indexOf(DBPropertyUtil.SEP + "KInfo");
    int offset = segment.substring(pos + 1).indexOf(DBPropertyUtil.SEP);
    segment    = segment.substring(0,pos + offset + 1);

    String q = segment + ".AllowedGV%.code";
    final Boolean support = (Boolean) service.execute("select content from property where name like ? order by content",new String[] {q},new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        while (rs.next())
        {
          String code = rs.getString(1);
          
          // Die restlichen Parameter des Geschaeftsvorfalls interessieren uns
          // erstmal nicht. Es reicht, wenn der Geschaeftsvorfall in der Liste
          // der unterstuetzen auftaucht.
          if (code != null && code.equals(query.gvcode))
            return true;
        }
        
        return false;
      }
    });

    return support.booleanValue();
  }

  /**
   * Liefert die BPD fuer das Konto und den angegebenen Suchfilter.
   * @param k das Konto.
   * @param query die Abfrage.
   * @param version die Segment-Version des Geschaeftsvorfalls.
   * @return Liste der Properties mit den BPD-Parametern.
   * Die Funktion liefert nie NULL sondern hoechstens leere Properties.
   * @throws RemoteException
   */
  private static TypedProperties getBPD(final Konto k, final Query query, final Integer version) throws RemoteException
  {
    final HBCIDBService service = Settings.getDBService();

    // Wir haengen noch unseren Prefix mit BPD und BLZ/Kundennummer vorn dran. Das wurde vom Callback so erfasst
    final String prefix = Prefix.BPD.value() + DBPropertyUtil.SEP + createScope(k.getBLZ(),k.getKundennummer()) + DBPropertyUtil.SEP;
    
    final TypedProperties props = new HBCITypedProperties();
    final String q = prefix + "Params%." + query.query + "Par" + (version != null ? version : "%") + ".Par" + query.query + "%";
    service.execute("select name,content from property where name like ? order by name",new String[] {q},new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        while (rs.next())
        {
          String name  = rs.getString(1);
          String value = rs.getString(2);

          if (name == null || value == null) continue;
          
          // Wir trimmen noch den Prefix aus dem Namen raus
          name = name.substring(name.lastIndexOf('.')+1);
          props.put(name,value);
        }
        return null;
      }
    });

    return props;
  }
  
  /**
   * Aktualisiert den Cache fuer den Passport.
   * @param passport der Passport.
   * @param prefix der Prefix.
   * @return true, wenn der Cache aktualisiert wurde.
   */
  public static boolean updateCache(HBCIPassport passport, Prefix prefix)
  {
    if (passport == null)
      return false;

    try
    {
      final Properties data = prefix == Prefix.BPD ? passport.getBPD() : passport.getUPD();
      final String version  = prefix == Prefix.BPD ? passport.getBPDVersion() : passport.getUPDVersion();
      final String user     = passport.getUserId();
      final String blz      = passport.getBLZ();
      
      if (version == null || version.isBlank() ||
          blz == null || blz.isBlank() ||
          user == null || user.isBlank() || 
          data == null || data.size() == 0)
      {
        Logger.debug("[" + prefix + "] no version, no blz/userid or no data found, skipping update");
        return false;
      }
      
      migrateCache(blz,user);
      
      final String scope = createScope(blz,user);

      // Wir machen das Update nicht jedesmal sondern periodisch. Denn unter
      // Umstaenden koennen hierbei mehrere 100 Datensaetze angelegt werden.
      // Auf jeden Fall aber, wenn wir eine neue Versionsnummer erhalten haben
      final long now = System.currentTimeMillis();
      Version v = null;

      //////////////////////////////////////////////////////////////////////////
      // Expiry-Status
      boolean expired = true;
      
      try
      {
        final long timestamp = Long.parseLong(DBPropertyUtil.get(prefix,scope,null,DBPropertyUtil.KEY_CACHE_UPDATE,"0"));
        expired = (timestamp == 0L || timestamp < (now - CACHE_MAX_AGE));
      }
      catch (Exception e)
      {
        Logger.write(Level.DEBUG,"unable to parse last cache-update date",e);
      }
      //
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Version-Status
      boolean newVersion = false;
      
      try
      {
        v = VersionUtil.getVersion(Settings.getDBService(),prefix.value() + "." + scope);
        
        int nv = Integer.parseInt(version);
        int cv = v.getVersion();
        
        newVersion = (nv > cv);
        
        if (cv < 0 || nv < 0)
          Logger.warn("SUSPECT - " + prefix + " version smaller than zero. new: " + nv + ", current: " + cv);

        // Neue Version uebernehmen
        v.setVersion(nv);
      }
      catch (Exception e)
      {
        Logger.write(Level.DEBUG,"unable to parse version",e);
      }
      //
      //////////////////////////////////////////////////////////////////////////

      Logger.info(prefix + " cache update state [expired: " + expired + ", new version: " + newVersion + "]");
      if (!expired && !newVersion)
        return false;
      
      final BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      final SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
      final ProgressMonitor monitor = session != null ? session.getProgressMonitor() : null;

      if (monitor != null)
        monitor.log(i18n.tr("Aktualisiere " + prefix.name()));
      
      Logger.info("updating " + prefix + " cache");
      
      int count = 1;
      for (String customerId:HBCIProperties.getCustomerIDs(passport))
      {
        Update update = DBPropertyUtil.updateScope(prefix,createScope(blz,customerId),data);
        Logger.info("customer " + count + ": updated " + prefix + "- inserts: " + update.inserts + ", updates: " + update.updates + ", deletions: " + update.deletes);
        if (monitor != null)
          monitor.log(i18n.tr("  Kennung {0} - {1}-Parameter neu: {2}, geändert: {3}, gelöscht: {4}",Integer.toString(count),prefix.name(),Integer.toString(update.inserts),Integer.toString(update.updates),Integer.toString(update.deletes)));
        
        count++;
      }
      
      // Speichern der neuen Versionsnummer
      v.store();
      
      // Datum des letzten Abrufs speichern
      DBPropertyUtil.set(prefix,scope,null,DBPropertyUtil.KEY_CACHE_UPDATE,Long.toString(now));
      return true;
    }
    catch (Exception e)
    {
      Logger.error("error while updating " + prefix + " - will be ignored",e);
      return false;
    }
  }
  
  /**
   * Markiert den Cache als expired.
   * @param passport der Passport.
   * @param prefix der Prefix.
   */
  public static void expireCache(HBCIPassport passport, Prefix prefix)
  {
    final String blz = passport.getBLZ();
    if (blz == null || blz.isBlank())
    {
      Logger.debug("[" + prefix + "] no blz found, skipping cache expiry");
      return;
    }
    
    Set<String> customerIds = HBCIProperties.getCustomerIDs(passport);
    for (String customerId:customerIds)
    {
      try
      {
        migrateCache(blz,customerId);
        Logger.info("expire " + prefix.name() + " cache");
        DBPropertyUtil.set(prefix,createScope(blz,customerId),null,DBPropertyUtil.KEY_CACHE_UPDATE,"0");
      }
      catch (Exception e)
      {
        Logger.error("error while expiring " + prefix + " cache",e);
      }
    }
  }
  
  /**
   * Löscht den Cache.
   * @param passport der Passport.
   */
  public static void deleteCache(HBCIPassport passport)
  {
    final String blz = passport.getBLZ();
    if (blz == null || blz.isBlank())
    {
      Logger.debug("no blz found, skipping bpd/upd cache deletion");
      return;
    }
    
    Set<String> customerIds = HBCIProperties.getCustomerIDs(passport);
    for (String customerId:customerIds)
    {
      final String scope = createScope(blz,customerId);
      try
      {
        Logger.info("deleting bpd/upd cache");
        DBPropertyUtil.deleteScope(DBPropertyUtil.Prefix.BPD,scope);
        DBPropertyUtil.deleteScope(DBPropertyUtil.Prefix.UPD,scope);
        
        // Versionsnummer der Caches loeschen, um das Neubefuellen des Cache zu forcieren
        Logger.info("deleting stored bpd/upd version numbers");
        VersionUtil.delete(Settings.getDBService(),DBPropertyUtil.Prefix.BPD.value() + "." + scope);
        VersionUtil.delete(Settings.getDBService(),DBPropertyUtil.Prefix.UPD.value() + "." + scope);
      }
      catch (Exception e)
      {
        Logger.error("error while clearing BPD/UPD cache",e);
      }
    }
    
    Logger.info("mark upd/bpd caches expired");
    expireCache(passport,Prefix.BPD);
    expireCache(passport,Prefix.UPD);
  }
  
  /**
   * Migriert den Cache auf die neuen Keys mit BLZ.
   * @param String blz.
   * @param String kd
   */
  private static void migrateCache(String blz, String kd)
  {
    try
    {
      if (kd == null || kd.isBlank())
        return;
      
      if (blz == null || blz.isBlank())
        return;

      final String scope = createScope(blz,kd);
      final String migrated = DBPropertyUtil.get(Prefix.BPD,scope,null,DBPropertyUtil.KEY_CACHE_UPDATE,null);
      if (migrated != null)
        return;
      
      // Migrieren
      final HBCIDBService service = Settings.getDBService();
      Logger.info("migrating BPD/UPD cache from key=customer to key=blz.customer");
      int bpdCount = service.executeUpdate("update property set name = REPLACE(name,?,?) where name like ?",Prefix.BPD.value() + DBPropertyUtil.SEP + kd,Prefix.BPD.value() + DBPropertyUtil.SEP + blz + DBPropertyUtil.SEP + kd,Prefix.BPD.value() + DBPropertyUtil.SEP + kd + DBPropertyUtil.SEP + "%");
      int updCount = service.executeUpdate("update property set name = REPLACE(name,?,?) where name like ?",Prefix.UPD.value() + DBPropertyUtil.SEP + kd,Prefix.UPD.value() + DBPropertyUtil.SEP + blz + DBPropertyUtil.SEP + kd,Prefix.UPD.value() + DBPropertyUtil.SEP + kd + DBPropertyUtil.SEP + "%");
      Logger.info("BPD/UPD cache migrated [bpd entries: " + bpdCount + ", upd entries: " + updCount + "]");
    }
    catch (Exception e)
    {
      Logger.error("error while migrating BPD/UPD cache",e);
    }
  }
  
  /**
   * Erzeugt den Scope-Wert.
   * @param blz die BLZ.
   * @param user der User.
   * @return der Scope.
   */
  private static String createScope(String blz, String user)
  {
    return blz.trim() + DBPropertyUtil.SEP + user.trim();
  }
}
