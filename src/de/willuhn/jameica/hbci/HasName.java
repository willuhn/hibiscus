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
 * Interface f�r alle Objekte, die einen sprechenden Namen ausgeben k�nnen sollen.
 */
public interface HasName
{
  /**
   * Liefert einen sprechenden Namen f�r die Objektinstanz.
   *
   * @return Name oder leerer String (niemals {@code NULL})
   */
  String getName();
}
