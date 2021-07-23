/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
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

  @Override
  public SepaSammelUeberweisungBuchung getBuchung()
	{
		if (this.buchung != null)
			return this.buchung;
		this.buchung = (SepaSammelUeberweisungBuchung) this.getCurrentObject();
		return this.buchung;
	}
}
