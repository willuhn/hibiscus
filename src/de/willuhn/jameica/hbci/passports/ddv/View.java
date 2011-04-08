/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/View.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/04/08 15:19:15 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt die Liste der Kartenleser-Konfigurationen an.
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
    buttons.addButton(i18n.tr("Kartenleser suchen..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleScan();
      }
    },null,false,"view-refresh.png");
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
 * Revision 1.7  2011/04/08 15:19:15  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.6  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 **********************************************************************/