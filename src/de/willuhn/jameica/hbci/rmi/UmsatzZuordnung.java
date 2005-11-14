/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/UmsatzZuordnung.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/11/14 23:47:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

/**
 * Das Mapping eines Umsatzes auf eine Umsatz-Kategorie.
 */
public interface UmsatzZuordnung extends DBObject
{
  /**
   * Liefert den Umsatz der Zuordnung.
   * @return Umsatz.
   * @throws RemoteException
   */
  public Umsatz getUmsatz() throws RemoteException;
  
  /**
   * Liefert den Umsatztyp.
   * @return Umsatz-Typ.
   * @throws RemoteException
   */
  public UmsatzTyp getUmsatzTyp() throws RemoteException;
}


/*********************************************************************
 * $Log: UmsatzZuordnung.java,v $
 * Revision 1.1  2005/11/14 23:47:21  willuhn
 * @N added first code for umsatz categories
 *
 **********************************************************************/