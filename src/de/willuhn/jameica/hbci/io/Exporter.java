/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Exporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/08 16:48:54 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.OutputStream;
import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;

/**
 * Basis-Interface aller Export-Formate.
 * Alle Klassen, die dieses Interface implementieren, werden automatisch
 * von Hibiscus erkannt und dem Benutzer als Export-Moeglichkeit angeboten
 * insofern sie einen parameterlosen Konstruktor mit dem Modifier "public"
 * besitzen (Java-Bean-Konvention).
 */
public interface Exporter extends IO
{
  /**
   * Exportiert die genannte Umsaetze in den angegebenen OutputStream.
   * @param umsaetze die zu exportierenden Umsaetze.
   * @param os der Ziel-Ausgabe-Stream
   * @throws RemoteException
   * @throws ApplicationException 
   */
  public void export(Umsatz[] umsaetze, OutputStream os) throws RemoteException, ApplicationException;
}


/**********************************************************************
 * $Log: Exporter.java,v $
 * Revision 1.1  2005/06/08 16:48:54  web0
 * @N new Import/Export-System
 *
 * Revision 1.1  2005/06/02 21:48:44  web0
 * @N Exporter-Package
 * @N CSV-Exporter
 *
 **********************************************************************/