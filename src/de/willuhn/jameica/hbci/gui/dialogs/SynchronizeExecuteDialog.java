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

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber eine Liste von Auftraegen anzeigt, deren Ausfuehrung der
 * User bestaetigen muss.
 */
public class SynchronizeExecuteDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private List<SynchronizeJob> jobs = null;
  
  /**
   * ct.
   * @param jobs die Liste der Jobs.
   * @param position
   */
  public SynchronizeExecuteDialog(List<SynchronizeJob> jobs, int position)
  {
    super(position);
    this.setTitle(i18n.tr("Auszuführende Aufträge"));
    this.setSize(470,300);
    this.jobs = jobs;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent,true);
    container.addText(i18n.tr("Folgende Überweisungen und Lastschriften werden jetzt " +
    		                      "an die Bank übertragen. Bitte prüfen Sie diese nochmals " +
    		                      "um sicherzustellen, dass Sie keinen Auftrag versehentlich absenden."),true);
    
    TablePart table = new TablePart(this.jobs,null);
    table.addColumn(i18n.tr("Aufträge"),"name");
    table.setSummary(false);
    table.setRememberColWidths(true);
    container.addPart(table);
    
    // table.paint(parent);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Jetzt ausführen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"mail-send-receive.png");
    buttons.addButton(i18n.tr("Synchronisierung abbrechen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,true,"process-stop.png");
    
    container.addButtonArea(buttons);
    // buttons.paint(parent);
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
}

