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
 * Standard-Job-Implementierung zum Abrufen der elektronischen Kontoauszugs im PDF-Format.
 */
public class SynchronizeJobKontoauszugPdf extends AbstractSynchronizeJob
{
  /**
   * Context-Key fuer das forcierte Abrufen der Kontoauszuege. Auch dann, wenn es
   * in den Synchronisierungsoptionen deaktiviert ist. Der Wert des Keys
   * muss vom Typ {@link Boolean} sein.
   */
  public final static String CTX_FORCE = "ctx.konto.kontoauszugpdf.force";

  @Override
  public boolean isRecurring()
  {
    return true;
  }
}


