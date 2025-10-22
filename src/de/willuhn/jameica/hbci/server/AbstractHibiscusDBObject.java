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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
  // Hier speichern wir die Meta-Daten zwischen, die gesetzt wurden, bevor das Objekt eine ID hatte
  private Map<String,String> cachedMeta = new HashMap<>();
  
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

    String id = this.getID();
    if (id == null)
      return this.cachedMeta.getOrDefault(name,defaultValue);

    return DBPropertyUtil.get(DBPropertyUtil.Prefix.META,this.getTableName(),id,name,defaultValue);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusDBObject#setMeta(java.lang.String, java.lang.String)
   */
  public void setMeta(String name, String value) throws RemoteException
  {
    if (name == null || name.length() == 0)
      throw new RemoteException("no name given for meta attribute");

    String id = this.getID();
    if (id == null)
    {
      this.cachedMeta.put(name,value);
      return;
    }

    DBPropertyUtil.set(DBPropertyUtil.Prefix.META,this.getTableName(),id,name,value);
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
      // Muss synchron gemacht werden, damit die Aktionen innerhalb der Transaktion stattfinden
      Application.getMessagingFactory().getMessagingQueue("hibiscus.dbobject.delete").sendSyncMessage(new QueryMessage(this));

      // Meta-Daten loeschen - muss NACH der Message erfolgen - sonst fehlen uns dort die Meta-Daten schon
      DBPropertyUtil.delete(DBPropertyUtil.Prefix.META,this.getTableName(),this.getID());

      super.delete();
      this.transactionCommit();
      this.cachedMeta.clear();
    }
    catch (ApplicationException | RemoteException e)
    {
      this.transactionRollback();
      throw e;
    }
  }
  
  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#store()
   */
  @Override
  public void store() throws RemoteException, ApplicationException
  {
    super.store();

    if (!this.cachedMeta.isEmpty())
    {
      try
      {
        final String id = this.getID();
        for (Entry<String,String> s:this.cachedMeta.entrySet())
        {
          DBPropertyUtil.set(DBPropertyUtil.Prefix.META,this.getTableName(),id,s.getKey(),s.getValue());
        }
      }
      finally
      {
        this.cachedMeta.clear();
      }
    }
    
    // Store-Message schicken
    // Das wird asynchron gemacht, damit es das speichern nicht bremst
    Application.getMessagingFactory().getMessagingQueue("hibiscus.dbobject.store").sendMessage(new QueryMessage(this));
  }
}
