/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.jost_net.OBanToo.Dtaus.ASatz;
import de.jost_net.OBanToo.Dtaus.CSatz;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.util.ApplicationException;

/**
 * DTAUS-Importer fuer Umsaetze.
 */
public class DTAUSUmsatzImporter extends AbstractDTAUSImporter
{
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSImporter#create(de.willuhn.datasource.rmi.DBObject, java.lang.Object, de.jost_net.OBanToo.Dtaus.CSatz, de.jost_net.OBanToo.Dtaus.ASatz)
   */
  void create(DBObject skel, Object context, CSatz csatz, ASatz asatz)
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
    u.setGegenkontoBLZ(Long.toString(csatz.getBlzEndbeguenstigt()));
    u.setGegenkontoName(csatz.getNameEmpfaenger());
    u.setGegenkontoNummer(Long.toString(csatz.getKontonummer()));
    
    List<String> lines = new ArrayList<String>();
    for (int i=1;i<=csatz.getAnzahlVerwendungszwecke();++i)
    {
      lines.add(csatz.getVerwendungszweck(i));
    }
    VerwendungszweckUtil.apply(u,lines.toArray(new String[lines.size()]));
    
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
 * Revision 1.11  2011/06/07 10:07:50  willuhn
 * @C Verwendungszweck-Handling vereinheitlicht/vereinfacht - geht jetzt fast ueberall ueber VerwendungszweckUtil
 *
 * Revision 1.10  2009/06/15 08:51:16  willuhn
 * @N BUGZILLA 736
 *
 * Revision 1.9  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 * Revision 1.8  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.7  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
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