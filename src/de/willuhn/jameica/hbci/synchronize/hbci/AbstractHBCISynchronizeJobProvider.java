/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Abstrakte Basis-Klasse der HBCI-Job-Provider.
 */
public abstract class AbstractHBCISynchronizeJobProvider implements HBCISynchronizeJobProvider
{
  @Override
  public List<String> getPropertyNames(Konto k)
  {
    return null;
  }
}
