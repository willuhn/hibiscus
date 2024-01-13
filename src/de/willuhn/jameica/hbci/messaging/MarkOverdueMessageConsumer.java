/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.BaseDauerauftrag;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Wird benachrichtigt, wenn ein Objekt gespeichert wurde und aktualisiert die Anzahl der faelligen Auftraege.
 */
@Lifecycle(Type.CONTEXT)
public class MarkOverdueMessageConsumer implements MessageConsumer
{
  private final static ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
  private final static Map<String,AtomicLong> counters = new HashMap<String, AtomicLong>();
  
  private final static Map<Class,String> types = new HashMap<Class,String>()
  {{
    put(AuslandsUeberweisung.class,   "hibiscus.navi.transfer.uebforeign");
    put(SepaLastschrift.class,        "hibiscus.navi.transfer.sepalast");
    put(SepaSammelUeberweisung.class, "hibiscus.navi.transfer.sepasammelueb");
    put(SepaSammelLastschrift.class,  "hibiscus.navi.transfer.sepasammellast");
    put(SepaDauerauftrag.class,       "hibiscus.navi.transfer.sepadauer");
  }};

  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    if (Application.inServerMode())
      return;
    
    QueryMessage msg = (QueryMessage) message;
    final Entry<Class,String> type = this.getType(msg.getData());

    // unbekannter Typ
    if (type == null)
      return;
    
    final long currentValue = this.getCounter(type.getValue()).incrementAndGet();
    
    worker.schedule(new Runnable() {
      
      @Override
      public void run()
      {
        String typeId=type.getValue();
        // Zwischenzeitlich kam noch ein Aufruf rein. Dann soll der sich drum kuemmern
        // Das dient dazu, schnell aufeinander folgende Requests zu buendeln, damit
        // z.Bsp. beim Import von 100 Ueberweisungen nicht fuer jede Ueberweisung
        // 10 x pro Sekunde der Zaehler in der Navi angepasst werden muss.
        if (getCounter(typeId).get() != currentValue)
        {
          Logger.debug("ignoring frequent overdue counter updates for " + typeId);
          return;
        }
        
        update(type.getKey(), typeId);
      }
    },300,TimeUnit.MILLISECONDS);
  }
  
  /**
   * Initialisiert den Message-Consumer.
   */
  @PostConstruct
  private void init()
  {
    // Wir erstellen noch einen Worker, der einmal pro Stunde die Counter aktualisiert -
    // unabhängig davon, ob etwas geändert wurde. Dadurch werden die Counter auch dann
    // korrekt aktualisiert, wenn man das Programm über Nacht durchlaufen lässt
    
    if (Application.inServerMode())
      return;

    Logger.info("init mark-overdue message consumer");
    worker.scheduleAtFixedRate(() -> updateAll(),1,60,TimeUnit.MINUTES);
  }

  /**
   * Liefert den aktuellen Zaehlerstand fuer den Objekttyp.
   * @param key der Objekttyp.
   * @return der Zaehlerstand.
   */
  private AtomicLong getCounter(String key)
  {
    AtomicLong result = counters.get(key);
    if(result == null)
    {
      result = new AtomicLong(0);
      counters.put(key, result);
    }
    return result;
  }

  /**
   * Ermittelt Typ und ID des Objektes.
   * @param o das Objekt.
   * @return der Typ und die ID oder NULL, wenn nicht ermittelbar.
   */
  private Entry<Class,String> getType(Object o)
  {
    if (o == null)
      return null;
    
    // Checken, ob wir einen passenden Typ haben
    final Class type = o.getClass();
    for (Entry<Class,String> e:types.entrySet())
    {
      if (e.getKey().isAssignableFrom(type))
        return e;
    }
    
    return null;
  }
  
  /**
   * Aktualisiert einmalig alle Uberfaellig-Counter.
   */
  void updateAll()
  {
    Logger.info("update all unread counters");
    for (Entry<Class,String> e:types.entrySet())
    {
      update(e.getKey(),e.getValue());
    }
  }
  
  /**
   * Aktualisiert den Zaehler fuer den angegebenen Typ.
   * @param type der Typ.
   * @param id die ID des Navi-Elements.
   */
  private void update(final Class type, final String id)
  {
    if (type == null || id == null)
      return;
    
    try
    {
      Logger.debug("updating overdue counter for " + id);
      boolean da = BaseDauerauftrag.class.isAssignableFrom(type);
      HBCIDBService service = Settings.getDBService();
      DBIterator list = service.createList(type);
      if (!da)
        list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)");

      int sum = 0;
      while (list.hasNext())
      {
        GenericObject o = list.next();
        if (da)
        {
          BaseDauerauftrag t = (BaseDauerauftrag) o;
          if (!t.isActive())
            sum++;
        }
        else
        {
          Terminable t = (Terminable) o;
          if (t.ueberfaellig() && !t.ausgefuehrt())
            sum++;
        }
      }

      final int result = sum;
      GUI.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run()
        {
          GUI.getNavigation().setUnreadCount(id,result);
        }
      });
      
    }
    catch (Exception e)
    {
      Logger.error("unable to update number of overdue elements",e);
    }
  }

  @Override
  public boolean autoRegister()
  {
    // Per Manifest
    return false;
  }
  

}
