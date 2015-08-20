/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci;

import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer eine HBCI-Zugangsvariante.
 */
public interface HBCIVariant
{
  /**
   * Liefert einen sprechenden Namen fuer die HBCI-Variante.
   * @return sprechender Name fuer die HBCI-Variante.
   */
  public String getName();
  
  /**
   * Liefert ein vorausgefuelltes Info-Panel fuer die HBCI-Variante.
   * @return ein vorausgefuelltes Info-Panel fuer die HBCI-Variante.
   */
  public InfoPanel getInfo();
  
  /**
   * Startet den Assistenten zur Erzeugung eines Bankzugangs mit der Variante.
   * @throws ApplicationException
   */
  public void create() throws ApplicationException;
}


