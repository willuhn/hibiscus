/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/DTAUSSammelTransferImporter.java,v $
 * $Revision: 1.3 $
 * $Date: 2006/08/07 14:31:59 $
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
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
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
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    this.transferCache = new Hashtable();

    super.doImport(context, format, is, monitor);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSImporter#create(de.willuhn.datasource.rmi.DBObject, de.willuhn.datasource.GenericObject, de.jost_net.OBanToo.Dtaus.CSatz, de.jost_net.OBanToo.Dtaus.ASatz)
   */
  void create(DBObject skel, GenericObject context, CSatz csatz, ASatz asatz)
    throws RemoteException, ApplicationException
  {
    SammelTransfer t = (SammelTransfer) this.transferCache.get(asatz);
    if (t == null)
    {
      t = (SammelTransfer) skel;
      this.transferCache.put(asatz,t);

      DBService service = Settings.getDBService();

      // Konto suchen
      String kontonummer = Long.toString(asatz.getKonto());
      String blz         = Long.toString(asatz.getBlz());
      DBIterator konten = service.createList(Konto.class);
      konten.addFilter("kontonummer = '" + kontonummer + "'");
      konten.addFilter("blz = '" + blz + "'");

      Konto k = null;
      if (!konten.hasNext())
      {
        // Das Konto existiert nicht im Hibiscus-Datenbestand.
        KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
        d.setText(i18n.tr("Konto {0} [BLZ {1}] nicht gefunden\n" +
                          "Bitte wählen Sie das Konto aus, auf dem der Auftrag ausgeführt werden soll.",
                          new String[]{kontonummer,blz}));

        try
        {
          k = (Konto) d.open();
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          Logger.error("unable to choose konto",e);
          throw new OperationCanceledException(i18n.tr("Fehler beim Auswählen des Kontos"));
        }
      }
      else
      {
        k = (Konto) konten.next();
      }
      t.setKonto(k);
      t.setTermin(asatz.getAusfuehrungsdatum());
      t.setBezeichnung(asatz.getKundenname());
      t.store();
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
      ImportMessage im = new ImportMessage() {
        public GenericObject getImportedObject() throws RemoteException
        {
          return b;
        }
      };
      Application.getMessagingFactory().sendMessage(im);
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
 * Revision 1.3  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 **********************************************************************/