/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/PassportDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/27 01:10:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.Input;
import de.willuhn.jameica.gui.parts.SelectInput;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.PassportType;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;

/**
 * Dialog für die Auswahl des Passports.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class PassportDialog extends AbstractDialog {

	private Input auswahl;
	String id = null;

  /**
   * ct.
   * @param position Position des Dialogs.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_CENTER
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_MOUSE
   */
  public PassportDialog(int position) {
    super(position);
    setTitle(I18N.tr("Auswahl des Mediums"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {

		// wir laden den ausgewaehlten Passport
		Passport p = (Passport) Settings.getDatabase().createObject(Passport.class, id);

		// TODO Schoener machen
		PassportType pt = p.getPassportType();
		String clazz = pt.getImplementor();
		return (Passport) Settings.getDatabase().createObject(MultipleClassLoader.load(clazz),p.getID());
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception {
		// Composite um alles drumrum.
		Composite comp = new Composite(parent,SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(3,false));
		
		// Text
		CLabel label = new CLabel(comp,SWT.WRAP);
		label.setText(I18N.tr("Bitte wählen Sie ein Sicherheitsmedium aus."));
		GridData grid = new GridData(GridData.FILL_HORIZONTAL);
		grid.horizontalSpan = 3;
		label.setLayoutData(grid);
		
		// Label vor Eingabefeld
		CLabel pLabel = new CLabel(comp,SWT.NONE);
		pLabel.setText(I18N.tr("verfügbare Medien"));
		pLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		// Liste der Passport-Typen holen
		Composite c = new Composite(comp,SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		c.setLayoutData(gd);
		c.setLayout(new GridLayout(2,false));
		DBIterator list = Settings.getDatabase().createList(Passport.class);
		auswahl = new SelectInput(list,null);
		auswahl.paint(c,SWT.DEFAULT);

		// Dummy-Label damit die Buttons buendig unter dem Eingabefeld stehen
		Label dummy = new Label(comp,SWT.NONE);
		dummy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// OK-Button
		Button button = new Button(comp, SWT.FLAT);
		button.setText(I18N.tr("OK"));
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				id = auswahl.getValue();
				close();
			}
		});

		// Abbrechen-Button
		Button cancel = new Button(comp,SWT.FLAT);
		cancel.setText(I18N.tr("Abbrechen"));
		cancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cancel.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				throw new RuntimeException("Dialog abgebrochen");
			}
		});

		addShellListener(new ShellListener() {
			public void shellClosed(ShellEvent e) {
				throw new RuntimeException("dialog cancelled via close button");
			}
			public void shellActivated(ShellEvent e) {};
			public void shellDeactivated(ShellEvent e) {};
			public void shellDeiconified(ShellEvent e) {};
			public void shellIconified(ShellEvent e) {};
		});
  }

}


/**********************************************************************
 * $Log: PassportDialog.java,v $
 * Revision 1.1  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.2  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 **********************************************************************/