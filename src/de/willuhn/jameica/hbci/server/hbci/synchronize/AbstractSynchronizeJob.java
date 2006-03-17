/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/AbstractSynchronizeJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/03/17 00:51:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.synchronize;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung aller SynchronizeJobs,
 */
public abstract class AbstractSynchronizeJob implements SynchronizeJob
{
  I18N i18n = null;
  private GenericObject context = null;

  /**
   * ct.
   * @param context das Fachobjekt, welches zu behandeln ist.
   * Das kann eine Ueberweisung, ein Konto o.ae. sein.
   */
  public AbstractSynchronizeJob(GenericObject context)
  {
    this.context = context;
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
  /**
   * Liefert den Context des Jobs.
   * @return Context.
   */
  GenericObject getContext()
  {
    return this.context;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    return getName();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"name"};
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return getClass().getName() + "." + context.getID();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    if (arg0 == null || !(arg0 instanceof SynchronizeJob))
      return false;
    return getID().equals(arg0.getID());
  }
}


/*********************************************************************
 * $Log: AbstractSynchronizeJob.java,v $
 * Revision 1.1  2006/03/17 00:51:24  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 **********************************************************************/