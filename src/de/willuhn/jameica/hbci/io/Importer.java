/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Importer.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/01/18 00:51:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.InputStream;
import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.util.ApplicationException;

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
   * Der Importer muss den Import-Stream selbst schliessen!
   * @throws RemoteException
   * @throws ApplicationException 
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is) throws RemoteException, ApplicationException;

}


/*********************************************************************
 * $Log: Importer.java,v $
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