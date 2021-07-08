/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

/**
 * Interface fuer eine einzelne Buchung einer SEPA-Sammelueberweisung.
 */
public interface SepaSammelUeberweisungBuchung extends SepaSammelTransferBuchung<SepaSammelUeberweisung>, Duplicatable
{
}
