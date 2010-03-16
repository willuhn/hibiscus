/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/KontoUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/16 13:43:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Hilfsklasse mit statischen Funktionen fuer Konten.
 */
public class KontoUtil
{
  /**
   * Sucht das Konto in der Datenbank.
   * Die Funktion entfernt bei der Suche selbstaendig fuehrende Nullen in
   * Kontonummern.
   * @param kontonummer die Kontonummer.
   * @param blz die BLZ.
   * @return das gefundene Konto oder NULL, wenn es nicht existiert.
   * @throws RemoteException
   */
  public static Konto find(String kontonummer, String blz) throws RemoteException
  {
    if (kontonummer == null || kontonummer.length() == 0)
      return null;
    if (blz == null || blz.length() == 0)
      return null;
    
    // BUGZILLA 365
    // Fuehrende Nullen schneiden wir ab
    if (kontonummer.startsWith("0"))
      kontonummer = kontonummer.replaceAll("^0{1,}","");

    // Kontonummer bestand offensichtlich nur aus Nullen ;)
    if (kontonummer.length() == 0)
      return null;
    
    DBService service = de.willuhn.jameica.hbci.Settings.getDBService();
    DBIterator konten = service.createList(Konto.class);
    konten.addFilter("kontonummer like ?", new Object[]{"%" + kontonummer});
    konten.addFilter("blz = ?", new Object[]{blz});
    while (konten.hasNext())
    {
      // Fuehrende Nullen abschneiden und dann vergleichen
      Konto test = (Konto) konten.next();
      String kTest = test.getKontonummer();
      if (kTest == null || kTest.length() == 0)
        continue;
      if (kTest.startsWith("0"))
        kTest = kTest.replaceAll("^0{1,}","");
      
      // Mal schauen, ob die Kontonummern jetzt uebereinstimmen
      if (kTest.equals(kontonummer))
        return test;
    }
    
    return null;
  }
}



/**********************************************************************
 * $Log: KontoUtil.java,v $
 * Revision 1.1  2010/03/16 13:43:56  willuhn
 * @N CSV-Import von Ueberweisungen und Lastschriften
 * @N Versionierbarkeit von serialisierten CSV-Profilen
 *
 **********************************************************************/