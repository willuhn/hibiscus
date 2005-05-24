/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/OPPattern.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/24 23:30:03 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.filter.Pattern;

/**
 * Interface fuer ein einzelnes Filter-Kriterium fuer einen Eintrag
 * in der OP-Verwaltung.
 */
public interface OPPattern extends Pattern
{
  /**
   * Liefert den offenen Posten, zu dem der Pattern gehoert.
   * @return der offene Posten.
   * @throws RemoteException
   */
  public OffenerPosten getOffenerPosten() throws RemoteException;

  /**
   * Legt den offenen Posten fest, zu dem der Pattern gehoert.
   * @param p offener Posten.
   * @throws RemoteException
   */
  public void setOffenerPosten(OffenerPosten p) throws RemoteException;
  
  
}


/**********************************************************************
 * $Log: OPPattern.java,v $
 * Revision 1.1  2005/05/24 23:30:03  web0
 * @N Erster Code fuer OP-Verwaltung
 *
 **********************************************************************/