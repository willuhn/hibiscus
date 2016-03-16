/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.DBProperty;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.TypedProperties;

/**
 * Hilfsklasse zum Laden und Speichern der Properties.
 */
public class DBPropertyUtil
{
  /**
   * Der Prefix fuer die BPD.
   */
  public final static String PREFIX_BPD = "bpd";
  
  /**
   * Der Prefix fuer die UPD.
   */
  public final static String PREFIX_UPD = "upd";
  
  /**
   * Der Prefix fuer Meta-Daten.
   */
  public final static String PREFIX_META = "meta";
  
  
  /**
   * Query-Parameter fuer die BPD fuer "SEPA-Dauerauftrag aendern".
   */
  public final static String BPD_QUERY_SEPADAUER_EDIT = "Params%.DauerSEPAEditPar%.ParDauerSEPAEdit.%";
  
  /**
   * Filter fuer die BPD-Eintraege, die in den Cache uebernommen werden sollen.
   * Es werden nur jene Eintraege in die Datenbank uebernommen, deren Namen einen der folgenden String enthaelt.
   */
  public final static List<String> BPD_UPDATE_FILTER = new ArrayList<String>()
  {{
    add("ParDauerSEPAEdit");
  }};

  /**
   * Liefert die BPD fuer das Konto und den angegebenen Suchfilter.
   * @param konto das Konto.
   * @param query Suchfilter.
   * @return Liste der Properties.
   * Die Schluesselnamen sind um alle Prefixe gekuerzt, enthalten also nur noch den
   * eigentlichen Parameternamen wie etwas "maxusage".
   * Die Funktion liefert nie NULL sondern hoechstens leere Properties.
   * @throws RemoteException
   */
  public static TypedProperties getBPD(Konto konto, String query) throws RemoteException
  {
    final TypedProperties props = new HBCITypedProperties();

    // Konto angegeben?
    if (konto == null || query == null || query.length() == 0)
      return props;
    
    // Kundennummer korrekt?
    String kd = konto.getKundennummer();
    if (kd == null || kd.length() == 0 || !kd.trim().matches("[0-9a-zA-Z]{1,30}"))
      return props;

    // Wir haengen noch unseren Prefix vorn dran. Der wurde vom Callback hinzugefuegt
    query = PREFIX_BPD + "." + kd.trim() + "." + query;

    // Wir sortieren aufsteigend, da es pro BPD-Set (z.Bsp. in "%UebPar%") mehrere
    // gibt (jeweils pro Segment-Version). HBCI4Java nimmt bei Geschaeftsvorfaellen
    // immer die hoechste verfuegbare Segment-Version. Also machen wir das hier auch
    Settings.getDBService().execute("select name,content from property where name like ? order by name",new String[]{query},new ResultSetExtractor()
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
   * Legt ein Property neu an. Es wird vorher nicht gesucht, ob es bereits existiert.
   * @param name Name des Property.
   * @param value Wert des Property.
   * @throws RemoteException
   */
  public static void insert(String name, String value) throws RemoteException
  {
    if (value == null)
      return;
    
    if (name == null)
    {
      Logger.warn("parameter name missing");
      return;
    }
    
    try
    {
      DBService service = Settings.getDBService();
      DBProperty prop = (DBProperty) service.createObject(DBProperty.class,null);
      prop.setName(name);
      prop.setValue(value);
      prop.store();
    }
    catch (ApplicationException ae)
    {
      throw new RemoteException(ae.getMessage(),ae);
    }
  }
  

  /**
   * Speichert ein Property.
   * @param name Name des Property.
   * @param value Wert des Property.
   * @throws RemoteException
   */
  public static void set(String name, String value) throws RemoteException
  {
    DBProperty prop = find(name);
    if (prop == null)
    {
      Logger.warn("parameter name " + name + " invalid");
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
   * @param name Name des Parameters.
   * @param defaultValue Default-Wert, wenn der Parameter nicht existiert oder keinen Wert hat.
   * @return Wert des Parameters.
   * @throws RemoteException
   */
  public static String get(String name, String defaultValue) throws RemoteException
  {
    DBProperty prop = find(name);
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
  public static int deleteAll(String prefix) throws RemoteException
  {
    if (prefix == null || prefix.length() == 0)
      throw new RemoteException("no parameter prefix given");

    if (prefix.indexOf("%") != -1 || prefix.indexOf("_") != -1)
      throw new RemoteException("no wildcards allowed in parameter prefix");
    
    return Settings.getDBService().executeUpdate("delete from property where name like ?",prefix + ".%");
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
