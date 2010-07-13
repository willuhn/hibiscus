/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/View.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/07/13 11:01:05 $
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
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Konfiguration eines Passports vom Typ PIN/TAN.
 */
public class View extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Konfigurationen"));

		final Controller control = new Controller(this);

    control.getConfigList().paint(getParent());
    
    ButtonArea buttons = new ButtonArea(getParent(), 2);
    buttons.addButton(new Back(true));
    buttons.addButton(i18n.tr("Neue Konfiguration anlegen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleCreate();
      }
    },null,false,"document-new.png");
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: View.java,v $
 * Revision 1.2  2010/07/13 11:01:05  willuhn
 * @N Icons in PIN/TAN-Config
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.8  2006/08/03 13:51:38  willuhn
 * @N Bug 62
 * @C HBCICallback-Handling nach Zustaendigkeit auf Passports verteilt
 *
 * Revision 1.7  2006/03/28 22:52:31  willuhn
 * @B bug 218
 *
 * Revision 1.6  2006/03/28 22:35:14  willuhn
 * @B bug 218
 *
 * Revision 1.5  2006/03/28 17:51:08  willuhn
 * @B bug 218
 *
 * Revision 1.4  2005/06/21 21:44:49  web0
 * @B bug 80
 *
 * Revision 1.3  2005/06/21 20:19:04  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/09 17:24:40  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/03/07 12:06:12  web0
 * @N initial import
 *
 **********************************************************************/