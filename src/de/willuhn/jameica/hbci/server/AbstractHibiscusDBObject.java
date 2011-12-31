/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AbstractHibiscusDBObject.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/12/31 13:55:38 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Basis-Code fuer alle Entity-Klassen in Hibiscus.
 */
public abstract class AbstractHibiscusDBObject extends AbstractDBObject implements HibiscusDBObject
{
  /**
   * ct.
   * @throws RemoteException
   */
  public AbstractHibiscusDBObject() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusDBObject#getMeta(java.lang.String, java.lang.String)
   */
  public String getMeta(String name, String defaultValue) throws RemoteException
  {
    if (name == null || name.length() == 0)
      throw new RemoteException("no name given for meta attribute");
    
    if (this.getID() == null)
      return defaultValue;
    
    return DBPropertyUtil.get(this.getPrefix() + name,defaultValue);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusDBObject#setMeta(java.lang.String, java.lang.String)
   */
  public void setMeta(String name, String value) throws RemoteException
  {
    if (name == null || name.length() == 0)
      throw new RemoteException("no name given for meta attribute");
    
    DBPropertyUtil.set(this.getPrefix() + name,value);
  }
  
  /**
   * Liefert den Namens-Prefix fuer die Meta-Angaben der Bean.
   * @return der Namens-Prefix.
   * @throws RemoteException
   */
  private String getPrefix() throws RemoteException
  {
    String id = this.getID();
    if (id == null)
      throw new RemoteException("entity has no id");
    
    return "meta." + this.getTableName() + "." + id + ".";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    if (this.isNewObject())
      return; // Nichts zu loeschen
    
    this.transactionBegin();
    try
    {
      // Delete-Message schicken
      Application.getMessagingFactory().getMessagingQueue("hibiscus." + this.getTableName() + ".delete").sendSyncMessage(new QueryMessage(this));

      // Meta-Daten loeschen - muss NACH der Message erfolgen - sonst fehlen uns dort die Meta-Daten schon
      DBPropertyUtil.deleteAll(this.getPrefix());

      super.delete();
      this.transactionCommit();
    }
    catch (RemoteException e)
    {
      this.transactionRollback();
      throw e;
    }
    catch (ApplicationException e2)
    {
      this.transactionRollback();
      throw e2;
    }
  }
}



/**********************************************************************
 * $Log: AbstractHibiscusDBObject.java,v $
 * Revision 1.3  2011/12/31 13:55:38  willuhn
 * @N Beim Loeschen eines Reminder-faehigen Auftrages wird der Reminder jetzt via Messaging automatisch gleich mit geloescht
 *
 * Revision 1.2  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 * Revision 1.1  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 **********************************************************************/