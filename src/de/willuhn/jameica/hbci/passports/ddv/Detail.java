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
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
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
      container.addInput(control.getPCSCName());
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

    Container c = new SimpleContainer(getParent(),true);
    c.addHeadline(i18n.tr("Fest zugeordnete Konten"));
    c.addText(i18n.tr("Die folgende Liste enthält alle Konten, welche diesem Bankzugang fest zugeordnet werden können. " +
                      "Aktivieren Sie die Kontrollkästchen der gewünschten Konten in der Spalte \"Kontonummer\", um diese Konten fest zuzuordnen. Klicken Sie anschließend \"Speichern\". " +
                      "Weitere Informationen hierzu finden Sie links in der Hilfe.\n"),true);
    c.addPart(control.getKontoAuswahl());


    ButtonArea buttonArea = new ButtonArea();
    buttonArea.addButton(i18n.tr("BPD/UPD"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleDisplayProperties();
      }
    },null,false,"document-properties.png");
    buttonArea.addButton(i18n.tr("Synchronisieren"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleSync();
      }
    },null,false,"view-refresh.png");
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
    },null,false,"system-users.png");
    buttonArea.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    },null,false,"document-save.png");
    
    buttonArea.paint(getParent());
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#canBookmark()
   */
  public boolean canBookmark()
  {
    return false;
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#canAttach()
   */
  public boolean canAttach()
  {
    return false;
  }

}
