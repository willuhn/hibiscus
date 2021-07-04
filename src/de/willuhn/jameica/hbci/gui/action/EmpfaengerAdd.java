/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die ein oder mehrere Adressen dem Adressbuch hinzugefuegt werden.
 * Als Parameter koennen Transfer, Umsaetze oder Adressen uebergeben werden.
 */
public class EmpfaengerAdd implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Transfer</code> (bzw. Arrays davon)
   * Ausserdem Objekte des Typs <code>Address</code> sowie <code>Address[]</code> 
   * Die Empfaenger-Daten werden extrahiert und in der Datenbank gespeichert,
   * falls sie nicht schon existieren.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie ein oder mehrere Aufträge aus"));

		if (!(context instanceof Transfer) &&
        !(context instanceof Transfer[]) &&
        !(context instanceof Address) &&
        !(context instanceof Address[]) &&
        !(context instanceof Umsatz) &&
        !(context instanceof Umsatz[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein oder mehrere Aufträge aus"));

    List<HibiscusAddress> items = new ArrayList<HibiscusAddress>();
		try {

      ///////////////////////////////////////////////////////////////
      // Transfers
			if (context instanceof Transfer)
			{
				Transfer t = (Transfer) context;
        items.add(create(t.getGegenkontoName(),t.getGegenkontoNummer(),t.getGegenkontoBLZ()));
			}
      else if (context instanceof Transfer[])
      {
        Transfer[] list = (Transfer[]) context;
        for (Transfer t : list)
        {
          items.add(create(t.getGegenkontoName(),t.getGegenkontoNummer(),t.getGegenkontoBLZ()));
        }
      }
      ///////////////////////////////////////////////////////////////
      // Hibiscus-Adressen
      else if (context instanceof HibiscusAddress)
      {
        items.add((HibiscusAddress)context);
      }
      else if (context instanceof HibiscusAddress[])
      {
        HibiscusAddress[] list = (HibiscusAddress[]) context;
        items.addAll(Arrays.asList(list));
      }
      ///////////////////////////////////////////////////////////////
      // Address
      else if (context instanceof Address)
      {
        Address a = (Address) context;
        items.add(create(a.getName(),a.getKontonummer(),a.getBlz()));
      }
      else if (context instanceof Address[])
      {
        Address[] list = (Address[]) context;
        for (Address a : list)
        {
          items.add(create(a.getName(),a.getKontonummer(),a.getBlz()));
        }
      }
      ///////////////////////////////////////////////////////////////

      if (items.size() == 0)
        return;
      
      // Falls mehrere Eintraege markiert sind, kann es sein, dass einige
      // davon doppelt da sind, die fischen wir raus.
      HashMap<String, HibiscusAddress> seen = new HashMap<>();
      AddressbookService book = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");

      int count = 0;
      for (HibiscusAddress e : items)
      {
        // wir checken erstmal, ob wir den schon haben.
        if (e.getName() == null || e.getName().length() == 0)
        {
          Logger.warn("address [kto. " + e.getKontonummer() + ", blz " + e.getBlz() + " has no name, skipping");
          continue;
        }
        String key = e.getName() + "-" + e.getKontonummer() + "-" + e.getBlz() + "-" + e.getIban();
        if (seen.get(key) != null)
          continue; // den hatten wir schonmal. Und wir wollen den User doch nicht immer wieder fragen

        seen.put(key,e);

        if (book.contains(e) != null)
        {
          Logger.debug("address [iban. " + e.getIban() + ", bic " + e.getBic() + " already exists, skipping");
          continue;
        }

        // OK, speichern
        e.store();
        count++;
      }
      if (count > 0)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Adresse{0} gespeichert",(count > 1 ? "n" : "")), StatusBarMessage.TYPE_SUCCESS));
      else
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Adresse existiert bereits"), StatusBarMessage.TYPE_INFO));
		}
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while storing empfaenger",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Empfängers"));
		}
  }

  /**
   * Erzeugt ein Adress-Objekt aus den uebergebenen Daten.
   * @param name Name.
   * @param kontonummer Kontonummer.
   * @param blz BLZ.
   * @return das Adress-Objekt.
   * @throws RemoteException
   */
  private HibiscusAddress create(String name, String kontonummer, String blz) throws RemoteException
  {
    HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
    e.setName(strip(name));
    if (kontonummer != null && kontonummer.matches("[a-zA-Z]{2}.*")) // italienische IBANs haben z.Bsp. mittendrin auch noch Buchstaben
      e.setIban(kontonummer);
    else
      e.setKontonummer(kontonummer);
    
    if (blz != null && blz.matches("[a-zA-Z]{6}.*"))
      e.setBic(blz);
    else
      e.setBlz(blz);
    return e;
  }

  // BUGZILLA 78 http://www.willuhn.de/bugzilla/show_bug.cgi?id=78
  /**
   * Kuerzt den String um die angegebene Laenge.
   * @param s String
   * @return gekuerzter String.
   */
  private String strip(String s)
  {
    if (s == null || s.length() < HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH)
      return s;
    return s.substring(0,HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
  }
}


/**********************************************************************
 * $Log: EmpfaengerAdd.java,v $
 * Revision 1.12  2009/02/18 00:43:48  willuhn
 * @N Automatische Erkennung von IBAN/BIC beim Hinzufuegen von Adressen
 *
 * Revision 1.11  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.10  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.9  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.8  2006/08/23 09:45:13  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.7  2005/10/17 22:00:44  willuhn
 * @B bug 143
 *
 * Revision 1.6  2005/06/27 15:58:01  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/05/09 12:24:20  web0
 * @N Changelog
 * @N Support fuer Mehrfachmarkierungen
 * @N Mehere Adressen en bloc aus Umsatzliste uebernehmen
 *
 * Revision 1.4  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.3  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.2  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/