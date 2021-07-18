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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Versionsdatensatzes.
 */
public class VersionImpl extends AbstractDBObject implements Version
{

  /**
   * ct
   * @throws RemoteException
   */
  public VersionImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "version";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Version#getName()
   */
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Version#getVersion()
   */
  public int getVersion() throws RemoteException
  {
    // Wir fangen bei 0 an mit dem zaehlen
    Integer i = (Integer) getAttribute("version");
    return i == null ? 0 : i.intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Version#setVersion(int)
   */
  public void setVersion(int newVersion) throws RemoteException
  {
    if (newVersion < 0)
      throw new RemoteException("version cannot be smaller than zero");
    setAttribute("version",new Integer(newVersion));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Version#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException
  {
    setAttribute("name",name);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    try
    {
      if (getName() == null || getName().length() == 0)
      {
        throw new ApplicationException(i18n.tr("Keine Bezeichnung für die Version angegeben"));
      }
    }
    catch (RemoteException re)
    {
      Logger.error("error while checking version",re);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen der Version"));
    }
    super.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException
  {
    setVersion(getVersion()); // speichert automatisch die Startnummer
    super.insert();
  }

}

/*********************************************************************
 * $Log: VersionImpl.java,v $
 * Revision 1.2  2007/12/11 00:33:35  willuhn
 * @N Scharfschaltung des neuen Update-Prozesses
 *
 * Revision 1.1  2007/12/06 17:57:21  willuhn
 * @N Erster Code fuer das neue Versionierungs-System
 *
 **********************************************************************/