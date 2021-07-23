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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.input.HBCIVersionInput;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog fuer die Auswahl einer HBCI-Version.
 */
public class HBCIVersionDialog extends AbstractDialog
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private String version = null;

  /**
   * ct
   * @param position
   */
  public HBCIVersionDialog(int position)
  {
    super(position);
    setTitle(i18n.tr("HBCI-Version"));
  }

  @Override
  protected Object getData() throws Exception
  {
    return version;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    group.addText(i18n.tr("Bitte wählen Sie die zu verwendende HBCI-Version"),true);
    
    final HBCIVersionInput input = new HBCIVersionInput();
    group.addInput(input);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        version = (String) input.getValue();
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("cancelled while choosing hbci version");
      }
    
    },null,false,"process-stop.png");
    group.addButtonArea(buttons);
  }
}


/*********************************************************************
 * $Log: HBCIVersionDialog.java,v $
 * Revision 1.4  2011/10/20 16:18:44  willuhn
 * @N GUI-Cleanup
 *
 * Revision 1.3  2008/07/28 09:31:10  willuhn
 * @N Abfrage der HBCI-Version via Messaging
 *
 * Revision 1.2  2008/07/25 13:31:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/07/25 11:06:44  willuhn
 * @N Auswahl-Dialog fuer HBCI-Version
 * @N Code-Cleanup
 *
 **********************************************************************/