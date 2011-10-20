/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/OrderReminderMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/20 16:20:05 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.ReminderMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;

/**
 * Message-Consumer, der benachricht wird, wenn ein Auftrag dupliziert werden soll.
 */
public class OrderReminderMessageConsumer implements MessageConsumer
{
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{ReminderMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    ReminderMessage msg           = (ReminderMessage) message;
    Map<String,Serializable> data = (Map<String,Serializable>) msg.getData();
    Date termin                   = msg.getDate();
    
    MultipleClassLoader loader = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getClassLoader();
    DBService service          = Settings.getDBService();
    
    // 1. der zu duplizierende Auftrag
    Class type = loader.load((String) data.get("order.class"));
    String id  = (String) data.get("order.id");

    Logger.debug("checking, if order " + type.getSimpleName() + ":" + id + " has to be cloned for " + termin);

    // 2. Checken, ob der Auftrag bereits erzeugt wurde. Das kann z.Bsp. der
    //    Fall sein, wenn mehrere Hibiscus-Instanzen die gleiche DB verwenden.
    //    Dann erfolgt das Duplizieren durch den ersten Client, der den Termin
    //    ausfuehrt. Wir suchen also nach einem Auftrag, der auf dem zu duplizierenden
    //    Auftrag basiert und den gesuchten Termin besitzt
    DBIterator list = service.createList(type);
    list.addFilter("termin = ?",termin);
    while (list.hasNext())
    {
      HibiscusDBObject t = (HibiscusDBObject) list.next();
      // Wenn der Auftrag in den Meta-Daten die ID des gesuchten Auftrages hat,
      // dann ist er bereits erzeugt worden.
      String from = t.getMeta("reminder.template",null);
      if (from != null && from.equals(id))
      {
        Logger.debug("allready cloned by " + t.getMeta("reminder.creator","unknown"));
        return;
      }
    }
    
    // 3. Auftrag laden
    Duplicatable template = (Duplicatable) service.createObject(type,id);
    
    // 4. Auftrag clonen und speichern
    HibiscusDBObject order = (HibiscusDBObject) template.duplicate();
    String hostname        = Application.getCallback().getHostname();
    
    try
    {
      order.transactionBegin();
      
      ((Terminable)order).setTermin(termin);      // Ziel-Datum uebernehmen
      order.store();                              // speichern, noetig, weil wir die ID brauchen
      
      // Meta-Daten speichern
      order.setMeta("reminder.creator",hostname);
      order.setMeta("reminder.template",id);

      order.transactionCommit();
      Application.getMessagingFactory().sendSyncMessage(new ImportMessage(order)); //synchron senden, weil wir schon im Messaging-Thread sind
      
      Logger.info("order " + type.getSimpleName() + ":" + id + " cloned by " + hostname + ", id: " + order.getID() + ", date: " + termin);
    }
    catch (Exception e)
    {
      try
      {
        order.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw e;
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // manuell via Manifest registriert.
    return false;
  }

}


/**********************************************************************
 * $Log: OrderReminderMessageConsumer.java,v $
 * Revision 1.1  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 **********************************************************************/