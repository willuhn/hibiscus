/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts;

import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.HasName;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer einen Account-Provider.
 * Wird typischerweise einmal pro SynchronizeBackend implementiert.
 */
public interface AccountProvider extends HasName
{
  /**
   * Liefert einen sprechenden Namen fuer den Provider.
   * @return sprechender Name fuer den Provider.
   */
  @Override
  public String getName();
  
  /**
   * Liefert ein vorausgefuelltes Info-Panel fuer den Bankzugang.
   * @return ein vorausgefuelltes Info-Panel fuer den Bankzugang.
   */
  public InfoPanel getInfo();
  
  /**
   * Startet den Assistenten zur Erzeugung eines neuen Bankzugangs.
   * @throws ApplicationException
   */
  public void create() throws ApplicationException;
}


