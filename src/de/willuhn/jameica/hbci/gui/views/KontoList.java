/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/KontoList.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/04/29 11:38:57 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Bankverbindungen an.
 */
public class KontoList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		final KontoControl control = new KontoControl(this);
		GUI.getView().setTitle(i18n.tr("Vorhandene Bankverbindungen"));

    control.getKontoListe().paint(getParent());

		ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Konten aus HBCI-Konfiguration laden..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleReadFromPassport();
      }
    },null,false,"mail-send-receive.png");
		buttons.addButton(i18n.tr("Konto manuell anlegen"),new KontoNew(),null,false,"system-file-manager.png");
		buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.9  2011/04/29 11:38:57  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.8  2011-04-08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.7  2010-09-29 23:43:34  willuhn
 * @N Automatisches Abgleichen und Anlegen von Konten aus KontoFetchFromPassport in KontoMerge verschoben
 * @N Konten automatisch (mit Rueckfrage) anlegen, wenn das Testen der HBCI-Konfiguration erfolgreich war
 * @N Config-Test jetzt auch bei Schluesseldatei
 * @B in PassportHandleImpl#getKonten() wurder der Converter-Funktion seit jeher die falsche Passport-Klasse uebergeben. Da gehoerte nicht das Interface hin sondern die Impl
 **********************************************************************/