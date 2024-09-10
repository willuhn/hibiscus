/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;


/**
 * Event-Objekt.
 */
public class ImportEvent
{
  /**
   * Das zugehoerige Fachobjekt.
   */
  public Object data = null;
  
  /**
   * Optionales Context-Objekt des Imports.
   */
  public Object context = null;
  
  /**
   * Das CSV-Profil.
   */
  public Profile profile = null; 
}
