/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.BaseDauerauftrag;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "SEPA-Dauerauftraege abrufen".
 */
public class HBCISepaDauerauftragListJob extends AbstractHBCIJob
{

	private Konto konto = null;

  /**
   * @param konto Konto, ueber welches die existierenden SEPA-Dauerauftraege abgerufen werden.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISepaDauerauftragListJob(Konto konto) throws ApplicationException, RemoteException
	{
		try
		{
			if (konto == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));
			if (konto.isNewObject())
				konto.store();

			this.konto = konto;

      org.kapott.hbci.structures.Konto own = Converter.HibiscusKonto2HBCIKonto(konto);
      // Deutsche Umlaute im eigenen Namen noch ersetzen
      // siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=16052
      own.name = HBCIProperties.replace(own.name,HBCIProperties.TEXT_REPLACEMENTS_SEPA);
      setJobParam("src",own);
		}
		catch (ApplicationException | RemoteException e)
		{
			throw e;
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
  public String getIdentifier()
  {
    return "DauerSEPAList";
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getContext()
   */
  @Override
  protected HibiscusDBObject getContext()
  {
    return this.konto;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Abruf SEPA-Daueraufträge {0}",konto.getLongName());
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  protected void markExecuted() throws RemoteException, ApplicationException
  {
		GVRDauerList result = (GVRDauerList) getJobResult();

		// Abgleich mit den lokalen SEPA-Dauerauftraegen
		
    DBIterator existing        = konto.getSepaDauerauftraege();
    GVRDauerList.Dauer[] lines = result.getEntries();

    SepaDauerauftrag remote = null;
    SepaDauerauftrag local  = null;
    
    // Hier drin merken wir uns alle SepaDauerauftraege, die beim Abgleich
    // gefunden wurden. Denn die muessen garantiert nicht lokal geloescht werden.
    Map<SepaDauerauftrag,Boolean> matches = new HashMap<SepaDauerauftrag,Boolean>();

    ////////////////////////////////////////////////////////////////////////////
    // 1. Nach neuen und geaenderten Dauerauftraegen suchen
    Logger.info("checking for new and changed entries [received lines: " + lines.length + "]");
    for (Dauer standingOrder : lines)
    {
      try
      {
        remote = this.create(standingOrder);
        local = find(existing,remote);

        if (local != null)
        {
          Logger.info("found a local copy. order id: " + remote.getOrderID() + ". Checking for modifications");
          matches.put(local,Boolean.TRUE);
          if (remote.getChecksum() != local.getChecksum())
          {
            Logger.info("modifications found, updating local copy");
            local.overwrite(remote);
            this.store(local);
            Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(local));
          }
        }
        else
        {
          Logger.info("no local copy found. adding sepa-dauerauftrag order id: " + remote.getOrderID());
          this.store(remote);
          Application.getMessagingFactory().sendMessage(new ImportMessage(remote));
        }
      }
      catch (Exception e)
      {
        Logger.error("error while checking sepa-dauerauftrag, skipping this one",e);
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
      local = (SepaDauerauftrag) existing.next();
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
      
      Logger.info("sepa-dauerauftrag order id: " + local.getOrderID() + " does no longer exist online, can be deleted");
      local.delete();
    }

    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(konto));
    konto.addToProtokoll(i18n.tr("SEPA-Daueraufträge abgerufen"),Protokoll.TYP_SUCCESS);
    Logger.info("sepa-dauerauftrag list fetched successfully");
  }
  
  /**
   * Durchucht die Liste der lokalen SEPA-Dauerauftraege nach dem angegebenen.
   * Insofern die Bank korrekte Order-IDs liefert, findet die Funktion auch
   * dann die lokale Kopie, wenn Bankseitig Eigenschaften des SEPA-Dauerauftrages
   * (z.Bsp. Betrag oder Turnus) geaendert wurden.
   * @param existing Liste der lokal vorhandenen Dauerauftraege.
   * @param remote von der Bank gelieferter Dauerauftrag.
   * @return die lokale Kopie des Dauerauftrages oder NULL, wenn keine
   * lokale Kopie vorhanden ist.
   * @throws RemoteException
   */
  private SepaDauerauftrag find(DBIterator existing, SepaDauerauftrag remote) throws RemoteException
  {
    existing.begin();
    while (existing.hasNext())
    {
      SepaDauerauftrag local = (SepaDauerauftrag) existing.next();
      if (!local.isActive())
        continue; // der ist noch gar nicht bei der Bank und muss daher auch nicht abgeglichen werden
      
      String idLocal  = StringUtils.trimToEmpty(local.getOrderID());
      String idRemote = StringUtils.trimToEmpty(remote.getOrderID());

      // Platzhalter-ID verwenden, wenn die Bank keine uebertragen hat
      if (idRemote.length() == 0)
        idRemote = BaseDauerauftrag.ORDERID_PLACEHOLDER;
      
      if (idLocal.equals(idRemote))
      {
        // OK, die IDs sind schonmal identisch. Jetzt noch checken, ob
        // es wirklich eine echte ID ist oder der Platzhalter
        if (!idLocal.equals(BaseDauerauftrag.ORDERID_PLACEHOLDER))
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
   * Erstellt ein lokales SepaDauerauftrags-Objekt aus dem Remote-Auftrag von HBCI4Java.
   * @param remote der Remote-Auftrag von HBCI4Java.
   * @return das lokale Objekt.
   * @throws RemoteException
   * @throws ApplicationException
   */
  private SepaDauerauftrag create(Dauer remote) throws RemoteException, ApplicationException
  {
    SepaDauerauftrag auftrag = Converter.HBCIDauer2HibiscusSepaDauerauftrag(remote);
    auftrag.setKonto(this.konto); // Konto hart zuweisen - sie kamen ja auch von dem
    return auftrag;
  }
  
  /**
   * Speichert den Dauerauftrag, faengt aber ggf. auftretende Exceptions und loggt sie.
   * @param auftrag der zu speichernde Auftrag.
   * BUGZILLA 1031
   */
  private void store(SepaDauerauftrag auftrag)
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
        this.konto.addToProtokoll(i18n.tr("Speichern des SEPA-Dauerauftrages fehlgeschlagen: {0}",e.getMessage()),Protokoll.TYP_ERROR);
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
  protected String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Abrufen der SEPA-Daueraufträge: {0}",error);
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}
