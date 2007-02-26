/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/parser/Attic/UmsatzParser.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/02/26 12:48:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.parser;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Es gibt eine Reihe von Banken, welche die Daten in Umsaetzen nicht
 * korrekt codieren. Damit wir auch die sauber einlesen, kann Hibiscus
 * um einige Spezial-Parser erweitert werden. Sie muessen dann dieses
 * Interface hier implementieren.
 * Die Implementierungen muessen einen parameterlosen Konstruktor besitzen.
 */
public interface UmsatzParser
{
  /**
   * Parst den unformatierten Text und befuellt damit den Umsatz.
   * @param lines die von der Bank gesendeten ungeparsten Zeilen des Umsatzes.
   * @param u der leere Umsatz
   * @throws RemoteException
   */
  public void parse(String[] lines, Umsatz u) throws RemoteException;
}


/*********************************************************************
 * $Log: UmsatzParser.java,v $
 * Revision 1.1  2007/02/26 12:48:23  willuhn
 * @N Spezial-PSD-Parser von Michael Lambers
 *
 **********************************************************************/