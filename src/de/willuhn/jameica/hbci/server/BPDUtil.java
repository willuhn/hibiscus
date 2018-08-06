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
import java.util.Enumeration;
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
      // Kundennummer korrekt?
      String kd = konto.getKundennummer();
      if (kd == null || kd.length() == 0 || !kd.trim().matches("[0-9a-zA-Z]{1,30}"))
        return null;
      
      Support support = new Support();
      support.konto = konto;
      support.query = query;

      support.maxVersion = getMaxVersion(kd,query);
      
      // Wenn keine maxVersion ermittelbar ist, dann wird der Job per BPD gar nicht
      // unterstuetzt. Die restlichen Abfragen koennen wir uns dann schenken.
      if (support.maxVersion == null)
        return support;

      // Wird per BPD unterstuetzt - sonst haetten wir keine maxVersion
      support.bpdSupport = true;

      // Die BPD selbst ermitteln
      support.bpd = getBPD(kd,query,support.maxVersion);
      
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
   * @param kd die Kundennummer.
   * @param query die Abfrage.
   * @return die Versionsnummer oder NULL, wenn der Geschaeftsvorfall fuer das Konto nicht unterstuetzt wird.
   * @throws RemoteException
   */
  private static Integer getMaxVersion(final String kd, final Query query) throws RemoteException
  {
    final HBCIDBService service = Settings.getDBService();
    
    // Wir haengen noch unseren Prefix mit BPD und Kundennummer vorn dran. Das wurde vom Callback so erfasst
    final String prefix = Prefix.BPD.value() + DBPropertyUtil.SEP + kd.trim() + DBPropertyUtil.SEP;
    
    // Wir ermitteln die hoechste Segment-Version des Geschaeftsvorfalls
    String q = prefix + "Params%." + query.query + "Par%.SegHead.version";
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

    String q = Prefix.UPD.value() + DBPropertyUtil.SEP + "%" + DBPropertyUtil.SEP + "KInfo%";
    String segment = (String) service.execute("select name,content from property where name like ?",new String[] {q},new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        while (rs.next())
        {
          String name  = rs.getString("name");
          String value = rs.getString("content");

          if (name == null || name.length() == 0 || value == null || name.length() == 0)
            continue;

          if (name.endsWith(".iban") && value.equals(k.getIban()))
            return name;

          if (name.endsWith(".KTV.number") && value.equals(k.getKontonummer()))
            return name;
        }
        
        return null;
      }
    });
    
    if (segment == null || segment.length() == 0)
      return false;
    
    // Den Namen des KInfo-Elements ermitteln
    // Das Format des Segments ist ungefaehr so: "upd.<customernumber>.KInfo_<Nr>....
    // Wir wollen alles bis incl. "KInfo_<Nr>" haben
    int pos    = segment.indexOf(DBPropertyUtil.SEP + "KInfo");
    int offset = segment.substring(pos + 1).indexOf(DBPropertyUtil.SEP);
    segment    = segment.substring(0,pos + offset + 1);

    q = segment + ".AllowedGV%.code";
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
   * @param kd die Kundennummer.
   * @param query die Abfrage.
   * @param version die Segment-Version des Geschaeftsvorfalls.
   * @return Liste der Properties mit den BPD-Parametern.
   * Die Funktion liefert nie NULL sondern hoechstens leere Properties.
   * @throws RemoteException
   */
  private static TypedProperties getBPD(final String kd, final Query query, final Integer version) throws RemoteException
  {
    final TypedProperties props = new HBCITypedProperties();

    final HBCIDBService service = Settings.getDBService();

    // Wir haengen noch unseren Prefix mit BPD und Kundennummer vorn dran. Das wurde vom Callback so erfasst
    final String prefix = Prefix.BPD.value() + DBPropertyUtil.SEP + kd.trim() + DBPropertyUtil.SEP;
    String q = prefix + "Params%." + query.query + "Par" + (version != null ? version : "%") + ".Par" + query.query + "%";
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
   */
  public static void updateCache(HBCIPassport passport, Prefix prefix)
  {
    if (passport == null)
      return;

    try
    {
      final Properties data = prefix == Prefix.BPD ? passport.getBPD() : passport.getUPD();
      final String version  = prefix == Prefix.BPD ? passport.getBPDVersion() : passport.getUPDVersion();
      final String user     = passport.getUserId();
      
      if (version == null || version.length() == 0 || user == null || user.length() == 0 || data == null || data.size() == 0)
      {
        Logger.debug("[" + prefix + "] no version, no userid or no data found, skipping update");
        return;
      }

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
        long timestamp = Long.parseLong(DBPropertyUtil.get(prefix,user,null,"cacheupdate","0"));
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
        v = VersionUtil.getVersion(Settings.getDBService(),prefix.value() + "." + user);
        
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
        return;
      
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
      ProgressMonitor monitor = session != null ? session.getProgressMonitor() : null;

      if (monitor != null)
        monitor.log(i18n.tr("Aktualisiere " + prefix.name()));
      
      Logger.info("updating " + prefix + " cache");
      Set<String> customerIDs = HBCIProperties.getCustomerIDs(passport);
      
      int count = 0;
      
      for (String customerId:customerIDs)
      {
        int deleted = DBPropertyUtil.deleteScope(prefix,customerId);
        Logger.info("deleted " + deleted + " old " + prefix.name() + " cache entries");
        
        for (Enumeration keys = data.keys();keys.hasMoreElements();)
        {
          String name = (String) keys.nextElement();
          if (DBPropertyUtil.insert(prefix,customerId,null,name,data.getProperty(name)))
          {
            count++;
            if (count > 0 && count % 20 == 0 && monitor != null)
              monitor.log("  " + i18n.tr("{0} Datensätze",Integer.toString(count)));
          }
            
        }
      }
      Logger.info("created " + count + " new " + prefix.name() + " cache entries");
      
      // Speichern der neuen Versionsnummer
      v.store();
      
      // Datum des letzten Abrufs speichern
      DBPropertyUtil.set(prefix,user,null,"cacheupdate",Long.toString(now));
    }
    catch (Exception e)
    {
      Logger.error("error while updating " + prefix + " - will be ignored",e);
    }
  }
  
  /**
   * Markiert den Cache als expired.
   * @param passport der Passport.
   * @param prefix der Prefix.
   * @throws RemoteException
   */
  public static void expireCache(HBCIPassport passport, Prefix prefix) throws RemoteException
  {
    final String user = passport.getUserId();
    if (user == null || user.length() == 0)
    {
      Logger.debug("[" + prefix + "] no userid found, skipping cache expiry");
      return;
    }

    Logger.info("expire " + prefix.name() + " cache");
    DBPropertyUtil.set(prefix,user,null,"cacheupdate","0");
  }

}
