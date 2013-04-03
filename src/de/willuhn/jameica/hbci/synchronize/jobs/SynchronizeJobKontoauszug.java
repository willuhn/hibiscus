/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

/**
 * Interface fuer den Job zum Abrufen der Kontoauszuege.
 */
public interface SynchronizeJobKontoauszug extends SynchronizeJob
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

}


