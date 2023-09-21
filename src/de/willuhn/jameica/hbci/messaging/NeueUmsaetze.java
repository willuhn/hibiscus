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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.DBPropertyUtil;
import de.willuhn.jameica.hbci.server.DBPropertyUtil.Prefix;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Ueber die Klasse koennen die in der aktuellen Session
 * abgerufenen Umsaetze ermittelt werden.
 */
public class NeueUmsaetze implements MessageConsumer
{
  private static Timer timer        = null;
  private static SchedulerTask task = null;
  private static DelayedListener listener = new DelayedListener(1000,new Worker());
  private static Set<String> unread = new HashSet<>();

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  @Override
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  @Override
  public Class[] getExpectedMessageTypes()
  {
    // Die ObjectChangedMessage ist hier nicht dabei. Das wäre auch gefährlich, weil
    // wir die weiter unten selbst schicken und damit eine Rekursion auslösen könnten.
    return new Class[]{ImportMessage.class,ObjectDeletedMessage.class,SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  @Override
  public void handleMessage(Message message) throws Exception
  {
    if (message instanceof SystemMessage)
    {
      final SystemMessage msg = (SystemMessage) message;
      if (msg.getStatusCode() == SystemMessage.SYSTEM_STARTED)
      {
        load(false);
        timer  = new Timer();
        task   = new SchedulerTask();
        timer.schedule(task,60 * 1000L,60 * 1000L);
      }
      else if (msg.getStatusCode() == SystemMessage.SYSTEM_SHUTDOWN)
      {
        if (Settings.getMarkReadOnExit())
        {
          Logger.info("clear unread mark for bookings");
          unread.clear();
        }
        
        store();
      }
    }
    
    if (message instanceof ImportMessage)
    {
      GenericObject o = ((ImportMessage)message).getObject();
      
      if (o == null || !(o instanceof Umsatz) || o.getID() == null)
        return; // interessiert uns nicht
      
      unread.add(o.getID());
      
      if (Settings.getStoreUnreadFlag())
        listener.handleEvent(null);
    }
    
    if (message instanceof ObjectDeletedMessage)
    {
      final ObjectDeletedMessage msg = (ObjectDeletedMessage) message;
      final GenericObject o = msg.getObject();
      
      if (!(o instanceof Umsatz))
        return; // interessiert uns nicht
      
      if (unread.remove(msg.getID()) && Settings.getStoreUnreadFlag())
        listener.handleEvent(null);
    }
  }
  
  /**
   * Liefert eine Liste mit allen in der aktuellen Sitzung hinzugekommenen Umsaetzen.
   * @return Liste der neuen Umsaetze.
   * @throws RemoteException
   */
  public static GenericIterator<Umsatz> getNeueUmsaetze() throws RemoteException
  {
    if (unread.size() == 0)
      return PseudoIterator.fromArray(new Umsatz[0]);
    
    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("id in (" + StringUtils.join(unread,",") + ")");
    if (list.size() == 0)
      unread.clear(); // Wenn nichts gefunden wurde, resetten wir uns
    return list;
  }
  
  /**
   * Markiert einen oder mehrere Umsaetze als ungelesen.
   * @param umsaetze der oder die als ungelesen zu markierende Umsatz.
   */
  public static void setUnread(Object umsaetze)
  {
    update(id -> unread.add(id),umsaetze);
  }
  
  /**
   * Markiert einen oder mehrere Umsaetze als gelesen.
   * @param umsaetze der oder die als gelesen zu markierende Umsatz.
   */
  public static void setRead(Object umsaetze)
  {
    update(id -> unread.remove(id),umsaetze);
  }
  
  /**
   * Markiert einen oder mehrere Umsaetze als ungelesen.
   * @param umsaetze der oder die als ungelesen zu markierende Umsatz.
   */
  private static void update(Function<String,Boolean> action, Object umsaetze)
  {
    try
    {
      if (umsaetze == null)
        return;
      
      List list = new ArrayList();
      
      if (umsaetze instanceof Umsatz)
        list.add((Umsatz) umsaetze);
      if (umsaetze instanceof List)
        list = (List) umsaetze;
      if (umsaetze instanceof Object[])
        list = Arrays.asList((Object[])umsaetze);
      
      for (Object o:list)
      {
        if (!(o instanceof Umsatz))
          continue;
        
        final Umsatz u = (Umsatz) o;
        
        if (u.isNewObject())
          continue;
        
        action.apply(u.getID());
        Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(u));
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to mark as read/unread",e);
    }
    finally
    {
      store();
    }
  }
  
  /**
   * Liefert die Anzahl neuer Umsätze.
   * @return die Anzahl neuer Umsätze.
   */
  public static int size()
  {
    return unread.size();
  }

  /**
   * Liefert true, wenn der Umsatz in der aktuellen Sitzung abgerufen wurde.
   * @param u der zu pruefende Umsatz.
   * @return true, wenn er neu ist.
   */
  public static boolean isNew(Umsatz u)
  {
    try
    {
      if (u == null || u.isNewObject())
        return false;
      
      return unread.contains(u.getID());
    }
    catch (Exception e)
    {
      Logger.error("unable to determine new state",e);
    }
    return false;
  }
  
  /**
   * Setzt den Ungelesen-Zaehler der Umsaetze auf 0.
   */
  public static void setAllRead()
  {
    if (unread.size() == 0)
      return;

    try
    {
      unread.clear();
      store();
      
      // Anzeige aktualisieren
      // Im Prinzip koennten wir fuer jeden Umsatz, der vorher als neu galt, eine ObjectChangedMessage schicken
      // Das funktioniert aber nicht ganz sauber, denn:
      // 1) In der Umsatzliste wird der Fettdruck zwar entfernt. Da sich dabei aber die Objekt-Referenzen aendern
      //    (die Umsaetze wuerden hier ja neu geladen werden), hat das u.U. zu Konsequenz, dass ein Umsatz nicht mehr
      //    verschwindet, wenn man ihn direkt danach loescht. Erst beim Neuladen der View ist er weg.
      // 2) Wenn auf der Startseite die View "Neue Umsätze" aktiv ist, würde dort nur die Fett-Markierung entfernt
      //    werden. Stattdessen müssen die Umsätze dort aber entfernt werden.
      
      // Daher schicken wir keine ObjectChangeMessage sondern laden die aktuelle View neu.
      // GUI.getCurrentView().reload() wird von vielen Views nicht implementiert. Daher starten wir die View neu
      // Achtung: Nicht die Instanz der View wiederverwenden. Bringt keinen Vorteil. Verlangt aber, dass die View
      // alle Resourcen sauber disposed und neu erstellt. KontoNew macht das z.Bsp. nicht, weil es den Controller
      // als Member haelt.
      if (!Application.inServerMode())
        GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
    }
    catch (Exception e)
    {
      Logger.error("unable to refresh view",e);
    }
  }
  
  /**
   * Lädt die Ungelesen-Infos der Umsätze aus der Datenbank neu.
   */
  public static void reload()
  {
    load(true);
  }
  
  /**
   * Speichert die Ungelesen-Meldungen in der Datenbank.
   */
  private static synchronized void store()
  {
    if (Settings.getStoreUnreadFlag())
    {
      try
      {
        Logger.info("store umsatz unread count");
        DBPropertyUtil.set(Prefix.UNREAD,"umsatz",null,"count",StringUtils.join(unread,","));
        Logger.info("umsatz unread count: " + unread.size());
      }
      catch (Throwable t)
      {
        Logger.error("unable to store umsatz unread count",t);
      }
    }
    
    updateUI();
  }
  
  /**
   * Laedt die Ungelesen-Meldungen aus der Datenbank.
   * @param reload true, wenn es sich um ein Reload handelt. In dem Fall wird das Loglevel auf DEBUG gesetzt.
   */
  private static synchronized void load(boolean reload)
  {
    if (Settings.getStoreUnreadFlag())
    {
      final Level level = reload ? Level.DEBUG : Level.INFO;
      try
      {
        Logger.write(level,"load umsatz unread count");
        
        // Die neuen IDs laden
        final String[] ids = StringUtils.split(DBPropertyUtil.get(Prefix.UNREAD,"umsatz",null,"count",""),",");
        unread.clear();
        unread.addAll(Arrays.asList(ids));
        
        // Wir müssen für jeden einzelnen Umsatz checken, ob er existiert
        // Andernfalls würden sich hier IDs von Umsätzen sammeln, die anderweitig gelöscht wurden.
        // Es bleiben nur noch die übrig, die in der Datenbank existieren
        final Set<String> existing = new HashSet<String>();
        final GenericIterator<Umsatz> fromDb = getNeueUmsaetze();
        while (fromDb.hasNext())
        {
          final Umsatz u = fromDb.next();
          existing.add(u.getID());
          unread.remove(u.getID());
        }
        
        // Wenn jetzt noch welche in unread drin sind, dann sind das genau die,
        // die inzwischen nicht mehr existieren
        if (unread.size() > 0)
          Logger.info("removed unread entries that no longer exist in database: " + StringUtils.join(unread,","));
        
        unread = existing;
        Logger.write(level,"umsatz unread count: " + unread.size());
      }
      catch (Throwable t)
      {
        Logger.error("unable to load umsatz unread count",t);
      }
    }

    updateUI();
  }
  
  /**
   * Aktualisiert den Ungelesen-Zähler in der UI.
   */
  private static void updateUI()
  {
    if (Application.inServerMode())
      return;
    
    GUI.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run()
      {
        GUI.getNavigation().setUnreadCount("hibiscus.navi.umsatz",size());
      }
    });
  }
  
  /**
   * Der Worker zum zeitverzögerten Speichern der Ungelesen-Zähler beim Eintreffen neuer Umsätze.
   */
  private static class Worker implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      store();
    }
  }
  
  /**
   * Implementierung des Timer-Tasks.
   */
  private static class SchedulerTask extends TimerTask
  {
    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      reload();
    }
  }
}
