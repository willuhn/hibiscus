/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Importer.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/01/17 00:22:36 $
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

import de.willuhn.util.ApplicationException;

/**
 * Basis-Interface aller Importer.
 */
public interface Importer extends IO
{

  /**
   * Importiert Daten aus dem InputStream.
   * @param format das vom User ausgewaehlte Import-Format.
   * @param is der Stream, aus dem die Daten gelesen werden.
   * @throws RemoteException
   * @throws ApplicationException 
   */
  public void doImport(IOFormat format, InputStream is) throws RemoteException, ApplicationException;

}


/*********************************************************************
 * $Log: Importer.java,v $
 * Revision 1.2  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 * Revision 1.1  2005/06/08 16:48:54  web0
 * @N new Import/Export-System
 *
 *********************************************************************/