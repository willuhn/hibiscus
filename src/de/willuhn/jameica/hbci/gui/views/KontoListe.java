/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/KontoListe.java,v $
 * $Revision: 1.15 $
 * $Date: 2004/07/21 23:54:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Zeigt eine Liste mit den vorhandenen Bankverbindungen an.
 */
public class KontoListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		final KontoControl control = new KontoControl(this);

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Bankverbindungen"));

		try {

			control.getKontoListe().paint(getParent());
			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addCreateButton(i18n.tr("Neue Bankverbindung"),control);

			LabelGroup group = new LabelGroup(getParent(),i18n.tr("Konten aus Medium lesen"));
			group.addLabelPair(i18n.tr("Sicherheitsmedium"),control.getPassportAuswahl());

			ButtonArea c = group.createButtonArea(2);
			c.addCustomButton(i18n.tr("Medium konfigurieren"), new Listener()
      {
        public void handleEvent(Event event)
        {
					control.handleConfigurePassport();
        }
      });
			c.addCustomButton(i18n.tr("Daten aus Medium lesen"), new Listener()
      {
        public void handleEvent(Event event)
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
 * $Log: KontoListe.java,v $
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