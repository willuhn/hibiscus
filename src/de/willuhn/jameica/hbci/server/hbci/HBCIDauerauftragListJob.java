/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragListJob.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/11/12 18:25:08 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Dauerauftraege abrufen".
 */
public class HBCIDauerauftragListJob extends AbstractHBCIJob {

	private I18N i18n = null;
	private Konto konto = null;

  /**
   * @param konto Konto, ueber welches die existierenden Dauerauftraege abgerufen werden.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIDauerauftragListJob(Konto konto) throws ApplicationException, RemoteException
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (konto == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));
		if (konto.isNewObject())
			konto.store();

		this.konto = konto;

		setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier()
  {
    return "DauerList";
  }
  
  /**
   * Prueft, ob das Abrufen der Dauerauftraege erfolgreich war und aktualisiert
   * die lokalen Kopien.
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
  void handleResult() throws ApplicationException, RemoteException
  {
		GVRDauerList result = (GVRDauerList) getJobResult();
		String statusText = getStatusText();
		if (!result.isOK())
		{
			String msg = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Fehler beim Abrufen der Umsätze");

			konto.addToProtokoll(i18n.tr("Fehler beim Abrufen der Daueraufträge") + " ("+ msg +")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg);
		}

		konto.addToProtokoll(i18n.tr("Daueraufträge abgerufen"),Protokoll.TYP_SUCCESS);

		// So, jetzt kopieren wir das ResultSet noch in unsere
		// eigenen Datenstrukturen.

		// Wir vergleichen noch mit den Dauerauftraegen, die wir schon haben und
		// speichern nur die neuen. Achtung: Es kann sein, dass ein Dauerauftrag
		// geaendert wurde, ohne dass wir davon erfahren haben (z.Bsp. wenn der
		// Kunde ihn uebers Webfrontend der Bank geaendert hat. Folglich
		// ueberschreiben wir Dauerauftraege auch dann schon, wenn die Order-ID
		// uebereinstimmt.
		DBIterator existing = konto.getDauerauftraege();

		GVRDauerList.Dauer[] lines = result.getEntries();
		Dauerauftrag auftrag;
		Dauerauftrag ex;
		for (int i=0;i<lines.length;++i)
		{
			try
			{
				auftrag = Converter.HBCIDauer2HibiscusDauerauftrag(lines[i]);
				boolean found = false;
				while(existing.hasNext())
				{
					ex = (Dauerauftrag) existing.next();
					if (auftrag.getOrderID() != null && 
							auftrag.getOrderID().equals(ex.getOrderID()) &&
							auftrag.getKonto().equals(ex.getKonto())
						 )
					{
						// Den haben wir schon, ueberschreiben wir
						found = true;
						ex.overwrite(auftrag);
						ex.store();
						break;
					}
				}
				if (!found)
					auftrag.store();// den hammer nicht gefunden. Neu anlegen

				existing.begin();
			}
			catch (Exception e)
			{
				Logger.error("error while checking dauerauftrag, skipping this one",e);
			}
		}


		Logger.info("dauerauftrag list fetched successfully");
  }
}


/**********************************************************************
 * $Log: HBCIDauerauftragListJob.java,v $
 * Revision 1.10  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.7  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
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