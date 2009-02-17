/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/AuslandsUeberweisung.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/02/17 00:00:02 $
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



/**
 * Bildet eine Auslands-Ueberweisung ab.
 */
public interface AuslandsUeberweisung extends BaseUeberweisung, Duplicatable
{
  /**
   * Liefert den Namen des Geldinstituts.
   * @return Name des Geldinstituts.
   * @throws RemoteException
   */
  public String getGegenkontoInstitut() throws RemoteException;

  /**
   * Speichert den Namen des Geldinstituts.
   * @param name Name des Geldinstituts.
   * @throws RemoteException
   */
  public void setGegenkontoInstitut(String name) throws RemoteException;

}


/**********************************************************************
 * $Log: AuslandsUeberweisung.java,v $
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/