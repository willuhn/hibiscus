/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/KontoListe.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/04/12 19:15:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.hbci.gui.dialogs.NewPassportDialog;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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


		DBIterator passports = Settings.getDatabase().createList(Passport.class);
		if (passports.size() == 0)
		{
			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Kein Sicherheitsmedium"));
			d.setText(i18n.tr("Es ist noch kein Sicherheitsmedium eingerichtet. " +				"Möchten Sie dies jetzt einrichten?"));
			Boolean choice = (Boolean) d.open();
			if (choice.booleanValue())
			{
				NewPassportDialog d2 = new NewPassportDialog(NewPassportDialog.POSITION_MOUSE);
				try {
					GUI.startView(PassportDetails.class.getName(),d2.open());
					return;
				}
				catch (Exception e2)
				{
					// Dialog wurde abgebrochen
					Application.getLog().info(e2.getMessage());
				}
			}
			else {
				LabelInput dummy = new LabelInput(i18n.tr("Kein Sicherheitsmedium vorhanden"));
				dummy.paint(getParent());
				return;
			}
		}

		try {

			control.getKontoListe().paint(getParent());
			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addCreateButton(i18n.tr("Neue Bankverbindung"),control);


			LabelGroup group = new LabelGroup(getParent(),i18n.tr("Konten aus Medium lesen"));
			group.addLabelPair(i18n.tr("Sicherheitsmedium"),control.getPassportAuswahl());

			ButtonArea c = group.createButtonArea(1);
			c.addCustomButton(i18n.tr("Daten aus Medium lesen"), new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					control.handleReadFromPassport();
				}
			});

		}
		catch (Exception e)
		{
			Application.getLog().error("error while loading konto list",e);
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