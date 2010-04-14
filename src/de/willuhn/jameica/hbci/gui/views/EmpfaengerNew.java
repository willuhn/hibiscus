/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/EmpfaengerNew.java,v $
 * $Revision: 1.19 $
 * $Date: 2010/04/14 17:44:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.controller.EmpfaengerControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Empfaenger bearbeiten.
 */
public class EmpfaengerNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Adresse bearbeiten"));
		
		final EmpfaengerControl control = new EmpfaengerControl(this);

    ColumnLayout columns = new ColumnLayout(getParent(),2);

    SimpleContainer left = new SimpleContainer(columns.getComposite());
    left.addHeadline(i18n.tr("Eigenschaften"));
    left.addLabelPair(i18n.tr("Kontoinhaber"),              control.getName());
		left.addLabelPair(i18n.tr("Kontonummer"),               control.getKontonummer());
		left.addLabelPair(i18n.tr("Bankleitzahl"),              control.getBlz());
    left.addHeadline(i18n.tr("Ausländische Bankverbindung"));
    left.addLabelPair(i18n.tr("IBAN"),                      control.getIban());
    left.addLabelPair(i18n.tr("BIC"),                       control.getBic());
    left.addLabelPair(i18n.tr("Name des Kredit-Instituts"), control.getBank());

    SimpleContainer right = new SimpleContainer(columns.getComposite(),true);
    right.addHeadline(i18n.tr("Notizen"));
    right.addPart(control.getKommentar());
    right.addLabelPair(i18n.tr("Kategorie"),control.getKategorie());

    // und noch die Abschicken-Knoepfe
    ButtonArea buttonArea = new ButtonArea(getParent(),control.isHibiscusAdresse() ? 3 : 1);
    buttonArea.addButton(new Back(false));

    new Headline(getParent(),i18n.tr("Buchungen von/an diese Adresse"));

    if (control.isHibiscusAdresse())
    {
      buttonArea.addButton(i18n.tr("Löschen"), new DBObjectDelete(),control.getAddress(),false,"user-trash-full.png");
      buttonArea.addButton(i18n.tr("Speichern"), new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          control.handleStore();
        }
      },null,true,"document-save.png");
    }
      
    TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 100; // wir verdecken sonst den Kommentar
    folder.setLayoutData(gd);
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    TabGroup tab = new TabGroup(folder,i18n.tr("Kontoauszüge"), false,1);
    control.getUmsatzListe().paint(tab.getComposite());

    // BUGZILLA 107 http://www.willuhn.de/bugzilla/show_bug.cgi?id=107
    TabGroup tab2 = new TabGroup(folder,i18n.tr("Eingezogene Sammel-Lastschriften"));
    control.getSammelLastListe().paint(tab2.getComposite());

    TabGroup tab3 = new TabGroup(folder,i18n.tr("Sammel-Überweisungen"));
    control.getSammelUeberweisungListe().paint(tab3.getComposite());
  }
}


/**********************************************************************
 * $Log: EmpfaengerNew.java,v $
 * Revision 1.19  2010/04/14 17:44:10  willuhn
 * @N BUGZILLA 83
 *
 * Revision 1.18  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.17  2009/02/18 00:35:54  willuhn
 * @N Auslaendische Bankverbindungen im Adressbuch
 *
 * Revision 1.16  2009/01/20 10:51:45  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.15  2008/11/25 00:13:47  willuhn
 * @N Erweiterte Verwendungswecke anzeigen
 * @N Notizen nicht mehr in einem separaten Tab sondern in der rechten Spalte anzeigen
 *
 * Revision 1.14  2007/04/23 21:03:48  willuhn
 * @R "getTransfers" aus Address entfernt - hat im Adressbuch eigentlich nichts zu suchen
 *
 * Revision 1.13  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.12  2006/08/17 21:46:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.10  2006/05/11 10:55:49  willuhn
 * @C Buttons nach oben verschoben
 *
 * Revision 1.9.2.1  2006/05/11 10:44:43  willuhn
 * @B bug 232
 *
 * Revision 1.9  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.8  2005/10/03 16:17:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2005/08/22 12:23:18  willuhn
 * @N bug 107
 *
 * Revision 1.6  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 * Revision 1.5  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 * Revision 1.4  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.3  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/03/04 00:52:45  web0
 * @C s/Empfaenger/Adresse/
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/29 16:16:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.3  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/