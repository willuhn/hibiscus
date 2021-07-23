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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.Settings;

/**
 * Cache fuer oft geladene Fachobjekte.
 */
class Cache
{
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(Cache.class);
  private static int timeout = 0;
  
  // Enthaelt alle Caches.
  private final static Map<Class,Cache> caches = new HashMap<Class,Cache>();
  
  // Der konkrete Cache
  private Map<String,DBObject> data = new HashMap<String,DBObject>(); // Als Map fuer schnellen Zugriff auf einzelne Werte
  private List<DBObject> values = new LinkedList<DBObject>();         // Als Liste fuer die Sicherstellung der Reihenfolge
  private Class<? extends DBObject> type = null;
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
   * Loescht den genannten Cache.
   * @param type der Cache.
   */
  static void clear(Class<? extends DBObject> type)
  {
    caches.remove(type);
  }
  
  /**
   * Liefert den Cache fuer den genannten Typ.
   * @param type der Typ.
   * @param init true, wenn der Cache bei der Erzeugung automatisch befuellt werden soll.
   * @return der Cache.
   * @throws RemoteException
   */
  static Cache get(final Class<? extends DBObject> type, boolean init) throws RemoteException
  {
    return get(type,new ObjectFactory() {
      @Override
      public DBIterator load() throws RemoteException
      {
        return Settings.getDBService().createList(type);
      }
    },init);
  }
  
  /**
   * Liefert den Cache fuer den genannten Typ.
   * @param type der Typ.
   * @param factory die Object-Factory.
   * @param init true, wenn der Cache bei der Erzeugung automatisch befuellt werden soll.
   * @return der Cache.
   * @throws RemoteException
   */
  static Cache get(Class<? extends DBObject> type, ObjectFactory factory, boolean init) throws RemoteException
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
      
      if (init)
      {
        // Daten in den Cache laden
        DBIterator list = factory.load();
        while (list.hasNext())
        {
          DBObject o = (DBObject) list.next();
          cache.data.put(o.getID(),o);
          cache.values.add(o);
        }
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
  DBObject get(Object id) throws RemoteException
  {
    if (id == null)
      return null;
    
    String s = id.toString();
    
    DBObject value = data.get(s);
    
    if (value == null)
    {
      // Noch nicht im Cache. Vielleicht koennen wir es noch laden
      try
      {
        value = (DBObject) Settings.getDBService().createObject(type,s);
        if (value == null)
          return null;
        data.put(value.getID(),value);
        values.add(value);
      }
      catch (ObjectNotFoundException one)
      {
        // Objekt existiert nicht mehr
      }
    }
    return value;
  }
  
  /**
   * Liefert alle Werte aus dem Cache.
   * @return Liste der Werte aus dem Cache.
   */
  Collection<DBObject> values()
  {
    return values;
  }
  
  /**
   * Interface fuer eine Faktory, die die Objekte laedt.
   */
  public static interface ObjectFactory
  {
    /**
     * Laedt die Objekte.
     * @return die Objekte.
     * @throws RemoteException
     */
    public DBIterator load() throws RemoteException;
  }
}



/**********************************************************************
 * $Log: Cache.java,v $
 * Revision 1.6  2012/04/29 19:32:07  willuhn
 * @N Reihenfolge der Kategorien bei der Zuordnung beachten
 *
 * Revision 1.5  2010-12-14 12:48:00  willuhn
 * @B Cache wurde nicht immer korrekt aktualisiert, was dazu fuehren konnte, dass sich das Aendern/Loeschen/Anlegen von Kategorien erst nach 10 Sekunden auswirkte und bis dahin Umsaetze der Kategorie "nicht zugeordnet" zugewiesen wurden, obwohl sie in einer Kategorie waren
 *
 * Revision 1.4  2010-08-27 09:24:58  willuhn
 * @B Generics-Deklaration im Cache hat javac nicht akzeptiert (der Eclipse-Compiler hats komischerweise gefressen)
 *
 * Revision 1.3  2010-08-26 12:53:08  willuhn
 * @N Cache nur befuellen, wenn das explizit gefordert wird. Andernfalls wuerde der Cache u.U. unnoetig gefuellt werden, obwohl nur ein Objekt daraus geloescht werden soll
 *
 * Revision 1.2  2010-08-26 12:25:11  willuhn
 * @N 10 Sekunden Timeout fuer den Cache
 *
 * Revision 1.1  2010-08-26 11:31:23  willuhn
 * @N Neuer Cache. In dem werden jetzt die zugeordneten Konten von Auftraegen und Umsaetzen zwischengespeichert sowie die Umsatz-Kategorien. Das beschleunigt das Laden der Umsaetze und Auftraege teilweise erheblich
 *
 **********************************************************************/