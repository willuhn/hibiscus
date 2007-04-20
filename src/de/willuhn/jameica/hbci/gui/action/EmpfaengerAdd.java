/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/EmpfaengerAdd.java,v $
 * $Revision: 1.9 $
 * $Date: 2007/04/20 14:49:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.Adresse;
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
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> oder <code>Transfer</code> (bzw. Arrays davon)
   * sowie Objekte des Typs <code>Adresse</code> bzw. <code>Adresse[]</code>.
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
        !(context instanceof Adresse) &&
        !(context instanceof Adresse[]) &&
        !(context instanceof Address) &&
        !(context instanceof Address[]) &&
        !(context instanceof Umsatz) &&
        !(context instanceof Umsatz[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein oder mehrere Aufträge aus"));

    ArrayList items = new ArrayList();
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
        for (int i=0;i<list.length;++i)
        {
          Transfer t = list[i];
          items.add(create(t.getGegenkontoName(),t.getGegenkontoNummer(),t.getGegenkontoBLZ()));
        }
      }
      ///////////////////////////////////////////////////////////////
      // Adressen
      else if (context instanceof Adresse)
      {
        items.add(context);
      }
      else if (context instanceof Adresse[])
      {
        Adresse[] list = (Adresse[]) context;
        for (int i=0;i<list.length;++i)
        {
          items.add(list[i]);
        }
      }
      ///////////////////////////////////////////////////////////////
      // Address
      else if (context instanceof Address)
      {
        Address a = (Address) context;
        items.add(create(a.getName(),a.getKontonummer(),a.getBLZ()));
      }
      else if (context instanceof Address[])
      {
        Address[] list = (Address[]) context;
        for (int i=0;i<list.length;++i)
        {
          Address a = list[i];
          items.add(create(a.getName(),a.getKontonummer(),a.getBLZ()));
        }
      }
      ///////////////////////////////////////////////////////////////
      // Umsatz
			else if (context instanceof Umsatz)
			{
				Umsatz u = (Umsatz) context;
        items.add(create(u.getEmpfaengerName(),u.getEmpfaengerKonto(),u.getEmpfaengerBLZ()));
			}
      else if (context instanceof Umsatz[])
      {
        Umsatz[] list = (Umsatz[]) context;
        for (int i=0;i<list.length;++i)
        {
          Umsatz u = list[i];
          items.add(create(u.getEmpfaengerName(),u.getEmpfaengerKonto(),u.getEmpfaengerBLZ()));
        }
      }
      ///////////////////////////////////////////////////////////////

      if (items.size() == 0)
        return;
      
      // Falls mehrere Eintraege markiert sind, kann es sein, dass einige
      // davon doppelt da sind, die fischen wir raus.
      HashMap seen = new HashMap();
      AddressbookService book = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");

      String question = i18n.tr("Eine Adresse mit dem Namen {0} (Kto. {1}, BLZ {2}) existiert bereits im Adressbuch.\n" +
                        "Möchten Sie die Adresse dennoch hinzufügen?");

      int count = 0;
      for (int i=0;i<items.size();++i)
      {
        // wir checken erstmal, ob wir den schon haben.
        Adresse e = (Adresse) items.get(i);

        if (e.getName() == null || e.getName().length() == 0)
        {
          Logger.warn("address [kto. " + e.getKontonummer() + ", blz " + e.getBLZ() + " has no name, skipping");
          continue;
        }
        String key = e.getName() + "-" + e.getKontonummer() + "-" + e.getBLZ();
        if (seen.get(key) != null)
          continue; // den hatten wir schonmal. Und wir wollen den User doch nicht immer wieder fragen

        seen.put(key,e);

        if (book.contains(e) != null)
        {
          if (!Application.getCallback().askUser(question,new String[]{e.getName(),e.getKontonummer(),e.getBLZ()}))
            continue;
        }
        
        // OK, speichern
        e.store();
        count++;
      }
      if (count > 0)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Adresse{0} gespeichert",(count > 1 ? "n" : "")), StatusBarMessage.TYPE_SUCCESS));
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
  private Adresse create(String name, String kontonummer, String blz) throws RemoteException
  {
    Adresse e = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
    e.setName(strip(name));
    e.setKontonummer(kontonummer);
    e.setBLZ(blz);
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