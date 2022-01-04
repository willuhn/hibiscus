/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.reminder;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.DBReminder;
import de.willuhn.jameica.reminder.AbstractReminderStorageProvider;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.system.JameicaException;

/**
 * Implementierung eines Storage-Providers fuer Hibiscus.
 * Ist noetig, damit die Reminder ebenfalls in der Datenbank landen. Denn
 * wenn sich mehrere Hibiscus-Installationen eine gemeinsame SQL-Datenbank teilen,
 * muessen die Reminder auf allen Arbeitsplaetzen zur Verfuegung stehen. Beim
 * Default-Storage-Provider von Jameica waere das nicht der Fall, da der
 * die Daten in einer lokalen Wallet-Datei speichert. In dem Fall waeren
 * die Erinnerungen nur auf dem Arbeitsplatz verfuegbar, auf dem der
 * Reminder angelegt wurde.
 * 
 * Wir verwenden einen Context-Lifecycle, damit wir in Hibiscus die
 * gleiche Instanz verwenden, die auch der Reminder-Service von Jameica nutzt.
 * Ist zwar nicht notwendig, aber sauberer.
 */
@Lifecycle(Type.CONTEXT)
public class ReminderStorageProviderHibiscus extends AbstractReminderStorageProvider
{
  @Override
  public Reminder get(String uuid) throws Exception
  {
    DBReminder r = this.getDBReminder(uuid);
    return r != null ? r.getReminder() : null;
  }

  @Override
  public String add(Reminder reminder) throws Exception
  {
    if (reminder == null)
      throw new JameicaException("no reminder given");
    
    String uuid = this.createUUID();
    
    DBReminder r = (DBReminder) Settings.getDBService().createObject(DBReminder.class,null);
    r.setUUID(uuid);
    r.setReminder(reminder);
    r.store();
    
    return uuid;
  }

  @Override
  public void update(String uuid, Reminder reminder) throws Exception
  {
    if (StringUtils.trimToNull(uuid) == null)
      throw new JameicaException("no uuid given");
    
    if (reminder == null)
      throw new JameicaException("no reminder given");
    
    // Checken, ob wir den schon haben
    DBReminder r = this.getDBReminder(uuid);
    if (r == null)
      throw new ObjectNotFoundException("no reminder found for uuid: " + uuid);
    
    // Daten uebernehmen und speichern
    r.setReminder(reminder);
    r.store();
  }

  @Override
  public Reminder delete(String uuid) throws Exception
  {
    if (StringUtils.trimToNull(uuid) == null)
      throw new JameicaException("no uuid given");
    
    DBReminder r = this.getDBReminder(uuid);
    if (r == null)
      return null; // den gibts gar nicht
    
    Reminder reminder = r.getReminder();
    r.delete();
    
    // den geloeschten Reminder noch zurueckliefern
    return reminder;
  }

  @Override
  public String[] getUUIDs() throws Exception
  {
    return (String[]) Settings.getDBService().execute("select uuid from reminder",null,new ResultSetExtractor() {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        List<String> list = new ArrayList<String>();
        while (rs.next())
        {
          list.add(rs.getString(1));
        }
        
        return list.toArray(new String[list.size()]);
      }
    });
  }

  
  /**
   * Laedt den zugehoerigen Datensatz aus der DB.
   * @param uuid die UUID.
   * @return der Reminder aus der Datenbank oder NULL, wenn er nicht existiert.
   * @throws Exception
   */
  private DBReminder getDBReminder(String uuid) throws Exception
  {
    if (StringUtils.trimToNull(uuid) == null)
      throw new JameicaException("no uuid given");
    
    DBService service = Settings.getDBService();
    DBIterator i = service.createList(DBReminder.class);
    i.addFilter("uuid = ?",uuid);
    if (i.hasNext())
      return (DBReminder) i.next();
    
    return null;
  }
}



/**********************************************************************
 * $Log: ReminderStorageProviderHibiscus.java,v $
 * Revision 1.2  2011/12/27 22:54:55  willuhn
 * @N Reminder loeschen, wenn zugehoeriger Auftrag geloescht wurde
 *
 * Revision 1.1  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 **********************************************************************/