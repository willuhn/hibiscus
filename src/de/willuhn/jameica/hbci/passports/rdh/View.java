/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/View.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:26:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
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
		GUI.getView().setTitle(i18n.tr("Schlüssel-Liste"));

		final Controller control = new Controller(this);

		control.getKeyList().paint(getParent());
    
    ButtonArea buttons = new ButtonArea(getParent(),3);
    buttons.addButton(new Back(true));
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
  }
}


/**********************************************************************
 * $Log: View.java,v $
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.11  2009/03/04 22:49:16  willuhn
 * @C INI-Brief anzeigen/drucken nur noch in Detail-Ansicht
 * @B falsche Button-Anzahl
 **********************************************************************/