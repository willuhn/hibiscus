/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/print/Attic/PrintSupport.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/07 17:29:19 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.rmi.RemoteException;

import de.willuhn.util.ApplicationException;
import net.sf.paperclips.PrintJob;

/**
 * Basis-Interface zur Erzeugung von Druck-Jobs.
 * @param <T> der Typ der zu druckenden Daten.
 */
public interface PrintSupport<T>
{
  /**
   * Erzeugt den Druck-Job.
   * @param object die zu druckenden Daten.
   * @return der Druck-Job.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public PrintJob print(T object) throws ApplicationException, RemoteException;
}



/**********************************************************************
 * $Log: PrintSupport.java,v $
 * Revision 1.1  2011/04/07 17:29:19  willuhn
 * @N Test-Code fuer Druck-Support
 *
 **********************************************************************/