/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragListJob.java,v $
 * $Revision: 1.6 $
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

import org.kapott.hbci.GV_Result.GVRDauerList;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Job fuer "Dauerauftraege abrufen".
 */
public class HBCIDauerauftragListJob extends AbstractHBCIJob {

	private I18N i18n = null;

	/**
	 * ct.
   * @param konto Konto, ueber welches die existierenden Dauerauftraege abgerufen werden.
   */
  public HBCIDauerauftragListJob(Konto konto)
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
    return "DauerList";
  }
  
  /**
   * Liefert eine Liste der abgerufenen Dauerauftraege.
   * Die Objekte sind neu erstellt und <b>nicht</b> in der embedded Datenbank
   * gespeichert.<br>
   * Es werden grundsaetzlich alle bei der Bank verfuegbaren
   * Dauerauftraege fuer dieses Konto abgeholt. Es ist Sache des Aufrufers,
   * ueber die Liste der bereits lokal gespeicherten zu iterieren und
   * nur genau die zu speichern, die lokal noch nicht vorhanden sind. 
   * @return Liste der aktuell auf dem Konto verfuegbaren Dauerauftraege.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public Dauerauftrag[] getDauerauftraege() throws ApplicationException, RemoteException
  {
		GVRDauerList result = (GVRDauerList) getJobResult();

		String statusText = getStatusText();
		if (!result.isOK())
		{
			String msg = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Fehler beim Abrufen der Umsätze");

			try {
				getKonto().addToProtokoll(i18n.tr("Fehler beim Abrufen der Daueraufträge") + " ("+ msg +")",Protokoll.TYP_ERROR);
			}
			catch (RemoteException e)
			{
				Logger.error("error while writing protocol",e);
			}
			throw new ApplicationException(msg);
		}
		Logger.debug("job result is ok, returning ");

		// So, jetzt kopieren wir das ResultSet noch in unsere
		// eigenen Datenstrukturen. ;)
		GVRDauerList.Dauer[] lines = result.getEntries();
		Dauerauftrag[] auftraege = new Dauerauftrag[lines.length];
		for (int i=0;i<lines.length;++i)
		{
			auftraege[i] = Converter.HBCIDauer2HibiscusDauerauftrag(lines[i]);
			auftraege[i].activate();
		}
		try {
			getKonto().addToProtokoll(i18n.tr("Daueraufträge abgerufen"),Protokoll.TYP_SUCCESS);
		}
		catch (RemoteException e)
		{
			Logger.error("error while writing protocol",e);
		}
		return auftraege;

	}
}


/**********************************************************************
 * $Log: HBCIDauerauftragListJob.java,v $
 * Revision 1.6  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.4  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 **********************************************************************/