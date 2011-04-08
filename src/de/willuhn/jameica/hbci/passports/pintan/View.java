/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/View.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/04/08 15:19:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Konfiguration eines Passports vom Typ PIN/TAN.
 */
public class View extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		GUI.getView().setTitle(i18n.tr("Vorhandene Konfigurationen"));
		final Controller control = new Controller(this);

    control.getConfigList().paint(getParent());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Neue Konfiguration anlegen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleCreate();
      }
    },null,false,"document-new.png");
    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: View.java,v $
 * Revision 1.4  2011/04/08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.3  2010-09-07 15:17:07  willuhn
 * @N GUI-Cleanup
 *
 * Revision 1.2  2010-07-13 11:01:05  willuhn
 * @N Icons in PIN/TAN-Config
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/