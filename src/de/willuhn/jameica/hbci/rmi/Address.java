/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Address.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/04/23 18:07:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Basis-Interface fuer einen Adressbuch-Eintrag.
 * Die deutschen Methoden-Namen (trotz englischem Interface-Namen)
 * sind ein Zugestaendnis an die Hibiscus-API.
 */
public interface Address extends Remote
{
  /**
   * Liefert die Kontonummer.
   * @return Kontonummer.
   * @throws RemoteException
   */
  public String getKontonummer() throws RemoteException;
  
  /**
   * Liefert die BLZ.
   * @return BLZ.
   * @throws RemoteException
   */
  public String getBLZ() throws RemoteException;
  
  /**
   * Liefert den Namen.
   * @return Name.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Liefert einen zusaetzlichen Kommentar fuer den Adressbuch-Eintrag.
   * @return Kommentar.
   * @throws RemoteException
   */
  public String getKommentar() throws RemoteException;
  
  /**
   * Liefert eine Liste von Transfers von und/oder an diese Adresse.
   * Ob es sich hierbei um Umsaetze, Ueberweisungen oder Lastschriften handelt,
   * ist nebensaechlich.
   * @return Liste von Transfers von und/oder an diese Adresse.
   * Die Objekte muessen vom Typ {@link Transfer} sein.
   * @throws RemoteException
   */
  public List getTransfers() throws RemoteException;
}


/*********************************************************************
 * $Log: Address.java,v $
 * Revision 1.2  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.1  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 **********************************************************************/