/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/DTAUSTransferImporter.java,v $
 * $Revision: 1.12 $
 * $Date: 2011/06/07 10:07:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.jost_net.OBanToo.Dtaus.ASatz;
import de.jost_net.OBanToo.Dtaus.CSatz;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.util.ApplicationException;

/**
 * DTAUS-Importer fuer Ueberweisungen und Lastschriften.
 */
public class DTAUSTransferImporter extends AbstractDTAUSImporter
{
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSImporter#create(de.willuhn.datasource.rmi.DBObject, java.lang.Object, de.jost_net.OBanToo.Dtaus.CSatz, de.jost_net.OBanToo.Dtaus.ASatz)
   */
  void create(DBObject skel, Object context, CSatz csatz, ASatz asatz)
    throws RemoteException, ApplicationException
  {
    // Wir verlassen uns hier einfach drauf, dass es sich bei dem
    // Skelet um einen HibiscusTransfer handelt. Schliesslich haben wir
    // in getSupportedObjectTypes nur solche angegeben
    HibiscusTransfer t = (HibiscusTransfer) skel;

    // Konto suchen
    String kontonummer = Long.toString(asatz.getKonto());
    String blz         = Long.toString(asatz.getBlz());

    t.setKonto(findKonto(kontonummer,blz));
    t.setBetrag(csatz.getBetragInEuro());
    t.setGegenkontoBLZ(Long.toString(csatz.getBlzEndbeguenstigt()));
    t.setGegenkontoName(csatz.getNameEmpfaenger());
    t.setGegenkontoNummer(Long.toString(csatz.getKontonummer()));
    
    List<String> lines = new ArrayList<String>();
    for (int i=1;i<=csatz.getAnzahlVerwendungszwecke();++i)
    {
      lines.add(csatz.getVerwendungszweck(i));
    }
    VerwendungszweckUtil.apply(t,lines.toArray(new String[lines.size()]));
    
    t.store();
  }


  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSIO#getSupportedObjectTypes()
   */
  Class[] getSupportedObjectTypes()
  {
    return new Class[]
      {
        Ueberweisung.class,
        Lastschrift.class
      };
  }
}


/*********************************************************************
 * $Log: DTAUSTransferImporter.java,v $
 * Revision 1.12  2011/06/07 10:07:50  willuhn
 * @C Verwendungszweck-Handling vereinheitlicht/vereinfacht - geht jetzt fast ueberall ueber VerwendungszweckUtil
 *
 * Revision 1.11  2009/06/15 08:51:16  willuhn
 * @N BUGZILLA 736
 *
 * Revision 1.10  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.9  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.8  2007/03/05 15:38:43  willuhn
 * @B Bug 365
 *
 * Revision 1.7  2006/10/09 10:10:09  willuhn
 * @B unnoetige Datenbank-Abfrage auch wenn Konto bereits im Cache ist
 *
 * Revision 1.6  2006/10/08 19:03:00  jost
 * Bugfix: Trotz korrekter Bankverbindung in der DTAUS-Datei kam der Kontenauswahldialog
 *
 * Revision 1.5  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.4  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.3  2006/06/19 12:57:31  willuhn
 * @N DTAUS-Import fuer Umsaetze
 * @B Formatierungsfehler in Umsatzliste
 *
 * Revision 1.2  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.1  2006/06/08 17:40:59  willuhn
 * @N Vorbereitungen fuer DTAUS-Import von Sammellastschriften und Umsaetzen
 *
 * Revision 1.11  2006/06/07 22:42:00  willuhn
 * @N DTAUSExporter
 * @N Abstrakte Basis-Klasse fuer Export und Import
 *
 * Revision 1.10  2006/06/07 17:26:40  willuhn
 * @N DTAUS-Import fuer Lastschriften
 * @B Satusbar-Update in DTAUSImport gefixt
 *
 * Revision 1.9  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.8  2006/06/06 21:37:55  willuhn
 * @R FilternEngine entfernt. Wird jetzt ueber das Jameica-Messaging-System abgewickelt
 *
 * Revision 1.7  2006/06/05 09:55:50  jost
 * Anpassung an obantoo 0.5
 *
 * Revision 1.6  2006/05/31 09:04:21  willuhn
 * @C Wir merken uns die vom User bereits ausgewaehlten Konten
 *
 * Revision 1.5  2006/05/29 21:20:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2006/05/29 20:41:21  willuhn
 * @N Import aller logischen Dateien
 *
 * Revision 1.3  2006/05/29 09:16:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/05/25 13:54:38  willuhn
 * @R removed imports (occurs compile errors in nightly build)
 *
 * Revision 1.1  2006/05/25 13:47:03  willuhn
 * @N Skeleton for DTAUS-Import
 *
 **********************************************************************/