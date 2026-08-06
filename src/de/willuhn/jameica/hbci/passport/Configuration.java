/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passport;

import java.rmi.RemoteException;

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer eine einzelne Passport-Konfiguration.
 */
public interface Configuration
{
  /**
   * Liefert einen Beschreibungstext fuer die Konfiguration.
   * @return Beschreibungstext.
   */
  public String getDescription();
  
  /**
   * Liefert einen Identifier für die Konfiguration.
   * @return der Identifier.
   */
  public String getIdentifier();

  /**
   * Liefert die Klasse des Konfigurationsdialogs.
   * Oeffnet der Anwender den Konfigurations-Dialog durch
   * Doppelklick auf die Liste der vorhandenen Konfigurationen,
   * dann wird eine Instanz dieser Klasse erzeugt und
   * in der GUI angezeigt. Als Referenz kann hierzu
   * <i>de.willuhn.jameica.passports.ddv.View</i> dienen.
   * @return Die Klasse des Konfig-Dialogs.
   * Muss von <code>AbstractView</code> abgeleitet sein.
   * @throws RemoteException
   */
  public Class getConfigDialog() throws RemoteException;
  
  /**
   * Loescht die Konfiguration.
   * @throws ApplicationException
   */
  public void delete() throws ApplicationException;

}
