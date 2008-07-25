/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/HBCIVersionDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/07/25 13:31:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
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

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return version;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("HBCI-Version"));
    group.addText(i18n.tr("Bitte wählen Sie die zu verwendende HBCI-Version"),true);
    
    final HBCIVersionInput input = new HBCIVersionInput();
    group.addInput(input);
    
    ButtonArea buttons = group.createButtonArea(2);
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        version = (String) input.getValue();
        close();
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("cancelled while choosing hbci version");
      }
    
    });
  }

}


/*********************************************************************
 * $Log: HBCIVersionDialog.java,v $
 * Revision 1.2  2008/07/25 13:31:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/07/25 11:06:44  willuhn
 * @N Auswahl-Dialog fuer HBCI-Version
 * @N Code-Cleanup
 *
 **********************************************************************/