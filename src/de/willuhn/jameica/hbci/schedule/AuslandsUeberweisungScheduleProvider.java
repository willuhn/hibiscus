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
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;

/**
 * Implementierung eines Schedule-Providers fuer offene SEPA-Ueberweisungen.
 */
@Lifecycle(Type.REQUEST)
public class AuslandsUeberweisungScheduleProvider extends AbstractTransferScheduleProvider<AuslandsUeberweisung>
{
  /**
   * @see de.willuhn.jameica.hbci.schedule.ScheduleProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("SEPA-Überweisungen");
  }
}

/**********************************************************************
 * $Log: AuslandsUeberweisungScheduleProvider.java,v $
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/