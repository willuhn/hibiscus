/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/Detail.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/09/01 09:40:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Detail-Ansicht einer Kartenleser-Config.
 */
public class Detail extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Details der Kartenleser-Konfiguration"));
    
    final Controller control = new Controller(this);

    ColumnLayout columns = new ColumnLayout(getParent(),2);
    
    {
      // Linke Seite
      Container container = new SimpleContainer(columns.getComposite());
      container.addHeadline(i18n.tr("Einstellungen des Kartenlesers"));
      container.addInput(control.getReaderPresets());
      container.addInput(control.getCTAPI());
      container.addInput(control.getPort());
      container.addInput(control.getCTNumber());
    }
    
    {
      // Rechte Seite
      Container container = new SimpleContainer(columns.getComposite());
      container.addHeadline(i18n.tr("Benutzerdaten"));
      container.addInput(control.getHBCIVersion());
      container.addInput(control.getEntryIndex());
    }

    {
      Container container = new SimpleContainer(getParent());
      container.addHeadline(i18n.tr("Erweiterte Einstellungen"));
      container.addInput(control.getBezeichnung());
      container.addInput(control.getSoftPin());
    }

    new Headline(getParent(),i18n.tr("Fest zugeordnete Konten"));
    control.getKontoAuswahl().paint(getParent());

    ButtonArea buttonArea = new ButtonArea();
    buttonArea.addButton(i18n.tr("Konfiguration testen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleTest();
      }
    },null,false,"dialog-information.png");
    buttonArea.addButton(i18n.tr("Bankdaten ändern"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleChangeBankData();
      }
    },null,false,"seahorse-preferences.png");
    buttonArea.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    },null,false,"document-save.png");
    
    buttonArea.paint(getParent());
  }
}


/**********************************************************************
 * $Log: Detail.java,v $
 * Revision 1.4  2011/09/01 09:40:53  willuhn
 * @R Biometrie-Support bei Kartenlesern entfernt - wurde nie benutzt
 *
 * Revision 1.3  2011-04-29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.2  2011-04-08 15:19:15  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.1  2010-09-07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.5  2010-07-13 10:55:29  willuhn
 * @N Erster Code zum Aendern der Bank-Daten direkt auf der Karte. Muss dringend noch getestet werden - das will ich aber nicht mit meiner Karte machen, weil ich mir schonmal meine Karte mit Tests zerschossen hatte und die aber taeglich brauche ;)
 *
 * Revision 1.4  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/