/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/View.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/27 22:23:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Konfiguration eines Passports vom Typ DDV.
 */
public class View extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Eigenschaften des Chipkartenlesers"));

		final Controller control = new Controller(this);

		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
		
		group.addLabelPair(i18n.tr("Typ"),										control.getType());
		group.addLabelPair(i18n.tr("Bezeichnung"),						control.getName());
		group.addLabelPair(i18n.tr("Port des Lesers"),				control.getPort());
		group.addLabelPair(i18n.tr("CTAPI Treiber-Datei"),		control.getCTAPI());
		group.addLabelPair(i18n.tr("Index des Lesers"),				control.getCTNumber());
		group.addLabelPair(i18n.tr("Index des HBCI-Zugangs"),	control.getEntryIndex());

		group.addCheckbox(control.getBio(), 		i18n.tr("Biometrische Verfahren verwenden"));
		group.addCheckbox(control.getSoftPin(), i18n.tr("Tastatur des PCs zur PIN-Eingabe verwenden"));
		
		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addCustomButton(i18n.tr("Kartenleser testen"), new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				control.handleTest();
			}
		});
		buttonArea.addCancelButton(control);
		buttonArea.addDeleteButton(control);
		buttonArea.addStoreButton(control);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: View.java,v $
 * Revision 1.1  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.12  2004/04/19 22:05:52  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.11  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/19 01:44:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.6  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.4  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 00:38:41  willuhn
 * *** empty log message ***
 *
 **********************************************************************/