/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/DBPropertyUtil.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/10/18 09:28:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

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
   * Query-Parameter fuer die BPD fuer "Ueberweisung absenden".
   */
  public final static String BPD_QUERY_UEB = "Params%.UebPar%.ParUeb.%";

  /**
   * Query-Parameter fuer die BPD fuer "Dauerauftrag aendern".
   */
  public final static String BPD_QUERY_DAUER_EDIT = "Params%.DauerEditPar%.ParDauerEdit.%";

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
    query = "bpd." + kd.trim() + "." + query;

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
        if (prop.isNewObject())
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
   * @throws RemoteException
   */
  public static void deleteAll(String prefix) throws RemoteException
  {
    if (prefix == null || prefix.length() == 0)
      throw new RemoteException("no parameter prefix given");

    if (prefix.indexOf("%") != -1 || prefix.indexOf("_") != -1)
      throw new RemoteException("no wildcards allowed in parameter prefix");
    
    DBIterator i = Settings.getDBService().createList(DBProperty.class);
    i.addFilter("name like ?",prefix + "%");
    try
    {
      while (i.hasNext())
      {
        DBProperty p = (DBProperty) i.next();
        p.delete();
      }
    }
    catch (ApplicationException ae)
    {
      throw new RemoteException(ae.getMessage(),ae);
    }
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


/*********************************************************************
 * $Log: DBPropertyUtil.java,v $
 * Revision 1.5  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.4  2011/10/14 13:25:37  willuhn
 * @N Parameter automatisch loeschen, wenn sie einen NULL-Wert kriegen
 *
 * Revision 1.3  2011-08-10 10:46:50  willuhn
 * @N Aenderungen nur an den DA-Eigenschaften zulassen, die gemaess BPD aenderbar sind
 * @R AccountUtil entfernt, Code nach VerwendungszweckUtil verschoben
 * @N Neue Abfrage-Funktion in DBPropertyUtil, um die BPD-Parameter zu Geschaeftsvorfaellen bequemer abfragen zu koennen
 *
 * Revision 1.2  2008/09/17 23:44:29  willuhn
 * @B SQL-Query fuer MaxUsage-Abfrage korrigiert
 *
 * Revision 1.1  2008/05/30 14:23:48  willuhn
 * @N Vollautomatisches und versioniertes Speichern der BPD und UPD in der neuen Property-Tabelle
 *
 **********************************************************************/