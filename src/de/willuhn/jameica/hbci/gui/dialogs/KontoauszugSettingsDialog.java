/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Konfigurieren der Anzeigeeinstellungen fuer die Umsaetze.
 */
public class KontoauszugSettingsDialog extends AbstractDialog
{
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH = 500;
  
  private CheckboxInput displayAll = null;

  /**
   * ct.
   */
  public KontoauszugSettingsDialog()
  {
    super(POSITION_CENTER);
    this.setTitle(i18n.tr("Anzeige-Einstellungen"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }
  
  @Override
  protected void paint(Composite parent) throws Exception
  {
    final Container c = new SimpleContainer(parent);
    c.addInput(this.getDisplayAll());
    
    final Button apply = new Button(i18n.tr("Übernehmen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        settings.setAttribute("usage.display.all",((Boolean) getDisplayAll().getValue()).booleanValue());
        close();
      }
    },null,true,"ok.png");
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(apply);
    buttons.addButton(new Cancel());
    c.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
  
  /**
   * Liefert die Checkbox, mit der eingestellt werden kann, ob alle Daten des Verwendungszwecks angezeigt werden sollen.
   * @return Checkbox.
   */
  private CheckboxInput getDisplayAll()
  {
    if (this.displayAll != null)
      return this.displayAll;
    
    this.displayAll = new CheckboxInput(settings.getBoolean("usage.display.all",false));
    this.displayAll.setName(i18n.tr("Alle Daten des Verwendungszwecks anzeigen"));
    return this.displayAll;
  }

}


