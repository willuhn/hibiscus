/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/DTAUSSammelTransferImporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/06/08 22:29:47 $
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

import de.jost_net.OBanToo.Dtaus.CSatz;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * DTAUS-Importer fuer die einzelnen Buchungen von Sammel-Ueberweisungen und Sammel-Lastschriften.
 */
public class DTAUSSammelTransferImporter extends AbstractDTAUSImporter
{
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
    // Bevor wir den Import starten, checken wir, ob der Sammeltransfer schon
    // existiert und ein Konto hat. Das brauchen wir, um die einzelnen Buchungen
    // dann importieren zu koennen.
    if (context == null || !(context instanceof SammelTransfer))
      throw new ApplicationException(i18n.tr("Bitte wählen einen Sammel-Auftrag, in den die Buchungen importiert werden sollen"));
    
    super.doImport(context, format, is, monitor);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSImporter#fill(de.willuhn.datasource.rmi.DBObject, de.willuhn.datasource.GenericObject, de.jost_net.OBanToo.Dtaus.CSatz)
   */
  void fill(DBObject skel, GenericObject context, CSatz csatz)
    throws RemoteException, ApplicationException
  {
    // Wir verlassen uns hier einfach drauf, dass es sich bei dem
    // Skelet um eine SammelTransferBuchung handelt. Schliesslich haben wir
    // in getSupportedObjectTypes nur solche angegeben
    SammelTransferBuchung t = (SammelTransferBuchung) skel;

    t.setSammelTransfer((SammelTransfer)context);
    t.setBetrag(csatz.getBetragInEuro());
    t.setGegenkontoBLZ(Long.toString(csatz.getBlzEndbeguenstigt()));
    t.setGegenkontoName(csatz.getNameEmpfaenger());
    t.setGegenkontoNummer(Long.toString(csatz.getKontonummer()));
    t.setZweck(csatz.getVerwendungszweck(1));
    
    int z = csatz.getAnzahlVerwendungszwecke();
    if (z > 1)
      t.setZweck2(csatz.getVerwendungszweck(2));
  }


  /**
   * @see de.willuhn.jameica.hbci.io.AbstractDTAUSIO#getSupportedObjectTypes()
   */
  Class[] getSupportedObjectTypes()
  {
    return new Class[]
      {
        SammelLastBuchung.class,
        SammelUeberweisungBuchung.class
      };
  }
}


/*********************************************************************
 * $Log: DTAUSSammelTransferImporter.java,v $
 * Revision 1.1  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 **********************************************************************/