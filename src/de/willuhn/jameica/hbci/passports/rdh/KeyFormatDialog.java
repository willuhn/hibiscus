/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
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
  private final static int WINDOW_WIDTH  = 440;
  private final static int WINDOW_HEIGHT = 260;

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
    setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    this.warn = new LabelInput("");
    this.warn.setName("");
    this.warn.setColor(Color.ERROR);
    
    final Action action = new Action() {
      @Override
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

    Container c = new SimpleContainer(parent,true);
    c.addText(i18n.tr("Bitte wählen Sie das Datei-Format des Schlüssels"),true);
    c.addInput(this.warn);

    this.table = new TablePart(Arrays.asList(RDHKeyFactory.getKeyFormats(this.neededFeature)),action);
    this.table.addColumn("Bezeichnung","name");
    this.table.setMulti(false);
    this.table.setRememberColWidths(true);
    this.table.setRememberOrder(true);
    this.table.removeFeature(FeatureSummary.class);

    c.addPart(this.table);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"), action,null,false,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("cancelled in key format dialog");
      }
    },null,false,"process-stop.png");

    c.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,WINDOW_HEIGHT));
  }

  @Override
  protected Object getData() throws Exception
  {
    return this.choosen;
  }
  
}
