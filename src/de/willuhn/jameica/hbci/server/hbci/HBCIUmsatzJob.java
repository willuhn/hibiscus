/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUmsatzJob.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/05/25 23:23:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;

import org.kapott.hbci.GV_Result.GVRKUms;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Umsatz-Abfrage".
 */
public class HBCIUmsatzJob extends AbstractHBCIJob {

	private I18N i18n = null;

	/**
	 * ct.
   * @param konto
   */
  public HBCIUmsatzJob(Konto konto)
	{
		super(konto);

		try {
			setJobParam("my",Converter.JameicaKonto2HBCIKonto(konto));
		}
		catch (RemoteException e)
		{
			throw new RuntimeException("Fehler beim Setzen des Kontos");
		}

		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getIdentifier()
   */
  public String getIdentifier() {
    return "KUmsAll";
  }

	/**
	 * Liefert den Saldo.
   * @return Saldo.
   */
  public Umsatz[] getUmsaetze() throws ApplicationException, RemoteException
	{
		GVRKUms result = (GVRKUms) getJobResult();

		String statusText = getStatusText();
		if (!result.isOK())
		{
			String msg = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Fehler beim Abrufen der Umsätze");

			addToProtokoll(i18n.tr("Fehler beim Abrufen der Umsätze") + " ("+ msg +")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg);
		}
		Application.getLog().debug("job result is ok, returning saldo");

		// So, jetzt kopieren wir das ResultSet noch in unsere
		// eigenen Datenstrukturen. ;)
		GVRKUms.UmsLine[] lines = result.getFlatData();
		Umsatz[] umsaetze = new Umsatz[lines.length];
		for (int i=0;i<lines.length;++i)
		{
			umsaetze[i] = Converter.convert(lines[i]);
			umsaetze[i].setKonto(getKonto()); // muessen wir noch machen, weil der Converter das Konto nicht kennt
		}
		addToProtokoll(i18n.tr("Umsätze abgerufen"),Protokoll.TYP_SUCCESS);
		return umsaetze;

	}
}


/**********************************************************************
 * $Log: HBCIUmsatzJob.java,v $
 * Revision 1.3  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.2  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/