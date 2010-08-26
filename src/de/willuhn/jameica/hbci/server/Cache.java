/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Cache.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/08/26 12:25:11 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.Settings;

/**
 * Cache fuer oft geladene Fachobjekte.
 */
class Cache<T extends DBObject>
{
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(Cache.class);
  private static int timeout = 0;
  
  // Enthaelt alle Caches.
  private final static Map<Class,Cache> caches = new HashMap<Class,Cache>();
  
  // Der konkrete Cache
  private Map<String,T> data = new HashMap<String,T>();
  private Class<T> type = null;
  private long validTo = 0;
  
  static
  {
    settings.setStoreWhenRead(false);
    
    // Das Timeout betraegt nur 10 Sekunden. Mehr brauchen wir nicht.
    // Es geht ja nur darum, dass z.Bsp. beim Laden der Umsaetze die
    // immer wieder gleichen zugeordneten Konten oder Umsatz-Kategorien
    // nicht dauernd neu geladen sondern kurz zwischengespeichert werden
    // Das Timeout generell wird benoetigt, wenn mehrere Hibiscus-Instanzen
    // sich eine Datenbank teilen. Andernfalls wuerde Hibiscus die
    // Aenderungen der anderen nicht mitkriegen
    timeout = settings.getInt("timeout.seconds",10);
  }

  /**
   * ct.
   */
  private Cache()
  {
    touch();
  }
  
  /**
   * Aktualisiert das Verfallsdatum des Caches.
   */
  private void touch()
  {
    this.validTo = System.currentTimeMillis() + (timeout * 1000);
  }
  
  /**
   * Liefert den Cache fuer den genannten Typ.
   * @param type der Typ.
   * @return der Cache.
   * @throws RemoteException
   */
  static <T> Cache get(Class<? extends DBObject> type) throws RemoteException
  {
    Cache cache = caches.get(type);
    
    if (cache != null)
    {
      if (cache.validTo < System.currentTimeMillis())
      {
        caches.remove(type);
        cache = null; // Cache wegwerfen
      }
      else
      {
        cache.touch(); // Verfallsdatum aktualisieren
      }
    }
    
    // Cache erzeugen und mit Daten fuellen
    if (cache == null)
    {
      cache = new Cache();
      cache.type = type;
      
      // Daten in den Cache laden
      DBIterator list = Settings.getDBService().createList(type);
      while (list.hasNext())
      {
        DBObject o = (DBObject) list.next();
        cache.data.put(o.getID(),o);
      }
      caches.put(type,cache);
    }
    return cache;
  }
  
  /**
   * Liefert ein Objekt aus dem Cache.
   * @param id die ID des Objektes.
   * @return das Objekt oder NULL, wenn es nicht existiert.
   * @throws RemoteException
   */
  T get(Object id) throws RemoteException
  {
    if (id == null)
      return null;
    
    String s = id.toString();
    
    T value = data.get(s);
    
    if (value == null)
    {
      // Noch nicht im Cache. Vielleicht koennen wir es noch laden
      try
      {
        value = (T) Settings.getDBService().createObject(type,s);
        put(value); // tun wir gleich in den Cache
      }
      catch (ObjectNotFoundException one)
      {
        // Objekt existiert nicht mehr
      }
    }
    return value;
  }
  
  /**
   * Speichert ein Objekt im Cache.
   * @param object das zu speichernde Objekt.
   * @throws RemoteException
   */
  void put(T object) throws RemoteException
  {
    if (object == null)
      return;
    data.put(object.getID(),object);
  }
  
  /**
   * Entfernt ein Objekt aus dem Cache.
   * @param object das zu entfernende Objekt.
   * @throws RemoteException
   */
  void remove(T object) throws RemoteException
  {
    if (object == null)
      return;
    data.remove(object.getID());
  }
  
  /**
   * Liefert alle Werte aus dem Cache.
   * @return Liste der Werte aus dem Cache.
   */
  Collection<T> values()
  {
    return data.values();
  }
}



/**********************************************************************
 * $Log: Cache.java,v $
 * Revision 1.2  2010/08/26 12:25:11  willuhn
 * @N 10 Sekunden Timeout fuer den Cache
 *
 * Revision 1.1  2010-08-26 11:31:23  willuhn
 * @N Neuer Cache. In dem werden jetzt die zugeordneten Konten von Auftraegen und Umsaetzen zwischengespeichert sowie die Umsatz-Kategorien. Das beschleunigt das Laden der Umsaetze und Auftraege teilweise erheblich
 *
 **********************************************************************/