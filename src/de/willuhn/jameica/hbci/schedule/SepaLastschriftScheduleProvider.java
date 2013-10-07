/**********************************************************************
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
  /**
   * @see de.willuhn.jameica.hbci.schedule.ScheduleProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("SEPA-Lastschriften");
  }
}
