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

import java.rmi.RemoteException;
import java.util.Properties;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
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
      testTurnusDays();
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
		if (turnus.getZeiteinheit() != Turnus.ZEITEINHEIT_MONATLICH)
      return;

    String turnusMonths = p.getProperty("turnusmonths");
    // BUGZILLA 206
		if (turnusMonths == null || turnusMonths.length() == 0 || turnusMonths.equals("00"))
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

  private void testTurnusDays() throws ApplicationException, RemoteException
  {
    // checken ob, monatlicher Turnus unterstuetzt wird.
    if (turnus.getZeiteinheit() != Turnus.ZEITEINHEIT_MONATLICH)
      return;

    String days = p.getProperty("dayspermonth");
    // BUGZILLA 206
    if (days == null || days.length() == 0 || days.equals("00"))
      return; // keine Einschraenkung

    int test = turnus.getTag();

    Logger.debug("testing interval " + test + " against restriction \"dayspermonth\": " + days);

    StringBuffer sb = new StringBuffer();
    for (int i=0;i<days.length();i+=2)
    {
      try
      {
        int d = Integer.parseInt(days.substring(i,i+2));
        sb.append(","+d);
        if (test == d)
          return; // jepp, Bank hat diesen Turnus
      }
      catch (Exception e)
      {
        // skip
      }
    }

    if (test == HBCIProperties.HBCI_LAST_OF_MONTH)
      throw new ApplicationException(i18n.tr("Zahlungen zum Monatsletzten werden von Ihrer Bank nicht unterstützt"));

    String s = sb.toString().substring(1);
    String[] values = new String[] { ""+test,s};
    throw new ApplicationException(i18n.tr("Zahlungen am {0}. des Monats werden von Ihrer Bank nicht unterstützt. Erlaubte Werte: {1}",values));
  }

}


/**********************************************************************
 * $Log: TurnusRestriction.java,v $
 * Revision 1.4  2006/03/02 13:52:47  willuhn
 * @B bug 206
 *
 * Revision 1.3  2005/06/27 11:26:30  web0
 * @N neuer Test bei Dauerauftraegen (zum Monatsletzten)
 * @N neue DDV-Lib
 *
 * Revision 1.2  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/29 00:32:32  willuhn
 * @N HBCI job restrictions
 *
 **********************************************************************/