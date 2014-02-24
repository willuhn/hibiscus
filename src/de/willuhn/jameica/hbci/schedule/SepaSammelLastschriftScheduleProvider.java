/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.schedule;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;

/**
 * Implementierung eines Schedule-Providers fuer offene SEPA-Sammellastschriften.
 */
@Lifecycle(Type.REQUEST)
public class SepaSammelLastschriftScheduleProvider extends AbstractTransferScheduleProvider<SepaSammelLastschrift>
{
  /**
   * @see de.willuhn.jameica.hbci.schedule.ScheduleProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("SEPA-Sammellastschriften");
  }
}
