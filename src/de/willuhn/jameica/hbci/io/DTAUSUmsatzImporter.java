/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/DTAUSUmsatzImporter.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/03/05 15:38:43 $
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
import java.util.Date;

import de.jost_net.OBanToo.Dtaus.ASatz;
import de.jost_net.OBanToo.Dtaus.CSatz;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;

/**
 * DTAUS-Importer fuer Umsaetze.
 */
public class DTAUSUmsatzImporter extends AbstractDTAUSImporter
{
  /**
   * ct.
   */
  public DTAUSUmsatzImporter()
  {
    super();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSImporter#create(de.willuhn.datasource.rmi.DBObject, de.willuhn.datasource.GenericObject, de.jost_net.OBanToo.Dtaus.CSatz, de.jost_net.OBanToo.Dtaus.ASatz)
   */
  void create(DBObject skel, GenericObject context, CSatz csatz, ASatz asatz)
    throws RemoteException, ApplicationException
  {
    Umsatz u = (Umsatz) skel;

    // Konto suchen
    String kontonummer = Long.toString(asatz.getKonto());
    String blz         = Long.toString(asatz.getBlz());

    u.setKonto(findKonto(kontonummer,blz));

    Date date = asatz.getAusfuehrungsdatum();
    if (date == null)
      date = new Date();
    
    u.setDatum(date);
    u.setValuta(date);
      
    u.setArt(Long.toString(csatz.getTextschluessel()));
    u.setCustomerRef(Long.toString(csatz.getInterneKundennummer()));
    u.setBetrag(csatz.getBetragInEuro());
    u.setEmpfaengerBLZ(Long.toString(csatz.getBlzEndbeguenstigt()));
    u.setEmpfaengerName(csatz.getNameEmpfaenger());
    u.setEmpfaengerKonto(Long.toString(csatz.getKontonummer()));
    u.setZweck(csatz.getVerwendungszweck(1));

    int z = csatz.getAnzahlVerwendungszwecke();
    if (z > 1)
      u.setZweck2(csatz.getVerwendungszweck(2));

    u.setChangedByUser();
    u.store();
  
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
 * Revision 1.6  2007/03/05 15:38:43  willuhn
 * @B Bug 365
 *
 * Revision 1.5  2006/10/09 10:10:09  willuhn
 * @B unnoetige Datenbank-Abfrage auch wenn Konto bereits im Cache ist
 *
 * Revision 1.4  2006/10/08 19:11:37  jost
 * Bugfix: Trotz korrekter Bankverbindung in der DTAUS-Datei kam der Kontenauswahldialog
 *
 * Revision 1.3  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.2  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.1  2006/06/19 12:57:31  willuhn
 * @N DTAUS-Import fuer Umsaetze
 * @B Formatierungsfehler in Umsatzliste
 *
 **********************************************************************/