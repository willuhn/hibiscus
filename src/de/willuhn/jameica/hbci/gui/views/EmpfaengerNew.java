/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/EmpfaengerNew.java,v $
 * $Revision: 1.12 $
 * $Date: 2006/08/17 21:46:16 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
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
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));

		group.addLabelPair(i18n.tr("Kontonummer"),			    		control.getKontonummer());
		group.addLabelPair(i18n.tr("Bankleitzahl"),			    		control.getBlz());
		group.addLabelPair(i18n.tr("Name"),			    						control.getName());

    LabelGroup comment = new LabelGroup(getParent(),i18n.tr("Kommentar"),true);
    comment.addPart(control.getKommentar());

    // und noch die Abschicken-Knoepfe
    ButtonArea buttonArea = new ButtonArea(getParent(),3);
    buttonArea.addButton(i18n.tr("Zurück"),new Back());
    buttonArea.addButton(i18n.tr("Löschen"), new DBObjectDelete(), control.getEmpfaenger());
    buttonArea.addButton(i18n.tr("Speichern"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true);
    

    new Headline(getParent(),i18n.tr("Buchungen von/an diese Adresse"));

    TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    TabGroup tab = new TabGroup(folder,i18n.tr("Überweisungen"), false,1);
    control.getUmsatzListe().paint(tab.getComposite());

    // BUGZILLA 107 http://www.willuhn.de/bugzilla/show_bug.cgi?id=107
    DBIterator list = control.getEmpfaenger().getSammellastBuchungen();
    if (list.size() > 0)
    {
      TabGroup tab2 = new TabGroup(folder,i18n.tr("Eingezogene Sammel-Lastschriften"));
      control.getSammelLastListe().paint(tab2.getComposite());
    }
    DBIterator list2 = control.getEmpfaenger().getSammelUeberweisungBuchungen();
    if (list2.size() > 0)
    {
      TabGroup tab3 = new TabGroup(folder,i18n.tr("Sammel-Überweisungen"));
      control.getSammelUeberweisungListe().paint(tab3.getComposite());
    }
  }
}


/**********************************************************************
 * $Log: EmpfaengerNew.java,v $
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