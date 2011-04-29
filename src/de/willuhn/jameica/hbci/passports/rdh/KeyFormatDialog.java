/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/KeyFormatDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/04/29 11:38:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Auswahl des Schluesselformats.
 */
public class KeyFormatDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static int WINDOW_WIDTH = 440;

  private TablePart table   = null;
  private KeyFormat choosen = null;
  private LabelInput warn   = null;
  private int neededFeature = KeyFormat.FEATURE_CREATE;
  
  /**
   * ct.
   * @param position
   * @param feature das vom Format geforderte Feature.
   * @see KeyFormat#FEATURE_CREATE
   * @see KeyFormat#FEATURE_IMPORT
   */
  public KeyFormatDialog(int position, int feature)
  {
    super(position);
    this.neededFeature = feature;
    setTitle(i18n.tr("Auswahl des Datei-Formats"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    this.warn = new LabelInput("");
    this.warn.setName("");
    this.warn.setColor(Color.ERROR);
    
    final Action action = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Object o = table.getSelection();
        if (o == null || !(o instanceof KeyFormat))
        {
          // Der User soll nicht einfach auf "Uebernehmen" klicken koennen, ohne etwas auszuwaehlen
          warn.setValue(i18n.tr("Bitte wählen Sie ein Format aus"));
          return;
        }
        choosen = (KeyFormat) o;
        close();
      }
    
    };

    Container c = new SimpleContainer(parent);
    c.addText(i18n.tr("Bitte wählen Sie das Datei-Format des Schlüssels"),true);
    c.addInput(this.warn);

    this.table = new TablePart(Arrays.asList(RDHKeyFactory.getKeyFormats(this.neededFeature)),action);
    this.table.addColumn("Bezeichnung","name");
    this.table.setMulti(false);
    this.table.setRememberColWidths(true);
    this.table.setRememberOrder(true);
    this.table.setSummary(false);

    c.addPart(this.table);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"), action,null,false,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("cancelled in key format dialog");
      }
    },null,false,"process-stop.png");

    c.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.choosen;
  }
  
}


/*********************************************************************
 * $Log: KeyFormatDialog.java,v $
 * Revision 1.2  2011/04/29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.6  2010/06/14 22:55:00  willuhn
 * @C Dialog-Groesse angepasst
 *
 * Revision 1.5  2010/06/14 22:46:10  willuhn
 * @C Dialog-Groesse angepasst
 *
 * Revision 1.4  2008/11/06 21:44:37  willuhn
 * @B Benoetigtes Feature wurde in Auswahldialog nicht beruecksichtigt
 *
 * Revision 1.3  2008/07/28 08:35:44  willuhn
 * @N Finder-Methode fuer Schluesselformate in RDHKeyFactory verschoben
 *
 * Revision 1.2  2008/07/25 11:34:56  willuhn
 * @B Bugfixing
 *
 * Revision 1.1  2008/07/25 11:06:08  willuhn
 * @N RDH-2 Format
 * @C Haufenweise Code-Cleanup
 *
 * Revision 1.3  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 * Revision 1.2  2006/01/23 17:19:48  willuhn
 * @B bug 155
 *
 * Revision 1.1  2005/11/14 11:00:18  willuhn
 * @B bug 148
 *
 **********************************************************************/