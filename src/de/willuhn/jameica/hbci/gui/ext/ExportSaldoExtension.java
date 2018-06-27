/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.ext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.io.Exporter;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Erweitert den Export-Dialog um eine zusaetzliche Option mit der ausgewaehlt
 * werden kann, ob der Saldo mit exportiert werden soll.
 */
public class ExportSaldoExtension implements Extension
{
  /**
   * Der Context-Schluessel fuer die Option zum Ausblenden des Saldo im Export.
   */
  public final static String KEY_SALDO_HIDE = "saldo.hide";
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  public void extend(Extendable extendable)
  {
    if (!(extendable instanceof ExportDialog))
      return;
    
    ExportDialog e = (ExportDialog) extendable;
    
    Class type = e.getType();
    if (!type.isAssignableFrom(Umsatz.class))
      return;
    
    // Erstmal per Default nicht ausblenden
    boolean initial = ExportDialog.SETTINGS.getBoolean(KEY_SALDO_HIDE,false);
    Exporter.SESSION.put(KEY_SALDO_HIDE,initial);
    
    final CheckboxInput check = new CheckboxInput(initial);
    check.setName(i18n.tr("Spalte \"Saldo\" in Export ausblenden"));
    check.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        Boolean value = (Boolean) check.getValue();
        Exporter.SESSION.put(KEY_SALDO_HIDE,value);
        ExportDialog.SETTINGS.setAttribute(KEY_SALDO_HIDE,value.booleanValue());
      }
    });
    
    final Container c = e.getContainer();
    c.addInput(check);
    
    // Jetzt noch ein Listener an die Auswahl-Box mit dem Format.
    // Wenn das aktuell ausgewaehlte Format diese Extension nicht unterstuetzt,
    // dann deaktivieren wir uns selbst
    try
    {
      final Input format = e.getExporterList();
      Listener l = new Listener() {
        
        @Override
        public void handleEvent(Event event)
        {
          ExportDialog.ExpotFormat exp = (ExportDialog.ExpotFormat) format.getValue();
          if (exp == null)
            return;
          
          Exporter exporter = exp.getExporter();
          check.setEnabled(exporter.suppportsExtension(KEY_SALDO_HIDE));
        }
      };
      
      format.getControl().addListener(SWT.Selection,l);
      
      // Einmal initial ausloesen
      l.handleEvent(null);
    }
    catch (Exception ex)
    {
      Logger.error("unable to determine export format",ex);
    }
  }
}


