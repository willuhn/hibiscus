/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/InternetConnectionDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/02/26 11:40:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, der den User bittet, eine Internet-Verbindung herzustellen.
 */
public class InternetConnectionDialog extends AbstractDialog
{
  private I18N i18n = null;

  /**
   * ct.
   * @param position
   */
  public InternetConnectionDialog(int position)
  {
    super(position);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.setTitle(i18n.tr("Internet-Verbindung"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    final CheckboxInput check = new CheckboxInput(Settings.getOnlineMode());
    LabelGroup g = new LabelGroup(parent,"");
    g.addText(i18n.tr("Bitte stellen Sie eine Internetverbindung her und klicken Sie auf anschlieﬂend OK."),true);
    g.addCheckbox(check,Application.getI18n().tr("Diesen Hinweis nicht mehr anzeigen"));
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton("   " + i18n.tr("OK") + "   ", new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (((Boolean)check.getValue()).booleanValue())
          Settings.setOnlineMode(((Boolean)check.getValue()).booleanValue());
        close();
      }
    },null,true);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}


/*********************************************************************
 * $Log: InternetConnectionDialog.java,v $
 * Revision 1.2  2007/02/26 11:40:06  willuhn
 * @C Ergonomie-Vorschlag von Gottfried
 *
 * Revision 1.1  2006/04/03 12:30:17  willuhn
 * @N new InternetConnectionDialog
 *
 **********************************************************************/