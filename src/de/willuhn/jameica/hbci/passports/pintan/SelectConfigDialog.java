/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/SelectConfigDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/09/07 15:17:07 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
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
  private String text           = null;

  /**
   * @param position
   */
  public SelectConfigDialog(int position)
  {
    super(position);
    setTitle(i18n.tr("Auswahl der PIN/TAN-Konfiguration"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Konfiguration"));
    group.addText(text == null ? i18n.tr("Bitte wählen Sie die zu verwendende PIN/TAN-Konfiguration aus") : text,true);
    
    final TablePart table = new TablePart(PinTanConfigFactory.getConfigs(), new Action() {
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
    table.paint(parent);
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        selected = (PinTanConfig) table.getSelection();
        if (selected == null)
          return;
        close();
      }
    });
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
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


/*********************************************************************
 * $Log: SelectConfigDialog.java,v $
 * Revision 1.3  2010/09/07 15:17:07  willuhn
 * @N GUI-Cleanup
 *
 * Revision 1.2  2010-07-22 12:37:41  willuhn
 * @N GUI poliert
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.4  2005/07/26 22:56:48  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/07/18 12:53:30  web0
 * @B bug 96
 *
 * Revision 1.2  2005/06/27 15:30:17  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/23 21:52:49  web0
 * @B Bug 80
 *
 **********************************************************************/