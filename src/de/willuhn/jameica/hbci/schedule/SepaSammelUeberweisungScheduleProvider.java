/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.schedule;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;

/**
 * Implementierung eines Schedule-Providers fuer offene SEPA-Sammelueberweisungen.
 */
@Lifecycle(Type.REQUEST)
public class SepaSammelUeberweisungScheduleProvider extends AbstractTransferScheduleProvider<SepaSammelUeberweisung>
{
  /**
   * @see de.willuhn.jameica.hbci.schedule.ScheduleProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("SEPA-Sammelüberweisungen");
  }
}
