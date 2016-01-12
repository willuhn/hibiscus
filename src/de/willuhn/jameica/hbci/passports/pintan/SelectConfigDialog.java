/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


// BUGZILLA #80 http://www.willuhn.de/bugzilla/show_bug.cgi?id=80
/**
 * Ein Dialog zur Auswahl der zu verwendenden PIN/TAN-Config.
 */
public class SelectConfigDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private PinTanConfig selected = null;
  private GenericIterator list  = null;
  private String text           = null;

  /**
   * ct.
   * @param position Position des Dialogs.
   * @param list optionale Liste der anzuzeigenden Konfigurationen.
   * Wenn keine angegeben ist, werden alle verfuegbaren angezeigt.
   */
  public SelectConfigDialog(int position, GenericIterator list)
  {
    super(position);
    this.list = list;
    this.setSize(470,300);
    setTitle(i18n.tr("Auswahl der PIN/TAN-Konfiguration"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent,true);
    group.addText(text == null ? i18n.tr("Bitte wählen Sie die zu verwendende PIN/TAN-Konfiguration aus") : text,true);
    
    if (list != null)
      list.begin();
    
    final TablePart table = new TablePart(list != null && list.size() > 0 ? list : PinTanConfigFactory.getConfigs(), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof PinTanConfig))
          return;
        selected = (PinTanConfig) context;
        close();
      }
    });
    table.addColumn(i18n.tr("Bank"),"bank");
    table.addColumn(i18n.tr("Alias-Name"),"bezeichnung");
    table.addColumn(i18n.tr("URL"),"url");
    table.addColumn(i18n.tr("Kundenkennung"),"customerid");
    table.setMulti(false);
    table.setSummary(false);
    group.addPart(table);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        selected = (PinTanConfig) table.getSelection();
        if (selected == null)
          return;
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    group.addButtonArea(buttons);
  }

  /**
   * Legt den Text des Dialogs fest.
   * @param text
   */
  public void setText(String text)
  {
    this.text = text;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return selected;
  }

}
