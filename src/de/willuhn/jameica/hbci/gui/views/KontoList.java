/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/KontoList.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/13 17:12:15 $
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
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Bankverbindungen an.
 */
public class KontoList extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		final KontoControl control = new KontoControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Bankverbindungen"));

		try {

			control.getKontoListe().paint(getParent());
			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addButton(i18n.tr("Neue Bankverbindung"),new KontoNew());

			LabelGroup group = new LabelGroup(getParent(),i18n.tr("Konten aus Medium lesen"));
			group.addLabelPair(i18n.tr("Sicherheitsmedium"),control.getPassportAuswahl());

			ButtonArea c = group.createButtonArea(2);
			c.addButton(i18n.tr("Medium konfigurieren"), new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
					control.handleConfigurePassport();
        }
      });
			c.addButton(i18n.tr("Daten aus Medium lesen"), new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
					control.handleReadFromPassport();
        }
      });

		}
		catch (Exception e)
		{
			Logger.error("error while loading konto list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Bankverbindungen."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.19  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.15  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.13  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.12  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/06/03 00:23:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.9  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.8  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.7  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.5  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.4  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.3  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/