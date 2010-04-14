/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/Verwendungszweck.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/04/14 17:44:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;

/**
 * Eine Zeile erweiterter Verwendungszweck.
 * @deprecated Existiert nur noch zu Migrationszwecken (update0012)
 */
public interface Verwendungszweck extends DBObject
{
  /**
   * Liefert den Text des Verwendungszweckes.
   * @return der Text.
   * @throws RemoteException
   */
  public String getText() throws RemoteException;
  
  /**
   * Speichert den Text des Verwendungszweckes.
   * @param text
   * @throws RemoteException
   */
  public void setText(String text) throws RemoteException;
  
  /**
   * Weist diesem Verwendungszweck den Auftrag zu.
   * @param t der Auftrag.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void setTransfer(Transfer t) throws RemoteException, ApplicationException;
  
}


/*********************************************************************
 * $Log: Verwendungszweck.java,v $
 * Revision 1.3  2010/04/14 17:44:10  willuhn
 * @N BUGZILLA 83
 *
 * Revision 1.2  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.1  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 **********************************************************************/