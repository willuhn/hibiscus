/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/UmsatzImport.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/09 17:39:49 $
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Style;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UmsatzImportControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class UmsatzImport extends AbstractView {

	private UmsatzImportControl control = null;
	private I18N i18n = null;

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

  	i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		control = new UmsatzImportControl(this);
		GUI.getView().setTitle(i18n.tr("Import von Umsätzen"));

		////////////////////////////////////////////////////////
		// Log
		Group group2 = new Group(getParent(), SWT.NONE);
		group2.setBackground(Style.COLOR_BG);
		group2.setText(i18n.tr("Protokoll"));
		group2.setFont(Style.FONT_H2);

		GridLayout layout2 = new GridLayout(2, false);
		group2.setLayout(layout2);
		group2.setLayoutData(new GridData(GridData.FILL_BOTH));
		TextPart t = control.getLog();
		t.paint(group2);
		//
		////////////////////////////////////////////////////////

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(getParent(),2);
		buttonArea.addCustomButton(i18n.tr("abbrechen"), new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				if (!control.isRunning())
					return;
				MessageBox box = new MessageBox(GUI.getShell(),SWT.ICON_WARNING | SWT.YES | SWT.NO);
				box.setText(i18n.tr("Import läuft"));
				box.setMessage(i18n.tr("Der Import läuft gerade.\n" +
				"Wollen Sie den Vorgang wirklich abbrechen?"));

				if (box.open() == SWT.YES)
					control.cancel();
			}
		});
		buttonArea.addCustomButton(i18n.tr("Import starten"), new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				control.start();
			}
		});

		
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
		if (!control.isRunning())
			return;

		MessageBox box = new MessageBox(GUI.getShell(),SWT.ICON_WARNING | SWT.YES | SWT.NO);
		box.setText(i18n.tr("Import läuft"));
		box.setMessage(i18n.tr("Der Import läuft gerade.\n" +
													 "Wollen Sie den Vorgang wirklich abbrechen?"));

		if (box.open() == SWT.YES)
		{
			control.cancel();
			return;
		}

		throw new ApplicationException();
  }

}


/**********************************************************************
 * $Log: UmsatzImport.java,v $
 * Revision 1.1  2004/05/09 17:39:49  willuhn
 * *** empty log message ***
 *
 **********************************************************************/