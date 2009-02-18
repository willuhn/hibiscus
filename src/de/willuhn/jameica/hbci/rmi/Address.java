/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Address.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/02/18 00:35:54 $
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
   * @deprecated Bitte stattdessen getBlz() verwenden.
   */
  @Deprecated
  public String getBLZ() throws RemoteException;

  /**
   * Liefert die BLZ.
   * @return BLZ.
   * @throws RemoteException
   * BUGZILLA 534
   */
  public String getBlz() throws RemoteException;

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
}


/*********************************************************************
 * $Log: Address.java,v $
 * Revision 1.6  2009/02/18 00:35:54  willuhn
 * @N Auslaendische Bankverbindungen im Adressbuch
 *
 * Revision 1.5  2008/11/05 09:26:40  willuhn
 * @B tag typo
 *
 * Revision 1.4  2008/01/09 23:32:54  willuhn
 * @B Bug 534
 *
 * Revision 1.3  2007/04/23 21:03:48  willuhn
 * @R "getTransfers" aus Address entfernt - hat im Adressbuch eigentlich nichts zu suchen
 *
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