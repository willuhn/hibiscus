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
import java.util.HashSet;
import java.util.Set;

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
  /**
   * Separator-Zeichen fuer die Properties.
   */
  public final static char SEP = '.';
  
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
    UPD("upd",new HashSet(Arrays.asList("KInfo"))),
    
    /**
     *  Prefix fuer Meta-Daten.
     */
    META("meta",null),
    
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
    if (prefix != null)
    {
      sb.append(prefix.value());
      sb.append(SEP);
    }
    if (scope != null && scope.length() > 0)
    {
      sb.append(scope);
      sb.append(SEP);
    }
    if (id != null && id.length() > 0)
    {
      sb.append(id);
      sb.append(SEP);
    }
    if (name != null && name.length() > 0)
    {
      sb.append(name);
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
      // Kein Wert angegeben
      if (value == null)
      {
        // Wenn er in der DB existiert, loeschen wir ihn gleich ganz
        if (!prop.isNewObject())
          prop.delete();
        
        // auf jeden Fall nichts zu speichern
        return;
      }

      // Ansonsten abspeichern
      prop.setValue(value);
      prop.store();
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
  public static String get(Prefix prefix, String scope, String id, String name, String defaultValue) throws RemoteException
  {
    String localname = createIdentifier(prefix,scope,id,name);

    DBProperty prop = find(localname);
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

    return Settings.getDBService().executeUpdate("delete from property where name like ?",prefix.value() + ".%");
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

    if (scope.indexOf("%") != -1 || scope.indexOf("_") != -1)
      throw new RemoteException("no wildcards allowed in scope");
    
    String localPrefix = prefix.value() + "." + scope;
    return Settings.getDBService().executeUpdate("delete from property where name like ?",localPrefix + ".%");
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

    if (scope.indexOf("%") != -1 || scope.indexOf("_") != -1)
      throw new RemoteException("no wildcards allowed in scope");

    if (id != null && (id.indexOf("%") != -1 || scope.indexOf("_") != -1))
      throw new RemoteException("no wildcards allowed in id");

    String localPrefix = createIdentifier(prefix,scope,id,null);
    return Settings.getDBService().executeUpdate("delete from property where name like ?",localPrefix + ".%");
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
