/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/DauerauftragImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/11 16:14:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Dauer-Auftrags.
 */
public class DauerauftragImpl extends AbstractTransferImpl implements Dauerauftrag
{

	private I18N i18n;

  /**
   * ct.
   * @throws RemoteException
   */
  public DauerauftragImpl() throws RemoteException
  {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "dauerauftrag";
  }

  /**
   * @see de.willuhn.datasource.rmi.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException
  {
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getErsteZahlung()
   */
  public Date getErsteZahlung() throws RemoteException
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getLetzteZahlung()
   */
  public Date getLetzteZahlung() throws RemoteException
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#getTurnus()
   */
  public Turnus getTurnus() throws RemoteException
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setErsteZahlung(java.util.Date)
   */
  public void setErsteZahlung(Date datum) throws RemoteException
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setLetzteZahlung(java.util.Date)
   */
  public void setLetzteZahlung(Date datum) throws RemoteException
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Dauerauftrag#setTurnus(de.willuhn.jameica.hbci.rmi.Turnus)
   */
  public void setTurnus(Turnus turnus) throws RemoteException
  {
    // TODO Auto-generated method stub

  }

}


/**********************************************************************
 * $Log: DauerauftragImpl.java,v $
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 **********************************************************************/