/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Checksum.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/17 16:28:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Klassen, die dieses Interface implementieren, besitzen eine
 * Funktion, welche eine fuer dieses Objekt eindeutige Checksumme
 * zurueckliefert.
 */
public interface Checksum extends Remote
{

	/**
	 * Liefert die Checksumme des Objektes.
   * @return Checksumme.
   * @throws RemoteException
   */
  public long getChecksum() throws RemoteException;

}


/**********************************************************************
 * $Log: Checksum.java,v $
 * Revision 1.1  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 **********************************************************************/