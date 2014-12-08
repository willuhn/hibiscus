/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeJobProvider#getPropertyNames(de.willuhn.jameica.hbci.rmi.Konto)
   */
  @Override
  public List<String> getPropertyNames(Konto k)
  {
    return null;
  }
}
