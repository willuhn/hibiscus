/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.schedule;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;

/**
 * Implementierung eines Schedule-Providers fuer offene SEPA-Lastschriften.
 */
@Lifecycle(Type.REQUEST)
public class SepaLastschriftScheduleProvider extends AbstractTransferScheduleProvider<SepaLastschrift>
{
  @Override
  public String getName()
  {
    return i18n.tr("SEPA-Lastschriften");
  }
}
