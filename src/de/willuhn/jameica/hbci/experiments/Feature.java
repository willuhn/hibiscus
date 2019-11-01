/**********************************************************************
 *
 * Copyright (c) 2019 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.experiments;

/**
 * Interface fuer ein aktivierbares/deaktivierbares Feature.
 */
public interface Feature
{
  /**
   * Liefert einen eindeutigen Namen fuer das Feature.
   * @return eindeutiger Name fuer das Feature.
   */
  public String getName();
  
  /**
   * Liefert die Beschreibung des Features.
   * @return die Beschreibung des Features.
   */
  public String getDescription();
  
  /**
   * Liefert true, wenn das Feature per Default aktiv sein soll.
   * @return true, wenn das Feature per Default aktiv sein soll.
   */
  public boolean getDefault();
  
  /**
   * Aktiviert/deaktiviert das Feature.
   * Wird automatisch beim Start von Hibiscus aufgerufen, um den gewuenschten Feature-Zustand herzustellen.
   * @param enabled true, wenn das Feature aktiviert sein soll.
   */
  public void setEnabled(boolean enabled);
}


