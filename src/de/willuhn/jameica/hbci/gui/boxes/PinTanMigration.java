/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.CopyClipboard;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.passports.pintan.PinTanMigrationService;
import de.willuhn.jameica.hbci.passports.pintan.PinTanMigrationService.VerificationEntry;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt Bankzugänge an, bei denen die URL migriert werden sollte.
 */
public class PinTanMigration extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Resource private PinTanMigrationService pinTanMigrationService;
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Aktualisierung von PIN/TAN-Bankzugängen");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final List<VerificationEntry> list = this.pinTanMigrationService.getConfigs();
    
    if (list.isEmpty())
      return;

    final Container c = new SimpleContainer(parent);
    c.addText(i18n.tr("Bei den folgenden Bankzugängen ist die Aktualisierung der URL erforderlich."),true,Color.ERROR);
    c.addText(i18n.tr("Wählen Sie die Bankzugänge aus, deren URL aktualisiert werden soll und klicken Sie anschließend auf die Schaltfläche \"Ausgewählte Bankzugänge aktualisieren\". Sie können die URL alternativ auch " +
                      "selbst in der Detailansicht des Bankzugangs ändern."),true);
    
    final TablePart table = new TablePart(list,new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null)
          return;
        
        VerificationEntry l = (VerificationEntry) context;
        new PassportDetail().handleAction(l.getConfig());
      }
    });
    table.setCheckable(true);
    table.setRememberColWidths(true);
    table.setRememberOrder(true);
    table.removeFeature(FeatureSummary.class);
    table.addColumn(i18n.tr("Bezeichnung"),"config.description");
    table.addColumn(i18n.tr("Bisherige URL"),"oldUrl");
    table.addColumn(i18n.tr("Neue URL"),"newUrl");
    
    final ContextMenu ctx = new ContextMenu();
    ctx.addItem(new CheckedSingleContextMenuItem(i18n.tr("Öffnen"),a -> new PassportDetail().handleAction(((VerificationEntry)a).getConfig()),"document-open.png"));
    ctx.addItem(new CheckedSingleContextMenuItem(i18n.tr("Bisherige URL kopieren"),a -> new CopyClipboard().handleAction(((VerificationEntry)a).getOldUrl()),"edit-copy.png"));
    ctx.addItem(new CheckedSingleContextMenuItem(i18n.tr("Neue URL kopieren"),a -> new CopyClipboard().handleAction(((VerificationEntry)a).getNewUrl()),"edit-copy.png"));
    table.setContextMenu(ctx);


    final Button apply = new Button(i18n.tr("Ausgewählte Bankzugänge aktualisieren"), new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          if (!Application.getCallback().askUser(i18n.tr("Sicher? Sie können die URL im Bankzugang jederzeit wieder ändern.")))
            return;
          
          final List<VerificationEntry> list = table.getItems(true);
          if (list == null || list.isEmpty())
            return;
          
          final int count = pinTanMigrationService.migrate(list);
          
          table.removeAll();
          for (VerificationEntry v:pinTanMigrationService.getConfigs())
          {
            table.addItem(v);
          }
          
          final int type = count > 0 ? StatusBarMessage.TYPE_SUCCESS : StatusBarMessage.TYPE_INFO;
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Bankzugänge erfolgreich aktualisiert: {0}",Integer.toString(count)),type));
        }
        catch (Exception ex)
        {
          Logger.error("unable to migrate passports",ex);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Aktualisieren fehlgeschlagen: {0}",ex.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
      }
    },null,false,"emblem-symbolic-link.png");

    table.addSelectionListener(e -> {
      try
      {
        final List<VerificationEntry> selected = table.getItems(true);
        apply.setEnabled(selected != null && selected.size() > 0);
      }
      catch (Exception ex)
      {
        Logger.error("unable to update button",ex);
      }
    });
    
    table.paint(parent);
    
    final ButtonArea buttons = new ButtonArea();
    buttons.addButton(apply);
    buttons.addButton(i18n.tr("Aktualisieren"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        pinTanMigrationService.refresh();
        GUI.getCurrentView().reload();
      }
    },null,false,"view-refresh.png");
    buttons.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && isEnabled(); // Nicht konfigurierbar
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    return super.isEnabled() && !this.pinTanMigrationService.getConfigs().isEmpty();
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  @Override
  public int getHeight()
  {
    return 180;
  }
}
