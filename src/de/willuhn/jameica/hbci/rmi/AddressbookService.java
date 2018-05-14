/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.Service;

/**
 * Interface fuer den Adressbuch-Service.
 * Hintergrund: Hibiscus soll einmal mehrere Adressbuecher
 * unterstuetzen koennen. Mit diesem Service hier koennen nun
 * die vorhandenen Adressbuecher abgefragt werden. Ein
 * Adressbuch muss das Interface "rmi.Addressbook" implementieren,
 * um automatisch erkannt zu werden.
 * Der Adressbuch-Service implementiert selbst ebenfalls das
 * Interface <code>Addressbook</code>. Hiermit ist es moeglich,
 * <b>alle</b> Adressbuecher wie ein einziges abzufragen.
 */
public interface AddressbookService extends Service, Addressbook
{
  /**
   * Liefert die Liste aller gefundenen Adressbuecher.
   * @return Liste der Adressbuecher.
   * Die Funktion liefert niemals <code>null</code> und auch
   * nie ein leeres Array. Denn da <code>AddressbookService</code>
   * selbst ebenfalls das <code>Addressbook</code>-Interface
   * implementiert, wird mindestens dieses zurueckgeliefert.
   * Allerdings wuerde es in diesem Fall keine Adressen finden,
   * da ja keine tatsaechlichen "Backend"-Adressbuecher existieren.
   * Zumindest das Hibiscus-eigene Adressbuch sollte aber immer
   * enthalten sein. 
   * @throws RemoteException
   */
  public Addressbook[] getAddressbooks() throws RemoteException;
  
  /**
   * Liefert true, wenn neben dem Hibiscus-eigenen Adressbuch noch weitere gefunden wurden.
   * In diesem Fall koennte dem Benutzer z.Bsp. ein Auswahl-Dialog
   * angezeigt werden, in dem er das gewuenschte Adressbuch auswaehlen kann.
   * @return true, wenn weitere Adressbuecher existieren.
   * @throws RemoteException
   */
  public boolean hasExternalAddressbooks() throws RemoteException;
}


/*********************************************************************
 * $Log: AddressbookService.java,v $
 * Revision 1.1  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 **********************************************************************/