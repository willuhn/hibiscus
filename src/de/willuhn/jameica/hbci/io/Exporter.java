/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Exporter.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/01/18 00:51:01 $
 * $Author: willuhn $
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

import de.willuhn.datasource.GenericObject;
import de.willuhn.util.ApplicationException;

/**
 * Basis-Interface aller Exporter.
 * Alle Klassen, die dieses Interface implementieren, werden automatisch
 * von Hibiscus erkannt und dem Benutzer als Export-Moeglichkeit angeboten
 * insofern sie einen parameterlosen Konstruktor mit dem Modifier "public"
 * besitzen (Java-Bean-Konvention).
 */
public interface Exporter extends IO
{
  /**
   * Exportiert die genannten Objekte in den angegebenen OutputStream.
   * @param objects die zu exportierenden Objekte.
   * @param format das vom User ausgewaehlte Export-Format.
   * @param os der Ziel-Ausgabe-Stream.
   * Der Exporter muss den OutputStream selbst schliessen!
   * @throws RemoteException
   * @throws ApplicationException 
   */
  public void doExport(GenericObject[] objects, IOFormat format, OutputStream os) throws RemoteException, ApplicationException;
}


/**********************************************************************
 * $Log: Exporter.java,v $
 * Revision 1.5  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 * Revision 1.4  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 * Revision 1.3  2005/07/04 12:41:39  web0
 * @B bug 90
 *
 * Revision 1.2  2005/06/30 23:52:42  web0
 * @N export via velocity
 *
 * Revision 1.1  2005/06/08 16:48:54  web0
 * @N new Import/Export-System
 *
 * Revision 1.1  2005/06/02 21:48:44  web0
 * @N Exporter-Package
 * @N CSV-Exporter
 *
 **********************************************************************/