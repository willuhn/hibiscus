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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.DBReminder;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines in der Datenbank gespeicherten Reminders.
 */
public class DBReminderImpl extends AbstractHibiscusDBObject implements DBReminder
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws RemoteException
   */
  public DBReminderImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (StringUtils.trimToNull(this.getUUID()) == null)
        throw new ApplicationException(i18n.tr("Keine UUID angegeben."));

      if (this.getReminder() == null)
        throw new ApplicationException(i18n.tr("Kein Reminder angegeben"));
    }
    catch (RemoteException e)
    {
      Logger.error("error while insertcheck", e);
      throw new ApplicationException(i18n.tr("Fehler bei der Prüfung der Daten"));
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    this.insertCheck();
  }

  @Override
  public String getUUID() throws RemoteException
  {
    return (String) this.getAttribute("uuid");
  }

  @Override
  public void setUUID(String uuid) throws RemoteException
  {
    this.setAttribute("uuid",uuid);
  }

  @Override
  public Reminder getReminder() throws RemoteException
  {
    String serialized = (String) this.getAttribute("content");
    if (serialized == null || serialized.length() == 0)
      return null;

    XMLDecoder decoder = null;
    try
    {
      decoder = new XMLDecoder(new ByteArrayInputStream(serialized.getBytes("UTF-8")));
      return (Reminder) decoder.readObject();
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to unserialize reminder",e);
    }
    finally
    {
      if (decoder != null)
        decoder.close();
    }
  }

  @Override
  public void setReminder(Reminder reminder) throws RemoteException
  {
    if (reminder == null)
    {
      this.setAttribute("content",null);
      return;
    }
    
    XMLEncoder encoder = null;
    try
    {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      encoder = new XMLEncoder(os);
      encoder.writeObject(reminder);
      encoder.close();
      this.setAttribute("content",os.toString("UTF-8"));
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to serialize reminder",e);
    }
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "uuid";
  }

  @Override
  protected String getTableName()
  {
    return "reminder";
  }
}



/**********************************************************************
 * $Log: DBReminderImpl.java,v $
 * Revision 1.1  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 **********************************************************************/