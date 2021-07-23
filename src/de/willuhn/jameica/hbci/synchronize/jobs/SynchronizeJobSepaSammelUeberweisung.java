/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;


/**
 * Standard-Job zum Senden einer SEPA-Sammelueberweisung.
 */
public class SynchronizeJobSepaSammelUeberweisung extends AbstractSynchronizeJob
{
  @Override
  public boolean isRecurring()
  {
    return false;
  }

}


