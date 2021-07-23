/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

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
 * Zeigt die Liste der Kartenleser-Konfigurationen an.
 */
public class View extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Chipkarten-Konfigurationen"));

    final Controller control = new Controller(this);

    Container c = new SimpleContainer(getParent());
    c.addText(i18n.tr("Starten Sie zunächst die automatische Suche nach dem Kartenleser. " +
    		              "Falls sie nicht erfolgreich verläuft, dann konfigurieren Sie den " +
    		              "Kartenleser bitte manuell."),true);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Kartenleser suchen..."), new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleScan();
      }
    },null,false,"system-search.png");
    buttons.addButton(i18n.tr("Kartenleser manuell anlegen"),new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleCreate();
      }
    },null,false,"document-new.png");
    buttons.paint(getParent());
    
    control.getConfigList().paint(getParent());
    
    
  }
}



/**********************************************************************
 * $Log: View.java,v $
 * Revision 1.8  2011/04/29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.7  2011-04-08 15:19:15  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.6  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 **********************************************************************/