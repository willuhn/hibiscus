/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/tests/CanTermDelRestriction.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/14 19:21:37 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci.tests;

import java.util.Properties;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Testet, ob eine Termin-Ueberweisung bei der Bank gelöscht werden kann.
 */
public class CanTermDelRestriction implements Restriction
{
	private Properties p  = null;
	
	private I18N i18n;

  /**
   * ct.
   * @param jobRestrictions Job-Restrictions des HBCI-Systems.
   */
  public CanTermDelRestriction(Properties jobRestrictions)
  {
  	this.p = jobRestrictions;
  	
  	this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.tests.Restriction#test()
   */
  public void test() throws ApplicationException
  {
		String s = p.getProperty("cantermdel");
		Logger.debug("test if transfer deletable: restriction \"cantermdel\": " + s);
		if ("N".equalsIgnoreCase(s))
			throw new ApplicationException(i18n.tr("Der Auftrag kann bei Ihrer Bank nicht via HBCI gelöscht werden. Bitte verwenden Sie stattdessen das Web-Frontend Ihrer Bank."));
  }
}


/**********************************************************************
 * $Log: CanTermDelRestriction.java,v $
 * Revision 1.1  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 **********************************************************************/