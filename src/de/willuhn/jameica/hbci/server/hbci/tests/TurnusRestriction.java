/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/tests/TurnusRestriction.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/11/12 18:25:07 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci.tests;

import java.rmi.RemoteException;
import java.util.Properties;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Testet, ob der Turnus den HBCI-Restriktionen der Bank entspricht. 
 */
public class TurnusRestriction implements Restriction
{
	private Turnus turnus = null;
	private Properties p  = null;
	
	private I18N i18n;

  /**
   * ct.
   * @param turnus zu testender Turnus.
   * @param jobRestrictions Job-Restrictions des HBCI-Systems.
   */
  public TurnusRestriction(Turnus turnus, Properties jobRestrictions)
  {
  	this.turnus = turnus;
  	this.p = jobRestrictions;
  	
  	this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.tests.Restriction#test()
   */
  public void test() throws ApplicationException
  {
		try
		{
			testTurnusMonths();
		}
		catch (RemoteException e)
		{
			Logger.error("error while performing turnus restriction test",e);
			throw new ApplicationException(i18n.tr("Fehler beim Testen des Zahlungsturnus auf Gültigkeit"));
		}
  }

	private void testTurnusMonths() throws ApplicationException, RemoteException
	{
		// checken ob, monatlicher Turnus unterstuetzt wird.
		if (turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH)
		{
			String turnusMonths = p.getProperty("turnusmonths");
			if (turnusMonths == null || turnusMonths.length() == 0)
				return; // keine Einschraenkung

			int test = turnus.getIntervall();

			Logger.debug("testing interval " + test + " against restriction \"turnusmonths\": " + turnusMonths);

			for (int i=0;i<turnusMonths.length();i+=2)
			{
				try
				{
					if (test == Integer.parseInt(turnusMonths.substring(i,i+2)))
						return; // jepp, Bank hat diesen Turnus
				}
				catch (Exception e)
				{
					// skip
				}
			}
			throw new ApplicationException(i18n.tr("Turnus mit einem Intervall von {0} Monaten wird von Ihrer Bank nicht unterstützt",""+test));
		}
	}
}


/**********************************************************************
 * $Log: TurnusRestriction.java,v $
 * Revision 1.2  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/29 00:32:32  willuhn
 * @N HBCI job restrictions
 *
 **********************************************************************/