/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.EinnahmeAusgabeExport;
import de.willuhn.jameica.hbci.gui.controller.EinnahmeAusgabeControl;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
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
          List data = control.getTable().getItems();
          new EinnahmeAusgabeExport().handleAction(data.toArray(new EinnahmeAusgabe[data.size()]));
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
    
    control.getTable().paint(this.getParent());
  }
}
