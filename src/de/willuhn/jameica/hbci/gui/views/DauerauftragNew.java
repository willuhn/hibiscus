/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/DauerauftragNew.java,v $
 * $Revision: 1.13 $
 * $Date: 2011/04/06 08:17:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DauerauftragDelete;
import de.willuhn.jameica.hbci.gui.action.DauerauftragExecute;
import de.willuhn.jameica.hbci.gui.controller.DauerauftragControl;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class DauerauftragNew extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		final DauerauftragControl control = new DauerauftragControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Dauerauftrag bearbeiten"));
		
    Container konten = new SimpleContainer(getParent());
    konten.addHeadline(i18n.tr("Konten"));
		konten.addLabelPair(i18n.tr("Persönliches Konto"),			  control.getKontoAuswahl());

    ColumnLayout columns = new ColumnLayout(getParent(),2);

    {
      // Links
      Container left = new SimpleContainer(columns.getComposite());
      left.addHeadline(i18n.tr("Empfänger"));
      left.addLabelPair(i18n.tr("Name"),                      control.getEmpfaengerName());
      left.addLabelPair(i18n.tr("Kontonummer"),               control.getEmpfaengerKonto());
      left.addLabelPair(i18n.tr("BLZ"),                       control.getEmpfaengerBlz());
      left.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("In Adressbuch übernehmen"));
    }
    {
      // Rechts
      Container right = new SimpleContainer(columns.getComposite());
      right.addHeadline(i18n.tr("Turnus"));
      right.addLabelPair(i18n.tr("Zahlungsturnus"),           control.getTurnus());
      right.addLabelPair(i18n.tr("Erste Zahlung"),            control.getErsteZahlung());
      right.addLabelPair(i18n.tr("Letzte Zahlung"),           control.getLetzteZahlung());
    }
		
    Container details = new SimpleContainer(getParent());
    details.addHeadline(i18n.tr("Details"));
    details.addLabelPair(i18n.tr("Verwendungszweck"),          control.getZweck());
    details.addLabelPair(i18n.tr("weiterer Verwendungszweck"), control.getZweck2());
    details.addLabelPair(i18n.tr("Textschlüssel"),            control.getTextSchluessel());
	  details.addLabelPair(i18n.tr("Betrag"),                    control.getBetrag());
	  details.addSeparator();
	  details.addLabelPair(i18n.tr("Auftragsnummer"),            control.getOrderID());
    
		final Dauerauftrag da = (Dauerauftrag) control.getTransfer();
		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		String s = i18n.tr("Jetzt ausführen...");
		if (da.isActive())
			s = "Jetzt aktualisieren...";

    buttonArea.addButton(new Back(false));
		buttonArea.addButton(i18n.tr("Löschen"),	 		 new DauerauftragDelete(), da, false,"user-trash-full.png");
		buttonArea.addButton(s,										 		 new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	if (control.handleStore())
					new DauerauftragExecute().handleAction(da);
      }
    },null,false,"emblem-important.png");
		buttonArea.addButton(i18n.tr("Speichern"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				control.handleStore();
			}
    },null,true,"document-save.png");
  }
}


/**********************************************************************
 * $Log: DauerauftragNew.java,v $
 * Revision 1.13  2011/04/06 08:17:16  willuhn
 * @N Detail-Anzeige zweispaltig, damit sie besser auf kleinere Bildschirme passt - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=74593#74593
 *
 * Revision 1.12  2010-09-24 12:22:04  willuhn
 * @N Thomas' Patch fuer Textschluessel in Dauerauftraegen
 *
 * Revision 1.11  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.10  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.9  2009/02/24 23:51:01  willuhn
 * @N Auswahl der Empfaenger/Zahlungspflichtigen jetzt ueber Auto-Suggest-Felder
 *
 * Revision 1.8  2009/01/20 10:51:45  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.7  2006/03/27 16:46:21  willuhn
 * @N GUI polish
 *
 * Revision 1.6  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.5  2005/10/17 22:00:44  willuhn
 * @B bug 143
 *
 * Revision 1.4  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/03/04 00:52:03  web0
 * @B typo
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.6  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.4  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/