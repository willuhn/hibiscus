/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/View.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/04/29 11:38:58 $
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
		GUI.getView().setTitle(i18n.tr("Schlüsseldisketten"));

		final Controller control = new Controller(this);

    Container c = new SimpleContainer(getParent());
    c.addText(i18n.tr("Zum Erstellen eines neuen INI-Briefes klicken Sie auf \"Neuen Schlüssel erstellen\". " +
    		              "Wenn Sie eine existierende Schlüssel-Datei (z.Bsp. aus einem anderen Programm) " +
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


/**********************************************************************
 * $Log: View.java,v $
 * Revision 1.3  2011/04/29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.2  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
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