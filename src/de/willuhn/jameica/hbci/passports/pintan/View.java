/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Konfiguration eines Passports vom Typ PIN/TAN.
 */
public class View extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public void bind() throws Exception
  {
		GUI.getView().setTitle(i18n.tr("PIN/TAN-Konfigurationen"));
		final Controller control = new Controller(this);

    Container c = new SimpleContainer(getParent());
    c.addText(i18n.tr("Klicken Sie auf \"PIN/TAN-Zugang anlegen\", um einen neuen Bank-Zugang über das PIN/TAN-Verfahren einzurichten."),true);

		ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("PIN/TAN-Zugang anlegen"),new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleCreate();
      }
    },null,false,"document-new.png");
    buttons.paint(getParent());

    control.getConfigList().paint(getParent());
    
    // Wenn wir mit einem Passport als Context statt der konkreten Config 
    // aufgerufen wurden, dann hatte der User explizit auf "Neuer Bank-Zugang..."
    // geklickt. In dem Fall starten wir sofort den Dialog zur Erstellung eines
    // neuen Bankzugangs.
    // Das machen wir aber als neues Runnable, damit die View erstmal
    // komplett gezeichnet werden kann
    Object ctx = this.getCurrentObject();
    if (ctx instanceof Passport)
    {
      GUI.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          setCurrentObject(null); // Context loeschen, damit wir das nicht nochmal aufrufen, wenn wir mit Back-Action zurueckkommen
          Logger.info("starting wizzard for creation of new passport");
          control.handleCreate();
        }
      });
    }
  }
}
