/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/KontoNew.java,v $
 * $Revision: 1.16.2.1 $
 * $Date: 2006/05/11 11:54:38 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.KontoDelete;
import de.willuhn.jameica.hbci.gui.action.KontoFetchUmsaetze;
import de.willuhn.jameica.hbci.gui.action.ProtokollList;
import de.willuhn.jameica.hbci.gui.action.UmsatzList;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bankverbindung bearbeiten.
 */
public class KontoNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {
		
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    final KontoControl control = new KontoControl(this);

    Konto k = control.getKonto();
    if (k != null)
    {
      String s1 = k.getBezeichnung();
      if (s1 == null) s1 = "";

      String s2 = k.getKontonummer();
      if (s2 == null) s2 = "";

      GUI.getView().setTitle(i18n.tr("Konto-Details: {0} [Kto.-Nr.: {1}]",new String[]{s1,s2}));
    }
    else
  		GUI.getView().setTitle(i18n.tr("Konto-Details"));

		try {

			LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));

			group.addLabelPair(i18n.tr("Kontonummer"),			    		control.getKontonummer());
			group.addLabelPair(i18n.tr("Bankleitzahl"),			    		control.getBlz());
			group.addLabelPair(i18n.tr("Bezeichnung des Kontos"),		control.getBezeichnung());
			group.addLabelPair(i18n.tr("Kontoinhaber"),			    		control.getName());
			group.addLabelPair(i18n.tr("Kundennummer"),							control.getKundennummer());
			group.addLabelPair(i18n.tr("Sicherheitsmedium"),    		control.getPassportAuswahl());
      group.addCheckbox(control.getSynchronize(),i18n.tr("Konto in Synchronisierung einbeziehen"));
			group.addSeparator();
      group.addLabelPair(i18n.tr("Saldo"),                    control.getSaldo());

			// und noch die Abschicken-Knoepfe
			ButtonArea buttonArea = group.createButtonArea(4);
      buttonArea.addButton(control.getSynchronizeOptions());
      buttonArea.addButton(i18n.tr("Protokoll des Kontos"),new ProtokollList(),control.getKonto());
			buttonArea.addButton(i18n.tr("Konto löschen"),new KontoDelete(),control.getKonto());
			buttonArea.addButton(i18n.tr("Speichern"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
        	control.handleStore();
        }
      });

      
      TabFolder folder = new TabFolder(getParent(), SWT.NONE);
      folder.setLayoutData(new GridData(GridData.FILL_BOTH));
      folder.setBackground(Color.BACKGROUND.getSWTColor());

      TabGroup tab = new TabGroup(folder,i18n.tr("Umsätze der letzten {0} Tage",""+HBCIProperties.UMSATZ_DEFAULT_DAYS), false,1);
      control.getUmsatzList().paint(tab.getComposite());

      TabGroup tab2 = new TabGroup(folder,i18n.tr("Saldo im Verlauf"));
      control.getUmsatzChart().paint(tab2.getComposite());

      control.init();

      ButtonArea buttons = new ButtonArea(getParent(),3);
      buttons.addButton(i18n.tr("Zurück"),new Back(),null,true);
      buttons.addButton(i18n.tr("Saldo und Umsätze abrufen"), new KontoFetchUmsaetze(),control.getKonto());
      buttons.addButton(i18n.tr("Alle Umsätze anzeigen"),     new UmsatzList(),control.getKonto());

		}
		catch (RemoteException e)
		{
			Logger.error("error while reading konto",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Bankverbindungsdaten."));
		}

  }
}


/**********************************************************************
 * $Log: KontoNew.java,v $
 * Revision 1.16.2.1  2006/05/11 11:54:38  willuhn
 * @B typo s/Ktr/Kto/
 *
 * Revision 1.16  2006/03/21 00:43:14  willuhn
 * @B bug 209
 *
 * Revision 1.15  2006/03/09 18:24:05  willuhn
 * @N Auswahl der Tage in Umsatz-Chart
 *
 * Revision 1.14  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.13  2005/12/16 16:35:31  willuhn
 * @N Filter UmsatzList width regular expressions
 *
 * Revision 1.12  2005/12/12 15:46:55  willuhn
 * @N Hibiscus verwendet jetzt Birt zum Erzeugen der Charts
 *
 * Revision 1.11  2005/11/14 21:41:02  willuhn
 * @B bug 5
 *
 * Revision 1.10  2005/07/29 16:48:13  web0
 * @N Synchronize
 *
 * Revision 1.9  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 * Revision 1.8  2005/07/11 14:08:10  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/07/11 14:03:42  web0
 * *** empty log message ***
 *
 * Revision 1.6  2005/07/11 13:51:49  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/21 21:48:24  web0
 * @B bug 80
 *
 * Revision 1.4  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.3  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.2  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.24  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.23  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/10/29 00:32:32  willuhn
 * @N HBCI job restrictions
 *
 * Revision 1.21  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.19  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.17  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.15  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.13  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.12  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.11  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.8  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.7  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.6  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.5  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 15:40:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/