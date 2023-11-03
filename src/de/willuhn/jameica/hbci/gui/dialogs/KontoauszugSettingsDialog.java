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
  
  private CheckboxInput displayAll     = null;
  private CheckboxInput instantSearch  = null;
  private CheckboxInput markReadOnExit = null;

  /**
   * ct.
   */
  public KontoauszugSettingsDialog()
  {
    super(POSITION_CENTER);
    this.setTitle(i18n.tr("Anzeige-Einstellungen"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    final Container c = new SimpleContainer(parent);
    c.addInput(this.getDisplayAll());
    c.addInput(this.getInstantSearch());
    c.addInput(this.getMarkReadOnExit());
    
    final Button apply = new Button(i18n.tr("Übernehmen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        settings.setAttribute("usage.display.all",((Boolean) getDisplayAll().getValue()).booleanValue());
        settings.setAttribute("usage.instantsearch",((Boolean) getInstantSearch().getValue()).booleanValue());
        de.willuhn.jameica.hbci.Settings.setMarkReadOnExit(((Boolean) getMarkReadOnExit().getValue()).booleanValue());
        close();
      }
    },null,true,"ok.png");
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(apply);
    buttons.addButton(new Cancel());
    c.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
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
  
  /**
   * Liefert die Checkbox, mit der eingestellt werden kann, die Suche nach Eingabe eines Suchbegriffs sofort starten soll.
   * @return Checkbox.
   */
  private CheckboxInput getInstantSearch()
  {
    if (this.instantSearch != null)
      return this.instantSearch;
    
    this.instantSearch = new CheckboxInput(settings.getBoolean("usage.instantsearch",false));
    this.instantSearch.setName(i18n.tr("Suche bei Eingabe eines Suchbegriffs sofort starten"));
    return this.instantSearch;
  }

  /**
   * Liefert die Checkbox, mit der eingestellt werden kann, ob die Umsätze beim Beenden automatisch als gelesen markiert werden.
   * @return Checkbox.
   */
  private CheckboxInput getMarkReadOnExit()
  {
    if (this.markReadOnExit != null)
      return this.markReadOnExit;
    
    this.markReadOnExit = new CheckboxInput(de.willuhn.jameica.hbci.Settings.getMarkReadOnExit());
    this.markReadOnExit.setName(i18n.tr("Umsätze beim Beenden als gelesen markieren"));
    return this.markReadOnExit;
  }

}


