/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
 * Testet, ob terminierte Loeschung eines Auftrages, also zu einem angegebenen
 * Zeitpunkt bei der Bank erlaubt ist.
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
		Logger.debug("test if transfer deletable at defined date: restriction \"cantermdel\": " + s);
		if ("N".equalsIgnoreCase(s))
			throw new ApplicationException(i18n.tr("Der Auftrag kann bei Ihrer Bank nicht zu einem definierten Zeitpunkt gelöscht werden."));
  }
}


/**********************************************************************
 * $Log: CanTermDelRestriction.java,v $
 * Revision 1.2  2004/11/17 19:02:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 **********************************************************************/