/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;


/**
 * Standard-Job zum Senden einer SEPA-Sammelueberweisung.
 */
public class SynchronizeJobSepaSammelUeberweisung extends AbstractSynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring()
  {
    return false;
  }

}


