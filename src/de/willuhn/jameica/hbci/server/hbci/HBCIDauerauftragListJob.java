/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragListJob.java,v $
 * $Revision: 1.36 $
 * $Date: 2009/01/04 22:13:27 $
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

/**
 * Job fuer "Dauerauftraege abrufen".
 */
public class HBCIDauerauftragListJob extends AbstractHBCIJob
{

	private Konto konto = null;

  /**
   * @param konto Konto, ueber welches die existierenden Dauerauftraege abgerufen werden.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIDauerauftragListJob(Konto konto) throws ApplicationException, RemoteException
	{
    super();
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
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Abruf Daueraufträge {0}",konto.getLongName());
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
		GVRDauerList result = (GVRDauerList) getJobResult();
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
        
        // Die Auftraege sind ueber das angegebene Konto abgerufen worden. Also
        // weisen wir dieses auch hart zu
        auftrag.setKonto(this.konto);

        // BUGZILLA 22 http://www.willuhn.de/bugzilla/show_bug.cgi?id=22
        // BEGIN
        String name = auftrag.getGegenkontoName();
        Logger.debug("checking name length: " + name + ", chars: " + name.length());
        if (name != null && name.length() > HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)
        {
          Logger.warn("name of other account longer than " + HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH + " chars. stripping");
          auftrag.setGegenkontoName(name.substring(0,HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH));
        }
        // END

        boolean found = false;
        while(existing.hasNext())
        {
          ex = (Dauerauftrag) existing.next();
          if (auftrag.getOrderID() != null && 
              auftrag.getOrderID().equals(ex.getOrderID())
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
            auftrag.getOrderID().equals(ex.getOrderID())
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

    konto.addToProtokoll(i18n.tr("Daueraufträge abgerufen"),Protokoll.TYP_SUCCESS);
    Logger.info("dauerauftrag list fetched successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Abrufen der Daueraufträge: {0}",error);
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}


/**********************************************************************
 * $Log: HBCIDauerauftragListJob.java,v $
 * Revision 1.36  2009/01/04 22:13:27  willuhn
 * @R redundanten Konto-Check auch beim Loeschen von Dauerauftraegen entfernt
 *
 * Revision 1.35  2009/01/03 22:38:52  willuhn
 * @R redundanten Konto-Vergleich entfernt - beide Konten sind IMMER identisch, da a) die existierenden Auftraege von diesem Konto ermittelt werden und b) vor dem Vergleich ein auftrag.setKonto() mit dem Konto aus a) gemacht wird
 *
 * Revision 1.34  2008/09/23 11:24:27  willuhn
 * @C Auswertung der Job-Results umgestellt. Die Entscheidung, ob Fehler oder Erfolg findet nun nur noch an einer Stelle (in AbstractHBCIJob) statt. Ausserdem wird ein Job auch dann als erfolgreich erledigt markiert, wenn der globale Job-Status zwar fehlerhaft war, aber fuer den einzelnen Auftrag nicht zweifelsfrei ermittelt werden konnte, ob er erfolgreich war oder nicht. Es koennte unter Umstaenden sein, eine Ueberweisung faelschlicherweise als ausgefuehrt markiert (wenn globaler Status OK, aber Job-Status != ERROR). Das ist aber allemal besser, als sie doppelt auszufuehren.
 *
 * Revision 1.33  2008/01/03 13:26:08  willuhn
 * @B Test-Bugfix - Dauerauftraege wurden doppelt angelegt
 *
 * Revision 1.32  2007/10/18 10:24:49  willuhn
 * @B Foreign-Objects in AbstractDBObject auch dann korrekt behandeln, wenn sie noch nicht gespeichert wurden
 * @C Beim Abrufen der Dauerauftraege nicht mehr nach Konten suchen sondern hart dem Konto zuweisen, ueber das sie abgerufen wurden
 **********************************************************************/