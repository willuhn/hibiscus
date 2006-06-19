/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/DTAUSUmsatzImporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/06/19 12:57:31 $
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
import java.util.Date;
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
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * DTAUS-Importer fuer Umsaetze.
 */
public class DTAUSUmsatzImporter extends AbstractDTAUSImporter
{
  private Hashtable kontenCache = null;
  
  /**
   * ct.
   */
  public DTAUSUmsatzImporter()
  {
    super();
  }
  
  
  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    // Wir merken uns die Konten, die der User schonmal ausgewaehlt
    // hat, um ihn nicht fuer jede Buchung mit immer wieder dem
    // gleichen Konto zu nerven
    this.kontenCache = new Hashtable();

    super.doImport(context,format,is,monitor);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSImporter#fill(de.willuhn.datasource.rmi.DBObject, de.willuhn.datasource.GenericObject, de.jost_net.OBanToo.Dtaus.CSatz, de.jost_net.OBanToo.Dtaus.ASatz)
   */
  void fill(DBObject skel, GenericObject context, CSatz csatz, ASatz asatz)
    throws RemoteException, ApplicationException
  {
    Umsatz u = (Umsatz) skel;

    DBService service = Settings.getDBService();

    // Konto suchen
    String kontonummer = Long.toString(csatz.getKontoAuftraggeber());
    String blz         = Long.toString(csatz.getBlzErstbeteiligt());
    DBIterator konten = service.createList(Konto.class);
    konten.addFilter("kontonummer = '" + kontonummer + "'");
    konten.addFilter("blz = '" + blz + "'");

    Konto k = null;
    if (!konten.hasNext())
    {
      // Das Konto existiert nicht im Hibiscus-Datenbestand.

      // Erstmal schauen, ob der User das Konto schonmal ausgewaehlt hat:
      k = (Konto) kontenCache.get(kontonummer + blz);
      if (k == null)
      {
        // Ne, hat er noch nicht.
        // Also muss der User eins auswaehlen.
        KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
        d.setText(i18n.tr("Konto {0} [BLZ {1}] nicht gefunden\n" +
                          "Bitte wählen Sie das Konto aus, für das der Umsatz importiert werden soll.",
                          new String[]{kontonummer,blz}));

        try
        {
          k = (Konto) d.open();
        }
        catch (OperationCanceledException oce)
        {
          throw new ApplicationException(i18n.tr("Umsatz wird übersprungen"));
        }
        catch (Exception e)
        {
          Logger.error("unable to choose konto",e);
        }
        
        if (k != null)
          kontenCache.put(kontonummer + blz,k);
      }
    }
    else
    {
      k = (Konto) konten.next();
    }
    
    Date date = asatz.getAusfuehrungsdatum();
    if (date == null)
      date = new Date();

    u.setKonto(k);
    u.setArt(Long.toString(csatz.getTextschluessel()));
    u.setCustomerRef(Long.toString(csatz.getInterneKundennummer()));
    u.setDatum(date);
    u.setValuta(date);
      
    u.setBetrag(csatz.getBetragInEuro());
    u.setEmpfaengerBLZ(Long.toString(csatz.getBlzEndbeguenstigt()));
    u.setEmpfaengerName(csatz.getNameEmpfaenger());
    u.setEmpfaengerKonto(Long.toString(csatz.getKontonummer()));
    u.setZweck(csatz.getVerwendungszweck(1));

    int z = csatz.getAnzahlVerwendungszwecke();
    if (z > 1)
      u.setZweck2(csatz.getVerwendungszweck(2));

    u.setChangedByUser();
  
  }


  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSIO#getSupportedObjectTypes()
   */
  Class[] getSupportedObjectTypes()
  {
    return new Class[]
      {
        Umsatz.class
      };
  }
}


/*********************************************************************
 * $Log: DTAUSUmsatzImporter.java,v $
 * Revision 1.1  2006/06/19 12:57:31  willuhn
 * @N DTAUS-Import fuer Umsaetze
 * @B Formatierungsfehler in Umsatzliste
 *
 **********************************************************************/