/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

/**
 * Standard-Job-Implementierung zum Abrufen der Kontoauszuege.
 */
public class SynchronizeJobKontoauszug extends AbstractSynchronizeJob
{
  /**
   * Context-Key fuer das forcierte Abrufen des Saldos. Auch dann, wenn es
   * in den Synchronisierungsoptionen deaktiviert ist. Der Wert des Keys
   * muss vom Typ {@link Boolean} sein.
   */
  public final static String CTX_FORCE_SALDO = "ctx.konto.saldo.force";

  /**
   * Context-Key fuer das forcierte Abrufen der Umsaetze. Auch dann, wenn es
   * in den Synchronisierungsoptionen deaktiviert ist. Der Wert des Keys
   * muss vom Typ {@link Boolean} sein.
   */
  public final static String CTX_FORCE_UMSATZ = "ctx.konto.umsatz.force";

  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring()
  {
    return true;
  }
}
