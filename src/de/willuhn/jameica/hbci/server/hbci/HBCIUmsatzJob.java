/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUmsatzJob.java,v $
 * $Revision: 1.11 $
 * $Date: 2004/10/23 17:34:31 $
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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

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
			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
		}
		catch (RemoteException e)
		{
			throw new RuntimeException("Fehler beim Setzen des Kontos");
		}

		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getIdentifier()
   */
  public String getIdentifier() {
    return "KUmsAll";
  }

  /**
   * Liefert eine Liste der abgerufenen Umsaetze.
   * Die Objekte sind neu erstellt und <b>nicht</b> in der embedded Datenbank
   * gespeichert.<br>
   * Es werden grundsaetzlich alle bei der Bank verfuegbaren
   * Umsaetze fuer dieses Konto abgeholt. Es ist Sache des Aufrufers,
   * ueber die Liste der bereits lokal gespeicherten zu iterieren und
   * nur genau die zu speichern, die lokal noch nicht vorhanden sind. 
   * @return Liste der aktuell auf dem Konto verfuegbaren Umsaetze.
   * @throws ApplicationException
   * @throws RemoteException
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

			try {
				getKonto().addToProtokoll(i18n.tr("Fehler beim Abrufen der Umsätze") + " ("+ msg +")",Protokoll.TYP_ERROR);
			}
			catch (RemoteException e)
			{
				Logger.error("error while writing protocol",e);
			}
			throw new ApplicationException(msg);
		}
		Logger.debug("job result is ok, returning umsatz list");

		// So, jetzt kopieren wir das ResultSet noch in unsere
		// eigenen Datenstrukturen. ;)
		GVRKUms.UmsLine[] lines = result.getFlatData();
		Umsatz[] umsaetze = new Umsatz[lines.length];
		for (int i=0;i<lines.length;++i)
		{
			umsaetze[i] = Converter.HBCIUmsatz2HibiscusUmsatz(lines[i]);
			umsaetze[i].setKonto(getKonto()); // muessen wir noch machen, weil der Converter das Konto nicht kennt
		}
		try {
			getKonto().addToProtokoll(i18n.tr("Umsätze abgerufen"),Protokoll.TYP_SUCCESS);
		}
		catch (RemoteException e)
		{
			Logger.error("error while writing protocol",e);
		}
		return umsaetze;

	}
}


/**********************************************************************
 * $Log: HBCIUmsatzJob.java,v $
 * Revision 1.11  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.9  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.8  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.6  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.5  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
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