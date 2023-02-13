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
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Detail-Ansicht eines Passports.
 */
public class Detail extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		final Controller control = new Controller(this);

		GUI.getView().setTitle(i18n.tr("Details der PIN/TAN-Konfiguration"));

    ColumnLayout layout = new ColumnLayout(getParent(),2);

    {
      Container group = new SimpleContainer(layout.getComposite());
      group.addHeadline(i18n.tr("Verbindungsdaten zur Bank"));
      group.addInput(control.getURL());
      group.addInput(control.getPort());
      group.addInput(control.getFilterType());
      group.addInput(control.getHBCIVersion());
    }
    
    {
      Container group = new SimpleContainer(layout.getComposite());
      group.addHeadline(i18n.tr("Benutzerdaten"));
      group.addInput(control.getUserId());
      group.addInput(control.getCustomerId());
      group.addInput(control.getBLZ());
    }
    
    {
      Container group = new SimpleContainer(getParent());

      group.addHeadline(i18n.tr("Erweiterte Einstellungen"));
      group.addInput(control.getBezeichnung());
      group.addInput(control.getTANMech());
      group.addCheckbox(control.getShowTan(),i18n.tr("TANs während der Eingabe anzeigen"));

      final PtSecMech secMech = control.getConfig().getCurrentSecMech();
      if (secMech != null && secMech.isFlickerCode())
      {
        group.addHeadline(i18n.tr("ChipTAN"));
        group.addText(i18n.tr("Hinweis: Die beiden Optionen zur Umwandlung des Flickercodes in einen QR-Code sowie die Verwendung eines Kartenlesers per USB schließen sich gegenseitig aus.\nSie können daher nur eine von beiden Optione wählen."),true, Color.COMMENT);
        group.addInput(control.getConvertQr());
        
        CheckboxInput check = control.getChipTANUSB();
        group.addInput(check);
        group.addInput(control.getCardReaders());
        
        // Wenn der User die Entscheidung noch nicht getroffen hat, dann ausgrauen
        if (control.getConfig().isChipTANUSB() == null)
        {
          org.eclipse.swt.widgets.Button b = (org.eclipse.swt.widgets.Button) check.getControl();
          b.setGrayed(true);
        }
      }
    }
    
    Container c = new SimpleContainer(getParent(),true);
    c.addHeadline(i18n.tr("Fest zugeordnete Konten"));
    c.addText(i18n.tr("Die folgende Liste enthält alle Konten, welche diesem Bankzugang fest zugeordnet werden können. " +
                      "Aktivieren Sie die Kontrollkästchen der gewünschten Konten in der Spalte \"Kontonummer\", um diese Konten fest zuzuordnen. Klicken Sie anschließend \"Speichern\". " +
                      "Weitere Informationen hierzu finden Sie links in der Hilfe.\n"),true);
    c.addPart(control.getKontoAuswahl());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("BPD/UPD"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleDisplayProperties();
      }
    },null,false,"document-properties.png");
    buttons.addButton(i18n.tr("Synchronisieren"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleSync();
      }
    },null,false,"view-refresh.png");
    buttons.addButton(i18n.tr("Konfiguration testen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				control.handleTest();
      }
    },null,false,"dialog-information.png");
    buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,false,"document-save.png");
    buttons.paint(getParent());
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
