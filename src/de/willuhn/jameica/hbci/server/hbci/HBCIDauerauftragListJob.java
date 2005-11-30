/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragListJob.java,v $
 * $Revision: 1.23 $
 * $Date: 2005/11/30 23:21:06 $
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
import de.willuhn.jameica.hbci.HBCIProperties;
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
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (konto == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));
			if (konto.isNewObject())
				konto.store();

			this.konto = konto;

			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (ApplicationException e2)
		{
			throw e2;
		}
		catch (Throwable t)
		{
			Logger.error("error while executing job " + getIdentifier(),t);
			throw new ApplicationException(i18n.tr("Fehler beim Erstellen des Auftrags. Fehlermeldung: {0}",t.getMessage()),t);
		}
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
		Logger.info("checking for new and changed entries");
		for (int i=0;i<lines.length;++i)
		{
			try
			{
				auftrag = Converter.HBCIDauer2HibiscusDauerauftrag(lines[i]);

        Logger.info("checking dauerauftrag order id: " + auftrag.getOrderID());

        // BUGZILLA 87 http://www.willuhn.de/bugzilla/show_bug.cgi?id=87
        Konto k = null;
        try
        {
          k = auftrag.getKonto();
          if (k != null && k.isNewObject())
          {
            Logger.info("current account is a new one, saving");
            k.store();
            auftrag.setKonto(k);
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to save account",e);
        }
        if (k == null || k.isNewObject())
        {
          Logger.warn("bank didn't sending account informations. using current account");
          auftrag.setKonto(konto);
        }

        // BUGZILLA 22 http://www.willuhn.de/bugzilla/show_bug.cgi?id=22
				// BEGIN
				String name = auftrag.getGegenkontoName();
				Logger.debug("checking name length: " + name + ", chars: " + name.length());
				if (name != null && name.length() > HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)
				{
					Logger.warn("name of other account longer than " + HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH +
						" chars. stripping");
					auftrag.setGegenkontoName(name.substring(0,HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH));
				}
				// END

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
            Logger.info("found a local copy. order id: " + auftrag.getOrderID() + ". Checking for modifications");
            if (auftrag.getChecksum() != ex.getChecksum())
            {
              Logger.info("modifications found, updating local copy");
              ex.overwrite(auftrag);
              ex.store();
            }
						break;
					}
				}

        if (!found)
				{
					Logger.info("no local copy found. adding dauerauftrag order id: " + auftrag.getOrderID());
					auftrag.store();// den hammer nicht gefunden. Neu anlegen
				}
				existing.begin();
			}
			catch (Exception e)
			{
				Logger.error("error while checking dauerauftrag, skipping this one",e);
			}
		}

		Logger.info("checking for deletable entries");
		existing.begin();
		while (existing.hasNext())
		{
			ex = (Dauerauftrag) existing.next();
			if (!ex.isActive())
				continue; // der existiert nicht bei der Bank und muss daher auch nicht geloescht werden.
			boolean found = false;
			for (int i=0;i<lines.length;++i)
			{
				auftrag = Converter.HBCIDauer2HibiscusDauerauftrag(lines[i]);
				if (auftrag.getOrderID() != null && 
						auftrag.getOrderID().equals(ex.getOrderID()) &&
					  auftrag.getKonto().equals(ex.getKonto())
					 )
				{
					found = true;
					break;
				}
			}
      if (!found)
      {
        Logger.info("dauerauftrag order id: " + ex.getOrderID() + " does no longer exist online, can be deleted");
        ex.delete();
      }
		}

		Logger.info("dauerauftrag list fetched successfully");
  }
}


/**********************************************************************
 * $Log: HBCIDauerauftragListJob.java,v $
 * Revision 1.23  2005/11/30 23:21:06  willuhn
 * @B ObjectNotFoundException beim Abrufen der Dauerauftraege
 *
 * Revision 1.22  2005/07/24 17:00:04  web0
 * *** empty log message ***
 *
 * Revision 1.21  2005/07/20 22:40:56  web0
 * *** empty log message ***
 *
 * Revision 1.20  2005/06/28 08:07:24  web0
 * @B bug 87
 *
 * Revision 1.19  2005/06/28 08:04:00  web0
 * @B bug 87
 *
 * Revision 1.18  2005/06/27 21:28:41  web0
 * @B bug 87
 *
 * Revision 1.17  2005/03/06 17:15:45  web0
 * *** empty log message ***
 *
 * Revision 1.16  2005/03/06 17:10:57  web0
 * *** empty log message ***
 *
 * Revision 1.15  2005/03/06 17:05:48  web0
 * @B bugzilla 22
 *
 * Revision 1.14  2005/03/06 16:53:52  web0
 * @B bugzilla 22
 *
 * Revision 1.13  2005/03/04 00:50:16  web0
 * @N Eingrauen abgelaufener Dauerauftraege
 * @N automatisches Loeschen von Dauerauftraegen, die lokal zwar
 * noch als aktiv markiert sind, bei der Bank jedoch nicht mehr existieren
 *
 * Revision 1.12  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
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