/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

/**
 * Interface fuer den Job zum Loeschen eines Dauerauftrages.
 */
public interface SynchronizeJobDauerauftragDelete extends SynchronizeJob
{
  /**
   * Context-Key fuer das Ziel-Datum zum Loeschen des Dauerauftrages.
   * Der Wert des Keys muss vom Typ {@link java.util.Date} sein.
   */
  public final static String CTX_DATE = "ctx.da.delete.date";
}


