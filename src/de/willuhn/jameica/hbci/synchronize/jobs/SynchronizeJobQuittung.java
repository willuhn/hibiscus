/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;



/**
 * Standard-Job-Implementierung zum Senden einer Empfangsquittung an die Bank.
 */
public class SynchronizeJobQuittung extends AbstractSynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob#isRecurring()
   */
  public boolean isRecurring()
  {
    return false;
  }
}


