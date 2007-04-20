/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Addressbook.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/04/20 14:55:31 $
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

import de.willuhn.datasource.GenericIterator;

/**
 * Interface fuer ein einzelnes Adressbuch.
 * Alle Klassen, die dieses Interface implementieren, werden automatisch von
 * Hibiscus erkannt.
 * Alle Implementierungen muessen einen parameterlosen Konstruktor
 * besitzen (Bean-Spezifikation), um via Reflection instanziiert werden zu koennen.
 */
public interface Addressbook extends Remote
{
  /**
   * Liefert einen sprechenden Namen fuer das Adressbuch.
   * @return Name des Adressbuches.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Sucht nach Adressen und liefert die gefundenen zurueck.
   * Es ist der jeweiligen Implementierung des Adressbuches ueberlassen,
   * in welchen Feldern/Attributen der Adressdaten sie nach dem uebergebenen Text
   * sucht.
   * Wird kein Text uebergeben, kann das Adressbuch selbst entscheiden,
   * ob es alle Adressen zurueckliefert oder gar keine.
   * @param text der Suchtext.
   * @return Liste der gefundenen Adressen.
   * Die Objekte der Liste muessen vom Typ <code>Address</code> sein.
   * Die Funktion darf auch <code>null</code> zurueckliefern, wenn keine Adressen gefunden wurden.
   * @throws RemoteException
   * @see {@link AddressbookService#findAddresses(String)}
   */
  public GenericIterator findAddresses(String text) throws RemoteException;
  
  /**
   * Prueft, ob im Adressbuch eine Adresse <b>mit diesen Eigenschaften</b> enthalten ist.
   * @param address die gesuchte Adresse.
   * @return die Adresse mit den gleichen Eigenschaften aus dem Adressbuch oder <code>null</code>.
   * @throws RemoteException
   */
  public Address contains(Address address) throws RemoteException;
}


/*********************************************************************
 * $Log: Addressbook.java,v $
 * Revision 1.2  2007/04/20 14:55:31  willuhn
 * @C s/findAddress/findAddresses/
 *
 * Revision 1.1  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 **********************************************************************/