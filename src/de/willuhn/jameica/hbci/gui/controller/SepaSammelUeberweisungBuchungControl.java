/*****************************************************************************
 * 
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 ****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;

/**
 * Controller fuer den Dialog "Buchung einer SEPA-Sammelueberweisung bearbeiten".
 */
public class SepaSammelUeberweisungBuchungControl extends AbstractSepaSammelTransferBuchungControl<SepaSammelUeberweisungBuchung>
{
	// Fach-Objekte
	private SepaSammelUeberweisungBuchung buchung = null;
	
  /**
   * ct.
   * @param view
   */
  public SepaSammelUeberweisungBuchungControl(AbstractView view)
  {
    super(view);
		
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferBuchungControl#getBuchung()
   */
  public SepaSammelUeberweisungBuchung getBuchung()
	{
		if (this.buchung != null)
			return this.buchung;
		this.buchung = (SepaSammelUeberweisungBuchung) this.getCurrentObject();
		return this.buchung;
	}
}
