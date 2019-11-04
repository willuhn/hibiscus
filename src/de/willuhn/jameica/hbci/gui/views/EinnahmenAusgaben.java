/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.EinnahmeAusgabeExport;
import de.willuhn.jameica.hbci.gui.controller.EinnahmeAusgabeControl;
import de.willuhn.jameica.hbci.gui.parts.EinnahmenAusgabenVerlauf;
import de.willuhn.jameica.hbci.rmi.EinnahmeAusgabeZeitraum;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View zur Anzeige der Auswertung "Einnahmen/Ausgaben".
 */
public class EinnahmenAusgaben extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Einnahmen/Ausgaben"));

    final EinnahmeAusgabeControl control = new EinnahmeAusgabeControl(this);

    {
      final TabFolder folder = new TabFolder(this.getParent(), SWT.NONE);
      folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));

      ColumnLayout cols = new ColumnLayout(tab.getComposite(),2);
      
      Container left = new SimpleContainer(cols.getComposite());
      left.addInput(control.getKontoAuswahl());
      left.addInput(control.getInterval());
      left.addInput(control.getActiveOnly());
      
      Container right = new SimpleContainer(cols.getComposite());
        
      right.addInput(control.getRange());
      MultiInput range = new MultiInput(control.getStart(),control.getEnd());
      right.addInput(range);
      
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Exportieren..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          List data = control.getTree().getItems();
          new EinnahmeAusgabeExport().handleAction(data.toArray(new EinnahmeAusgabeZeitraum[data.size()]));
        }
        catch (RemoteException re)
        {
          Logger.error("unable to export data",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Exportieren: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
      }
    },null,false,"document-save.png");
    buttons.addButton(i18n.tr("Aktualisieren"), new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleReload();
      }
    
    },null,true,"view-refresh.png");
    buttons.paint(getParent());
    
    final TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    
    final TabGroup tg1 = new TabGroup(folder,i18n.tr("Tabellarische Auswertung"),true,1);
    TreePart tree = control.getTree();
    tree.paint(tg1.getComposite());
    
    final TabGroup tg2 = new TabGroup(folder,i18n.tr("Grafische Auswertung"),true,1);
    final EinnahmenAusgabenVerlauf chart = control.getChart();
    chart.paint(tg2.getComposite());
    
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));
  }
}
