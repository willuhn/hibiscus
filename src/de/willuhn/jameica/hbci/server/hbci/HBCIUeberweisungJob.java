/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUeberweisungJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/19 22:05:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.I18N;

/**
 * Job fuer "Ueberweisung".
 */
public class HBCIUeberweisungJob extends AbstractHBCIJob {

	private I18N i18n = null;

	public HBCIUeberweisungJob(Konto konto)
	{
		super(konto);
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getIdentifier()
   */
  public String getIdentifier() {
    return "TODO"; // TODO
  }
}


/**********************************************************************
 * $Log: HBCIUeberweisungJob.java,v $
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/