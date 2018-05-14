/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Konfiguration eines Passports vom Typ RDH.
 */
public class View extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		GUI.getView().setTitle(i18n.tr("Schlüsseldateien"));

		final Controller control = new Controller(this);

    Container c = new SimpleContainer(getParent());
    c.addText(i18n.tr("Zum Erstellen eines neuen INI-Briefes klicken Sie auf \"Neuen Schlüssel erstellen\". " +
    		              "Wenn Sie eine existierende Schlüsseldatei (z.Bsp. aus einem anderen Programm) " +
    		              "importieren möchten, dann wählen Sie bitte \"Schlüssel importieren\"."),true);

		ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Schlüssel importieren..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.startImport();
      }
    },null,false,"document-open.png");
		buttons.addButton(i18n.tr("Neuen Schlüssel (INI-Brief) erstellen"),new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				control.startCreate();
			}
		},null,false,"document-new.png");
		buttons.paint(getParent());
		
    control.getKeyList().paint(getParent());
  }
}
