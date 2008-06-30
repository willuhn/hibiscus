/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/DTAUSSammelTransferImporter.java,v $
 * $Revision: 1.9 $
 * $Date: 2008/06/30 13:04:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;

import de.jost_net.OBanToo.Dtaus.ASatz;
import de.jost_net.OBanToo.Dtaus.CSatz;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * DTAUS-Importer fuer komplette Sammel-Ueberweisungen und Sammel-Lastschriften.
 */
public class DTAUSSammelTransferImporter extends AbstractDTAUSImporter
{
  private Hashtable transferCache = null;

  /**
   * ct.
   */
  public DTAUSSammelTransferImporter()
  {
    super();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(Object context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    this.transferCache = new Hashtable();
    super.doImport(context, format, is, monitor);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSImporter#create(de.willuhn.datasource.rmi.DBObject, java.lang.Object, de.jost_net.OBanToo.Dtaus.CSatz, de.jost_net.OBanToo.Dtaus.ASatz)
   */
  void create(DBObject skel, Object context, CSatz csatz, ASatz asatz)
    throws RemoteException, ApplicationException
  {
    SammelTransfer t = (SammelTransfer) this.transferCache.get(asatz);
    if (t == null)
    {
      t = (SammelTransfer) skel;
      this.transferCache.put(asatz,t);

      // Konto suchen
      String kontonummer = Long.toString(asatz.getKonto());
      String blz         = Long.toString(asatz.getBlz());

      t.setKonto(findKonto(kontonummer,blz));
      t.setTermin(asatz.getAusfuehrungsdatum());
      t.setBezeichnung(asatz.getKundenname());
      t.store();
      try
      {
        Application.getMessagingFactory().sendMessage(new ImportMessage(t));
      }
      catch (Exception ex)
      {
        Logger.error("error while sending import message",ex);
      }
    }

    final SammelTransferBuchung b = t.createBuchung();
    b.setBetrag(csatz.getBetragInEuro());
    b.setGegenkontoBLZ(Long.toString(csatz.getBlzEndbeguenstigt()));
    b.setGegenkontoName(csatz.getNameEmpfaenger());
    b.setGegenkontoNummer(Long.toString(csatz.getKontonummer()));
    b.setZweck(csatz.getVerwendungszweck(1));
    
    int z = csatz.getAnzahlVerwendungszwecke();
    if (z > 1)
      b.setZweck2(csatz.getVerwendungszweck(2));
    b.store();

    // Das muessen wir hier uebernehmen, da AbstractDTAUSImporter nichts
    // von den einzelnen Buchungen weiss.
    try
    {
      Application.getMessagingFactory().sendMessage(new ImportMessage(b));
    }
    catch (Exception ex)
    {
      Logger.error("error while sending import message",ex);
    }
  }


  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSIO#getSupportedObjectTypes()
   */
  Class[] getSupportedObjectTypes()
  {
    return new Class[]
      {
        SammelLastschrift.class,
        SammelUeberweisung.class
      };
  }
}


/*********************************************************************
 * $Log: DTAUSSammelTransferImporter.java,v $
 * Revision 1.9  2008/06/30 13:04:10  willuhn
 * @N Von-Bis-Filter auch in Sammel-Auftraegen
 *
 * Revision 1.8  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.7  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.6  2007/03/05 15:38:43  willuhn
 * @B Bug 365
 *
 * Revision 1.5  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.4  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.3  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 **********************************************************************/