/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Importer.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/04/23 18:07:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.InputStream;
import java.rmi.RemoteException;

import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Basis-Interface aller Importer.
 */
public interface Importer extends IO
{

  /**
   * Importiert Daten aus dem InputStream.
   * @param context Context, der dem Importer hilft, den Zusammenhang zu erkennen,
   * in dem er aufgerufen wurde. Das kann zum Beispiel ein Konto sein.
   * @param format das vom User ausgewaehlte Import-Format.
   * @param is der Stream, aus dem die Daten gelesen werden.
   * @param monitor ein Monitor, an den der Importer Ausgaben ueber seinen
   * Bearbeitungszustand ausgeben kann.
   * @param force Ob nicht-fatale Fehler übersprungen werden.
   * Der Importer muss den Import-Stream selbst schliessen!
   * @throws RemoteException
   * @throws ApplicationException 
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, boolean force) throws RemoteException, ApplicationException;

}


/*********************************************************************
 * $Log: Importer.java,v $
 * Revision 1.6  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.5  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.4  2006/01/23 00:36:29  willuhn
 * @N Import, Export und Chipkartentest laufen jetzt als Background-Task
 *
 * Revision 1.3  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 * Revision 1.2  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 * Revision 1.1  2005/06/08 16:48:54  web0
 * @N new Import/Export-System
 *
 *********************************************************************/
