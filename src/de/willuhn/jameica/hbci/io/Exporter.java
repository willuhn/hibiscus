/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Exporter.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/07/04 12:41:39 $
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
   * @param format das vom User ausgewaehlte Export-Format.
   * @param objectType Art der zu exportierenden Objekte.
   * @param objects die zu exportierenden Objekte.
   * @param os der Ziel-Ausgabe-Stream
   * @throws RemoteException
   * @throws ApplicationException 
   */
  public void export(ExportFormat format, Class objectType, GenericObject[] objects, OutputStream os) throws RemoteException, ApplicationException;

  /**
   * Liefert eine Liste der von diesem Exporter unterstuetzten Export-Formate.
   * @param objectType Art der zu exportierenden Objekte.
   * Z.Bsb.: Umsatz.class oder SammellastBuchung.class.
   * @return Liste der Export-Formate.
   */
  public ExportFormat[] getExportFormats(Class objectType);

}


/**********************************************************************
 * $Log: Exporter.java,v $
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