/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragListJob.java,v $
 * $Revision: 1.39 $
 * $Date: 2012/03/01 22:19:15 $
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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV_Result.GVRDauerList;
import org.kapott.hbci.GV_Result.GVRDauerList.Dauer;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
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
		try
		{
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

		// Abgleich mit den lokalen Dauerauftraegen
		
    DBIterator existing        = konto.getDauerauftraege();
    GVRDauerList.Dauer[] lines = result.getEntries();

    Dauerauftrag remote = null;
    Dauerauftrag local  = null;
    
    // Hier drin merken wir uns alle Dauerauftraege, die beim Abgleich
    // gefunden wurden. Denn die muessen garantiert nicht lokal geloescht werden.
    Map<Dauerauftrag,Boolean> matches = new HashMap<Dauerauftrag,Boolean>();

    ////////////////////////////////////////////////////////////////////////////
    // 1. Nach neuen und geaenderten Dauerauftraegen suchen
    Logger.info("checking for new and changed entries");
    for (int i=0;i<lines.length;++i)
    {
      try
      {
        remote = this.create(lines[i]);
        local  = find(existing,remote);
        
        if (local != null)
        {
          Logger.info("found a local copy. order id: " + remote.getOrderID() + ". Checking for modifications");
          matches.put(local,Boolean.TRUE);
          if (remote.getChecksum() != local.getChecksum())
          {
            Logger.info("modifications found, updating local copy");
            local.overwrite(remote);
            this.store(local);
          }
        }
        else
        {
          Logger.info("no local copy found. adding dauerauftrag order id: " + remote.getOrderID());
          this.store(remote);
        }
      }
      catch (Exception e)
      {
        Logger.error("error while checking dauerauftrag, skipping this one",e);
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    Logger.info("checking for deletable entries");
    existing.begin();
    
    // Wir koennen jetzt hier alle loeschen, die NICHT in matches gefunden (also nicht von der Bank geliefert wurden)
    // aber eine Order-ID haben und somit aktiv sein muessten
    while (existing.hasNext())
    {
      local = (Dauerauftrag) existing.next();
      if (!local.isActive())
      {
        Logger.info("skipping [id: " + local.getID() + "] - not yet submitted");
        continue; // der wurde noch nicht zur Bank geschickt und muss daher auch nicht geloescht werden
      }
      
      if (matches.containsKey(local))
      {
        Logger.info("skipping [id: " + local.getID() + ", order id: " + local.getOrderID() + "] - just matched");
        continue;
      }
      
      Logger.info("dauerauftrag order id: " + local.getOrderID() + " does no longer exist online, can be deleted");
      local.delete();
    }

    konto.addToProtokoll(i18n.tr("Daueraufträge abgerufen"),Protokoll.TYP_SUCCESS);
    Logger.info("dauerauftrag list fetched successfully");
  }
  
  /**
   * Durchucht die Liste der lokalen Dauerauftraege nach dem angegebenen.
   * Insofern die Bank korrekte Order-IDs liefert, findet die Funktion auch
   * dann die lokale Kopie, wenn Bankseitig Eigenschaften des Dauerauftrages
   * (z.Bsp. Betrag oder Turnus) geaendert wurden.
   * @param existing Liste der lokal vorhandenen Dauerauftraege.
   * @param remote von der Bank gelieferter Dauerauftrag.
   * @return die lokale Kopie des Dauerauftrages oder NULL, wenn keine
   * lokale Kopie vorhanden ist.
   * @throws RemoteException
   */
  private Dauerauftrag find(DBIterator existing, Dauerauftrag remote) throws RemoteException
  {
    existing.begin();
    while (existing.hasNext())
    {
      Dauerauftrag local = (Dauerauftrag) existing.next();
      if (!local.isActive())
        continue; // der ist noch gar nicht bei der Bank und muss daher auch nicht abgeglichen werden
      
      String idLocal  = StringUtils.trimToEmpty(local.getOrderID());
      String idRemote = StringUtils.trimToEmpty(remote.getOrderID());

      // Platzhalter-ID verwenden, wenn die Bank keine uebertragen hat
      if (idRemote.length() == 0)
        idRemote = Dauerauftrag.ORDERID_PLACEHOLDER;
      
      if (idLocal.equals(idRemote))
      {
        // OK, die IDs sind schonmal identisch. Jetzt noch checken, ob
        // es wirklich eine echte ID ist oder der Platzhalter
        if (!idLocal.equals(Dauerauftrag.ORDERID_PLACEHOLDER))
          return local; // Echte ID - also haben wir ihn gefunden
        
        // Es ist also die Platzhalter-ID. Dann vergleichen wir
        // anhand der Eigenschaften
        if (local.getChecksum() == remote.getChecksum())
          return local; // sind identisch - gefunden
      }
    }
    
    // Nicht gefunden
    return null;
  }
  
  /**
   * Erstellt ein lokales Dauerauftrags-Objekt aus dem Remote-Auftrag von HBCI4Java.
   * @param remote der Remote-Auftrag von HBCI4Java.
   * @return das lokale Objekt.
   * @throws RemoteException
   * @throws ApplicationException
   */
  private Dauerauftrag create(Dauer remote) throws RemoteException, ApplicationException
  {
    Dauerauftrag auftrag = Converter.HBCIDauer2HibiscusDauerauftrag(remote);
    auftrag.setKonto(this.konto); // Konto hart zuweisen - sie kamen ja auch von dem

    // BUGZILLA 22 http://www.willuhn.de/bugzilla/show_bug.cgi?id=22
    String name = auftrag.getGegenkontoName();
    Logger.debug("checking name length: " + name + ", chars: " + name.length());
    if (name != null && name.length() > HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)
    {
      Logger.warn("name of other account longer than " + HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH + " chars. stripping");
      auftrag.setGegenkontoName(name.substring(0,HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH));
    }
    
    return auftrag;
  }
  
  /**
   * Speichert den Dauerauftrag, faengt aber ggf. auftretende Exceptions und loggt sie.
   * @param auftrag der zu speichernde Auftrag.
   * BUGZILLA 1031
   */
  private void store(Dauerauftrag auftrag)
  {
    try
    {
      auftrag.store();
    }
    catch (Exception e) // BUGZILLA 1031
    {
      try
      {
        Logger.error("unable to store dauerauftrag " + auftrag.getOrderID() + ", skipping",e);
        this.konto.addToProtokoll(i18n.tr("Speichern des Dauerauftrages fehlgeschlagen: {0}",e.getMessage()),Protokoll.TYP_ERROR);
      }
      catch (Exception e2)
      {
        Logger.error("unable to log error",e2); // useless
      }
    }
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
 * Revision 1.39  2012/03/01 22:19:15  willuhn
 * @N i18n statisch und expliziten Super-Konstruktor entfernt - unnoetig
 *
 * Revision 1.38  2011-09-12 11:53:25  willuhn
 * @N Support fuer Banken (wie die deutsche Bank), die keine Order-IDs vergeben - BUGZILLA 1129
 *
 * Revision 1.37  2011-04-29 08:00:38  willuhn
 * @B BUGZILLA 1031
 *
 * Revision 1.36  2009-01-04 22:13:27  willuhn
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