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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.DBProperty;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.TypedProperties;

/**
 * Hilfsklasse zum Laden und Speichern der Properties.
 */
public class DBPropertyUtil
{
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(DBPropertyUtil.class);
  private static int timeout = 0;

  private static Cache<String,Map<String,DBProperty>> CACHE = null;
  
  static
  {
    // Wir muessen die Haltezeit des Caches limitieren. Denn wenn eine MySQL-Datenbank zum Einsatz kommt,
    // koennen Datenaenderungen ja auch durch andere User erfolgen. Wuerde der Cache nicht expiren, wuerden
    // wir diese Aenderungen bis zum Neustart des Clients nicht mitbekommen. Der Cache soll auch nur das Laden von fast identischen
    // Properties in hoher Frequenz zusammenfassen. Z.Bsp. die Reminder-UUIDs bei Auftraegen. Siehe auch de.willuhn.jameica.hbci.server.Cache
    // (identisch, jedoch fuer Fachobjekte)
    timeout = settings.getInt("timeout.seconds",10);
    logCache("init [timeout: " + timeout + " seconds]");
    CacheBuilder builder = CacheBuilder.newBuilder();
    builder.expireAfterWrite(timeout,TimeUnit.SECONDS); // Ja, write. Andernfalls koennte man das Timeout mit dauernden Reloads ja aufhalten
    CACHE = builder.build();
  }

  /**
   * Separator-Zeichen fuer die Properties.
   */
  public final static char SEP = '.';
  
  /**
   * Der Key, in dem wir den Timestamp mit dem letzten Cache-Update speichern.
   */
  public final static String KEY_CACHE_UPDATE = "cacheupdate";
  
  /**
   * Definition der Prefixe.
   */
  public enum Prefix
  {
    /**
     * Prefix fuer BPDs.
     */
    BPD("bpd",new HashSet(Arrays.asList("DauerSEPAEditPar","KontoauszugPar","KontoauszugPdfPar","KUmsZeitCamtPar"))),
    
    /**
     * Prefix fuer UPDs.
     */
    UPD("upd",new HashSet(Arrays.asList("KInfo","UPA"))),
    
    /**
     *  Prefix fuer Meta-Daten.
     */
    META("meta",null),
    
    /**
     * Prefix für Ungelesen-Markierungen.
     */
    UNREAD("unread",null),
    
    ;
    
    private String value = null;
    private Set<String> filter = null;
    
    /**
     * ct.
     * @param value der Wert des Prefix, mit dem er in der Datenbank erscheint.
     * @param filter optionale Angabe von Filtern. Wenn welche angegeben sind, werden nur Parameter dieses Filters angelegt.
     */
    private Prefix(String value, Set<String> filter)
    {
      this.value = value;
      this.filter = filter;
    }
    
    /**
     * Liefert den Wert des Prefix.
     * @return der Wert des Prefix.
     */
    public String value()
    {
      return this.value;
    }
    
    /**
     * Liefert die optionalen Filter.
     * @return die optionalen Filter.
     */
    private Set<String> getFilter()
    {
      return this.filter;
    }
  }
  
  /**
   * Kapselt die Update-Stats.
   */
  public static class Update
  {
    /**
     * Die Anzahl durchgefuehrter Inserts.
     */
    public int inserts = 0;
    
    /**
     * Die Anzahl durchgefuehrter Updates.
     */
    public int updates = 0;
    
    /**
     * Die Anzahl durchgefuehrter Loeschungen.
     */
    public int deletes = 0;
  }
  
  /**
   * Legt ein Property neu an. Es wird vorher nicht gesucht, ob es bereits existiert.
   * @param prefix der Prefix.
   * @param scope der Scope.
   * @param id optionale ID.
   * @param name Name des Property.
   * @param value Wert des Property.
   * @return true, wenn der Parameter angelegt wurde.
   * @throws RemoteException
   */
  public static boolean insert(Prefix prefix, String scope, String id, String name, String value) throws RemoteException
  {
    if (value == null)
      return false;
    
    if (prefix == null)
    {
      Logger.warn("prefix missing");
      return false;
    }

    if (scope == null || scope.length() == 0)
    {
      Logger.warn("scope missing");
      return false;
    }

    if (name == null || name.length() == 0)
    {
      Logger.warn("name missing");
      return false;
    }

    // Checken, ob Filter definiert sind
    Set<String> filters = prefix.getFilter();
    if (filters != null && filters.size() > 0)
    {
      boolean found = false;
      // Pruefen, ob der Name im Filter enthalten ist
      for (String filter:filters)
      {
        if (name.contains(filter))
        {
          found = true;
          break;
        }
      }
      
      if (!found)
        return false;
    }
    
    try
    {
      String localName = createIdentifier(prefix,scope,id,name);
      DBService service = Settings.getDBService();
      DBProperty prop = (DBProperty) service.createObject(DBProperty.class,null);
      prop.setName(localName);
      prop.setValue(value);
      prop.store();

      // Wenn wir den Cache bereits haben, tragen wir den Datensatz dort ein.
      // Wenn er nicht existiert, dann muss er eh neu geladen werden
      final Map<String,DBProperty> cache = CACHE.getIfPresent(createScopeIdentifier(prefix,scope));
      if (cache != null)
      {
        logCache("added " + localName + "=" + value);
        cache.put(localName,prop);
      }
      
      return true;
    }
    catch (ApplicationException ae)
    {
      throw new RemoteException(ae.getMessage(),ae);
    }
  }
  
  /**
   * Liefert den passenden Identifier fuer den Datensatz.
   * @param prefix der Prefix.
   * @param scope der Scope.
   * @param id die optionale ID.
   * @param name der Name des Parameters.
   * @return der Identifier.
   */
  private static String createIdentifier(Prefix prefix, String scope, String id, String name)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(createScopeIdentifier(prefix,scope));
    
    if (id != null && id.length() > 0)
    {
      sb.append(replaceWildcards(id));
      sb.append(SEP);
    }
    if (name != null && name.length() > 0)
    {
      sb.append(name);
    }
    
    return sb.toString();
  }
  
  /**
   * Erzeugt den Identifier fuer den Scope.
   * @param prefix der Prefix.
   * @param scope der Scope.
   * @return der Identifier fuer den Scope.
   */
  private static String createScopeIdentifier(Prefix prefix, String scope)
  {
    StringBuilder sb = new StringBuilder();
    if (prefix != null)
    {
      sb.append(prefix.value());
      sb.append(SEP);
    }
    if (scope != null && scope.length() > 0)
    {
      sb.append(replaceWildcards(scope));
      sb.append(SEP);
    }
    
    return sb.toString();
  }

  /**
   * Speichert ein Property.
   * @param prefix der Prefix.
   * @param scope der Scope.
   * @param id optionale ID.
   * @param name Name des Property.
   * @param value Wert des Property.
   * @throws RemoteException
   */
  public static void set(Prefix prefix, String scope, String id, String name, String value) throws RemoteException
  {
    String localname = createIdentifier(prefix,scope,id,name);
    
    DBProperty prop = find(localname);
    if (prop == null)
    {
      Logger.warn("parameter name " + localname + " invalid");
      return;
    }

    try
    {
      final Map<String,DBProperty> cache = CACHE.getIfPresent(createScopeIdentifier(prefix,scope));
      
      // Kein Wert angegeben
      if (value == null)
      {
        // Wenn er in der DB existiert, loeschen wir ihn gleich ganz
        if (!prop.isNewObject())
        {
          prop.delete();
          if (cache != null)
          {
            logCache("removed " + localname);
            cache.remove(localname);
          }
        }
        
        // auf jeden Fall nichts zu speichern
        return;
      }

      // Ansonsten abspeichern
      prop.setValue(value);
      prop.store();

      // Wenn wir den Cache bereits haben, aktualisieren wir ihn
      // Wenn er nicht existiert, dann muss er eh neu geladen werden
      if (cache != null)
      {
        logCache("added " + localname + "=" + value);
        cache.put(localname,prop);
      }

    }
    catch (ApplicationException ae)
    {
      throw new RemoteException(ae.getMessage(),ae);
    }
  }
  
  /**
   * Liefert den Wert des Parameters.
   * @param prefix der Prefix.
   * @param scope der Scope.
   * @param id optionale ID.
   * @param name Name des Property.
   * @param defaultValue Default-Wert, wenn der Parameter nicht existiert oder keinen Wert hat.
   * @return Wert des Parameters.
   * @throws RemoteException
   */
  public static String get(final Prefix prefix, final String scope, String id, String name, String defaultValue) throws RemoteException
  {
    // Scope aus dem Cache holen bzw. ggf. automatisch laden
    final String localName = createIdentifier(prefix,scope,id,name);

    DBProperty prop = null;
    boolean cacheChecked = false;
    try
    {
      final String localPrefix = createScopeIdentifier(prefix,scope);
      final Map<String,DBProperty> cache = CACHE.get(localPrefix,new Callable<Map<String,DBProperty>>() {
        /**
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public Map<String, DBProperty> call() throws Exception
        {
          logCache("loaded scope " + localPrefix);
          return getScope(prefix,scope);
        }
      });

      prop = cache.get(localName);
      cacheChecked = true;
    }
    catch (Exception e)
    {
      Logger.error("cache lookup error",e);
    }

    // Wenn prop null ist, duerfen wir nur dann in der DB schauen, wenn nicht im Cache geschaut
    // wurde (z.Bsp. wegen einer Exception). Andernfalls wuerde fuer jedes Property, welches nicht
    // existiert erst im Cache geschaut und dann nochmal extra in der Datenbank gesucht werden. Damit
    // waere der Performance-Vorteil komplett dahin.
    if (prop == null && !cacheChecked)
      prop = find(localName);
    
    if (prop == null)
      return defaultValue;
    String value = prop.getValue();
    return value != null ? value : defaultValue;
  }
  
  /**
   * Loescht alle Parameter, deren Namen mit dem angegebenen Prefix beginnt.
   * @param prefix der prefix.
   * @return die Anzahl der geloeschten Datensaetze.
   * @throws RemoteException
   */
  public static int deleteAll(Prefix prefix) throws RemoteException
  {
    if (prefix == null)
      throw new RemoteException("no prefix given");

    final int count = Settings.getDBService().executeUpdate("delete from property where name like ?",prefix.value() + ".%");
    
    // Wir wissen nicht, welches Scopes im Cache sind. Daher loeschen wir in dem Fall alles.
    logCache("invalidate cache");
    CACHE.invalidateAll();
    return count;
  }

  /**
   * Loescht alle passenden Parameter.
   * @param prefix der Prefix.
   * @param scope einschraenkender Scope.
   * @return die Anzahl der geloeschten Datensaetze.
   * @throws RemoteException
   */
  public static int deleteScope(Prefix prefix, String scope) throws RemoteException
  {
    if (prefix == null)
      throw new RemoteException("no prefix given");

    if (scope == null || scope.length() == 0)
      throw new RemoteException("no scope given");

    final String localPrefix = createScopeIdentifier(prefix,scope);
    final int count = Settings.getDBService().executeUpdate("delete from property where name like ?",localPrefix + "%");
    
    logCache("invalidate scope " + localPrefix);
    CACHE.invalidate(localPrefix);
    return count;
  }
  
  /**
   * Liefert alle passenden Parameter.
   * @param prefix der Prefix.
   * @param scope einschraenkender Scope.
   * @return Map mit den Parametern.
   * @throws RemoteException
   */
  private static Map<String, DBProperty> getScope(Prefix prefix, String scope) throws RemoteException
  {
    if (prefix == null)
      throw new RemoteException("no prefix given");

    if (scope == null || scope.length() == 0)
      throw new RemoteException("no scope given");
    
    scope = replaceWildcards(scope);

    final String localPrefix = createScopeIdentifier(prefix,scope);
    DBIterator<DBProperty> list = Settings.getDBService().createList(DBProperty.class);
    list.addFilter("name like ?",localPrefix + "%");
    
    Map<String,DBProperty> result = new HashMap<String,DBProperty>();
    while (list.hasNext())
    {
      DBProperty prop = list.next();
      String name = prop.getName();

      // Den internen Key nicht mit liefern.
      if (ObjectUtils.equals(name,createIdentifier(prefix,scope,null,KEY_CACHE_UPDATE)))
        continue;
      
      result.put(name,prop);
    }
    return result;
  }
  
  /**
   * Aktualisiert die Parameter.
   * @param prefix der Prefix.
   * @param scope einschraenkender Scope.
   * @param update die Updates.
   * Parameter, die in den Updates enthalten sind, in der lokalen Datenbank jedoch noch nicht, werden neu angelegt.
   * Parameter, die in der lokalen Datenbank enthalten sind, im Update jedoch nicht mehr, werden geloescht.
   * Parameter, die in den Updates einen anderen Wert haben, werden in der lokalen Datenbank aktualisiert.
   * @return die Update-Statistik.
   * @throws RemoteException
   */
  public static Update updateScope(Prefix prefix, String scope, Properties update) throws RemoteException
  {
    Update result = new Update();
    
    if (prefix == null)
    {
      Logger.warn("no prefix given");
      return result;
    }
    
    if (update == null || update.size() == 0)
    {
      Logger.warn("no update given");
      return result;
    }

    if (scope == null || scope.length() == 0)
    {
      Logger.warn("no scope given");
      return result;
    }

    scope = replaceWildcards(scope);
    
    Map<String,DBProperty> current = getScope(prefix,scope);
    Set<String> updateKeys = new HashSet<String>();
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    // Neue und geaenderte Schluessel
    for (Enumeration keys = update.keys();keys.hasMoreElements();)
    {
      String name        = (String) keys.nextElement();
      String localname   = createIdentifier(prefix,scope,null,name);
      updateKeys.add(localname);

      DBProperty oldValue = current.get(localname);
      String updateValue  = update.getProperty(name);

      // Wert ist bereits vorhanden
      if (oldValue != null)
      {
        // Unveraendert: nichts zu tun
        if (ObjectUtils.equals(oldValue.getValue(),updateValue))
          continue;
        
        // Geaendert: Wert aktualisieren
        oldValue.setValue(updateValue);
        
        try
        {
          oldValue.store();
          result.updates++;
        }
        catch (ApplicationException ae)
        {
          throw new RemoteException(ae.getMessage(),ae);
        }
      }
      else
      {
        // Wert ist neu
        if (insert(prefix,scope,null,name,updateValue))
          result.inserts++;
      }
    }
    //
    //////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Zu loeschende Schluessel
    for (Entry<String,DBProperty> e:current.entrySet())
    {
      if (!updateKeys.contains(e.getKey()))
        result.deletes += Settings.getDBService().executeUpdate("delete from property where name = ?",e.getKey());
    }
    //
    //////////////////////////////////////////////////////////////////////////////////////////////
    
    // Wir loeschen den Cache des ganzen Scope
    final String localPrefix = createScopeIdentifier(prefix,scope);
    logCache("invalidate scope " + localPrefix);
    CACHE.invalidate(localPrefix);

    return result;
  }

  /**
   * Loescht alle passenden Parameter, deren Namen mit dem angegebenen Prefix beginnt und die der Kundenkennung zugeordnet sind.
   * @param prefix der Prefix.
   * @param scope einschraenkender Scope.
   * @param id optionale Angabe der ID.
   * @return die Anzahl der geloeschten Datensaetze.
   * @throws RemoteException
   */
  public static int delete(Prefix prefix, String scope, String id) throws RemoteException
  {
    if (prefix == null)
      throw new RemoteException("no prefix given");

    if (scope == null || scope.length() == 0)
      throw new RemoteException("no scope given");

    String localPrefix = createIdentifier(prefix,scope,id,null);
    int count = Settings.getDBService().executeUpdate("delete from property where name like ?",localPrefix + "%");
    
    // Wir loeschen den Cache des ganzen Scope
    final String localPrefix2 = createScopeIdentifier(prefix,scope);
    logCache("invalidate scope " + localPrefix2);
    CACHE.invalidate(localPrefix2);

    return count;
  }

  /**
   * Liefert den Parameter mit dem genannten Namen.
   * Wenn er nicht existiert, wird er automatisch angelegt.
   * @param name Name des Parameters. Darf nicht <code>null</code> sein.
   * @return der Parameter oder <code>null</code>, wenn kein Name angegeben wurde.
   * @throws RemoteException
   */
  private static DBProperty find(String name) throws RemoteException
  {
    if (name == null)
      return null;
    
    // Mal schauen, ob wir das Property schon haben
    DBService service = Settings.getDBService();
    DBIterator i = service.createList(DBProperty.class);
    i.addFilter("name = ?",name);
    if (i.hasNext())
      return (DBProperty) i.next();

    // Ne, dann neu anlegen
    DBProperty prop = (DBProperty) service.createObject(DBProperty.class,null);
    prop.setName(name);
    return prop;
  }
  
  /**
   * Loggt Informationen zum Caching. An zentraler Stelle, damit das Loglevel dafuer hier geaendert werden kann.
   * @param msg die zu loggende Nachricht.
   */
  private static void logCache(String msg)
  {
    Logger.debug("*** [CACHE] " + msg);
  }
  
  /**
   * Ersetzt Wildcards in dem String.
   * @param s der String.
   * @return der String mit ersetzten Wildcards.
   */
  private static String replaceWildcards(String s)
  {
    if (s == null || s.length() == 0)
      return s;
    
    s = s.replace("%","-");
    s = s.replace("_","-");
    return s;
  }
  
  /**
   * Ueberschrieben, weil boolsche Werte in den BPD mit "J","N" statt "true","false"
   * gespeichert sind.
   */
  public static class HBCITypedProperties extends TypedProperties
  {
    /**
     * @see de.willuhn.util.TypedProperties#getBoolean(java.lang.String, boolean)
     */
    public boolean getBoolean(String name, boolean defaultValue)
    {
      String s = super.getProperty(name,defaultValue ? "J" : "N");
      return "J".equalsIgnoreCase(s);
    }
  }
}
