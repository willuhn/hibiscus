/**********************************************************************
 *
 * Copyright (c) 2019 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh.keyformat;

/**
 * Abstrakte Basis-Implementierung aller Schluesselformate.
 */
public abstract class AbstractKeyFormat implements KeyFormat
{
  @Override
  public int compareTo(KeyFormat o)
  {
    return Integer.compare(this.getOrder(),o.getOrder());
  }
  
  @Override
  public int getOrder()
  {
    // Default-Wert.
    return 0;
  }

}


