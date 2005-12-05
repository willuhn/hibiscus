/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/UmsatzZuordnungImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/12/05 17:20:40 $
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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.rmi.UmsatzZuordnung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer einzelnen Umsatz-Zuordnung.
 */
public class UmsatzZuordnungImpl extends AbstractDBObject implements
    UmsatzZuordnung
{

  private I18N i18n = null;
  
  /**
   * @throws java.rmi.RemoteException
   */
  public UmsatzZuordnungImpl() throws RemoteException
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "umsatzzuordnung";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "umsatztyp_id";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzZuordnung#getUmsatz()
   */
  public Umsatz getUmsatz() throws RemoteException
  {
    return (Umsatz) getAttribute("umsatz_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzZuordnung#getUmsatzTyp()
   */
  public UmsatzTyp getUmsatzTyp() throws RemoteException
  {
    return (UmsatzTyp) getAttribute("umsatztyp_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzZuordnung#setUmsatz(de.willuhn.jameica.hbci.rmi.Umsatz)
   */
  public void setUmsatz(Umsatz umsatz) throws RemoteException
  {
    setAttribute("umsatz_id",umsatz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzZuordnung#setUmsatzTyp(de.willuhn.jameica.hbci.rmi.UmsatzTyp)
   */
  public void setUmsatzTyp(UmsatzTyp typ) throws RemoteException
  {
    setAttribute("umsatztyp_id",typ);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("umsatz_id".equals(arg0))
      return Umsatz.class;
    if ("umsatztyp_id".equals(arg0))
      return UmsatzTyp.class;
    
    return super.getForeignObject(arg0);
  }
  
  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getUmsatz() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Umsatz aus"));
      if (getUmsatzTyp() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Umsatztyp aus"));
    }
    catch (RemoteException e)
    {
      Logger.error("unable to check umsatzzuordnung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen der Umsatzzuordnung"));
    }
    
    super.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    this.insertCheck();
  }
}


/*********************************************************************
 * $Log: UmsatzZuordnungImpl.java,v $
 * Revision 1.1  2005/12/05 17:20:40  willuhn
 * @N Umsatz-Filter Refactoring
 *
 **********************************************************************/