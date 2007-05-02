/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/tests/PreTimeRestriction.java,v $
 * $Revision: 1.5 $
 * $Date: 2007/05/02 12:03:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci.tests;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Testet, ob der erste Zahltag den HBCI-Restriktionen der Bank entspricht.
 */
public class PreTimeRestriction implements Restriction
{
	private Properties p  = null;
	private Date date     = null;
	
	private I18N i18n;

  /**
   * ct.
   * @param ersteZahlung
   * @param jobRestrictions Job-Restrictions des HBCI-Systems.
   */
  public PreTimeRestriction(Date ersteZahlung, Properties jobRestrictions)
  {
  	this.date = HBCIProperties.startOfDay(ersteZahlung);
  	this.p = jobRestrictions;
  	
  	this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.tests.Restriction#test()
   */
  public void test() throws ApplicationException
  {
		testMin();
		testMax();
  }

	private void testMin() throws ApplicationException
	{
		String min = p.getProperty("minpretime");
		if (min == null || min.length() == 0)
			return; // keine Einschraenkungen

		Logger.debug("testing first payment date " + date.toString() + " against restriction \"minpretime\": " + min);

		int i = 0;
		try
		{
			i = Integer.parseInt(min);
		}
		catch(NumberFormatException e)
		{
			// mhh, steht Bloedsinn drin. Naja, dann solls der User halt mal versuchen ;)
			Logger.warn("unable to parse job restriction value for \"minpretime\": " + min);
			return;
		}
		
		Calendar cal = Calendar.getInstance(Application.getConfig().getLocale());
		cal.add(Calendar.DATE, i);
    Date test = HBCIProperties.startOfDay(cal.getTime());
		if (date.before(test))
		{
			throw new ApplicationException(i18n.tr("Das Datum der Zahlung muss mindestens {0} Tag(e) in der Zukunft liegen",min));
		}
	}

	private void testMax() throws ApplicationException
	{
		String max = p.getProperty("maxpretime");
		if (max == null || max.length() == 0)
			return; // keine Einschraenkungen

		Logger.debug("testing first payment date " + date.toString() + " against restriction \"maxpretime\": " + max);

		int i = 0;
		try
		{
			i = Integer.parseInt(max);
		}
		catch(NumberFormatException e)
		{
			Logger.warn("unable to parse job restriction value for \"maxpretime\": " + max);
			return;
		}
		
		Calendar cal = Calendar.getInstance(Application.getConfig().getLocale());
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY,23);
		cal.set(Calendar.MINUTE,59);
		cal.add(Calendar.DATE, i);
		if (!cal.getTime().after(date))
		{
			throw new ApplicationException(i18n.tr("Das Datum der Zahlung darf höchstens {0} Tag(e) in der Zukunft liegen",max));
		}
	}

}


/**********************************************************************
 * $Log: PreTimeRestriction.java,v $
 * Revision 1.5  2007/05/02 12:03:40  willuhn
 * @B Uhrzeit bei Vorlaufzeit nicht beruecksichtigen
 *
 * Revision 1.4  2005/11/14 13:38:43  willuhn
 * @N Termin-Ueberweisungen
 *
 * Revision 1.3  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/29 00:32:32  willuhn
 * @N HBCI job restrictions
 *
 **********************************************************************/