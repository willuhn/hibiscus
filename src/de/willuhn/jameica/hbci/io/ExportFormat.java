/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/ExportFormat.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/30 23:52:42 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import org.eclipse.swt.graphics.Image;

/**
 * Basis-Interface fuer alle Export-Formate.
 * Jeder Exporter kann beliebig viele Export-Formate unterstuetzen.
 */
public interface ExportFormat
{
  /**
   * Liefert einen sprechenden Namen fuer das Export-Format.
   * Zum Beispiel &quotCSV-Datei&quot;
   * @return Sprechender Name des Export-Formats.
   */
  public String getName();

  /**
   * Liefert die Datei-Endung des Formats.
   * Angabe bitte ohne Punkt. Also zum Beispiel "csv" statt ".csv".
   * @return Datei-Endung.
   */
  public String getFileExtension();

  /**
   * Angabe eines optionalen Screenshots.
   * Damit kann der User schon vorher ungefaehr erkennen, wie das
   * exportierte Resultat aussehen soll.
   * @return Screenshot oder <code>null</code>.
   */
  public Image getScreenshot();
}


/*********************************************************************
 * $Log: ExportFormat.java,v $
 * Revision 1.1  2005/06/30 23:52:42  web0
 * @N export via velocity
 *
 **********************************************************************/