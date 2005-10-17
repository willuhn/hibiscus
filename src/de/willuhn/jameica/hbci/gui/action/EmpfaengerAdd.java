/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/EmpfaengerAdd.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/10/17 22:00:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.util.ArrayList;
import java.util.HashMap;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die ein Empfaenger dem Adressbuch hinzugefuegt wird.
 * Als Parameter kann eine Ueberweisung oder ein Umsatz uebergeben werden.
 */
public class EmpfaengerAdd implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> oder <code>Transfer</code>.
   * Die Empfaenger-Daten werden extrahiert und ein neuer Empfaenger
   * angelegt, falls dieser nicht schon existiert.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie eine Überweisung oder einen Umsatz aus"));

		if (!(context instanceof Transfer) && !(context instanceof Umsatz) && !(context instanceof Umsatz[]))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie eine Überweisung oder einen Umsatz aus"));

    ArrayList items = new ArrayList();
		try {

			if (context instanceof Transfer)
			{
				Transfer t = (Transfer) context;
        Adresse e = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
        e.setBLZ(t.getGegenkontoBLZ());
        e.setKontonummer(t.getGegenkontoNummer());
        e.setName(strip(t.getGegenkontoName()));
        items.add(e);
			}
			else if (context instanceof Umsatz)
			{
				Umsatz u = (Umsatz) context;
        Adresse e = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
        e.setBLZ(u.getEmpfaengerBLZ());
        e.setKontonummer(u.getEmpfaengerKonto());
        e.setName(strip(u.getEmpfaengerName()));
        items.add(e);
			}
      else if (context instanceof Umsatz[])
      {
        Umsatz[] list = (Umsatz[]) context;
        for (int i=0;i<list.length;++i)
        {
          Umsatz u = list[i];
          Adresse e = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
          e.setBLZ(u.getEmpfaengerBLZ());
          e.setKontonummer(u.getEmpfaengerKonto());
          e.setName(strip(u.getEmpfaengerName()));
          items.add(e);
        }
      }

      // Falls mehrere Eintraege markiert sind, kann es sein, dass einige
      // davon doppelt da sind, die fischen wir raus.
      HashMap seen = new HashMap();

      for (int i=0;i<items.size();++i)
      {
        // wir checken erstmal, ob wir den schon haben.
        Adresse e = (Adresse) items.get(i);

        if (seen.get(e.getKontonummer() + "-" + e.getBLZ()) != null)
          continue; // den haben wir schon

        seen.put(e.getKontonummer() + "-" + e.getBLZ(),e);
        DBIterator list = Settings.getDBService().createList(Adresse.class);
        list.addFilter("kontonummer = '" + e.getKontonummer() + "'");
        list.addFilter("blz = '" + e.getBLZ() + "'");
        if (list.hasNext())
        {
          YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
          d.setTitle(i18n.tr("Adresse existiert"));
          d.setText(i18n.tr("Eine Adresse mit Kontonummer {0} und BLZ {1} existiert bereits.\n" +
              "Möchten Sie den Empfänger dennoch zum Adressbuch hinzufügen?",new String[]{e.getKontonummer(),e.getBLZ()}));
          if (!((Boolean) d.open()).booleanValue()) continue;
        }
        e.store();
      }
      if (items.size() == 1)
  			GUI.getStatusBar().setSuccessText(i18n.tr("Adresse gespeichert"));
      else
        GUI.getStatusBar().setSuccessText(i18n.tr("Adressen gespeichert"));
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