/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/PassportDetails.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/02/27 01:10:18 $
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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.controller.PassportControlDDV;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportDDV;
import de.willuhn.jameica.hbci.rmi.PassportType;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;

/**
 * Dialog, ueber den die Passports konfiguriert werden koennen.
 */
public class PassportDetails extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		GUI.setTitleText(I18N.tr("Eigenschaften des Sicherheitsmediums"));

		Passport p = (Passport) getCurrentObject();
		
		// Das ist erstmal nur ein anonymer Passport. Wir muessen noch die
		// zugehoerige Impl suchen.
		// TODO: Das ist noch gar nicht schoen.
		PassportType pt = p.getPassportType();
		String clazz = pt.getImplementor();
		p = (Passport) Settings.getDatabase().createObject(MultipleClassLoader.load(clazz),p.getID());
		if (p.isNewObject())
			p.setPassportType(pt); // ist ein neuer Passport - der hat keinen Typ nach dem Laden
		setCurrentObject(p);


		///////////////////////////////////////////////////////////////////////////
		// DDV
  	if (p instanceof PassportDDV)
  	{
			final PassportControlDDV control = new PassportControlDDV(this);

			LabelGroup group = new LabelGroup(getParent(),I18N.tr("Eigenschaften"));
			
			group.addLabelPair(I18N.tr("Typ"),										control.getType());
			group.addLabelPair(I18N.tr("Bezeichnung"),						control.getName());
			group.addLabelPair(I18N.tr("Port des Lesers"),				control.getPort());
			group.addLabelPair(I18N.tr("Index des Lesers"),				control.getCTNumber());
			group.addLabelPair(I18N.tr("Index des HBCI-Zugangs"),	control.getEntryIndex());

			group.addCheckbox(control.getBio(), 		I18N.tr("Biometrische Verfahren verwenden"));
			group.addCheckbox(control.getSoftPin(), I18N.tr("Tastatur des PCs zur PIN-Eingabe verwenden"));
			
			// und noch die Abschicken-Knoepfe
			ButtonArea buttonArea = new ButtonArea(getParent(),3);
			buttonArea.addCustomButton(I18N.tr("Kartenleser testen"), new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					control.handleTest();
				}
			});
			buttonArea.addCancelButton(control);
			buttonArea.addStoreButton(control);

  	}
		//
		///////////////////////////////////////////////////////////////////////////
  	else {
			Application.getLog().error("choosen passport not found");
			GUI.setActionText(I18N.tr("Das ausgewählte Sicherheitsmedium konnte nicht geladen werden."));
  		
  	}

  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: PassportDetails.java,v $
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