/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

/**
 * Interface für alle Objekte, die einen sprechenden Namen ausgeben können sollen.
 */
public interface HasName
{
  /**
   * Liefert einen sprechenden Namen für die Objektinstanz.
   *
   * @return Name oder leerer String (niemals {@code NULL})
   */
  String getName();
}
