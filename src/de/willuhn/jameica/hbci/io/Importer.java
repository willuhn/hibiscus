/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.InputStream;
import java.rmi.RemoteException;

import de.willuhn.jameica.system.BackgroundTask;
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
   * Der Importer muss den Import-Stream selbst schliessen!
   * @param t der {@link BackgroundTask}
   * @throws RemoteException
   * @throws ApplicationException 
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask t) throws RemoteException, ApplicationException;
}
