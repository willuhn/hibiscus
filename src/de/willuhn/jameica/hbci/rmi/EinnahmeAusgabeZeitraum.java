/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.util.Date;

import de.willuhn.datasource.GenericObject;

/**
 * Interface fuer einen Zeitraum von Einnahmen und Ausgaben.
 */
public interface EinnahmeAusgabeZeitraum extends GenericObject
{
  /**
   * Liefert das Start-Datum.
   * @return das Start-Datum.
   */
  public Date getStartdatum();

  /**
   * Liefert das End-Datum.
   * @return das End-Datum.
   */
  public Date getEnddatum();
  
  /**
   * Liefert den Beschreibungstext der Zeile.
   * @return der Beschreibungstext der Zeile.
   */
  public String getText();

  /**
   * Liefert die Einnahmen.
   * @return die Einnahmen.
   */
  public double getEinnahmen();

  /**
   * Liefert die Ausgaben.
   * @return die Ausgaben.
   */
  public double getAusgaben();

}
