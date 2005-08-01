/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/PassportAuswahlDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/08/01 23:27:42 $
 * $Author: web0 $
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
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.PassportList;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Auswahl eines Passports.
 */
public class PassportAuswahlDialog extends AbstractDialog
{

  private Passport selected = null;
  private I18N i18n         = null;

  /**
   * @param position
   */
  public PassportAuswahlDialog(int position)
  {
    super(position);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("Auswahl des Sicherheitsmediums"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Sicherheitsmedien"));
    group.addText(i18n.tr("Bitte wählen Sie das zu verwendende Sicherheitsmedium aus"),false);
    
    final PassportList list = new PassportList(new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof Passport))
          return;
        selected = (Passport) context;
        close();
      }
    });
    list.paint(parent);
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Object o = list.getSelection();
        if (o == null || !(o instanceof Passport))
          return;
        selected = (Passport) o;
        close();
      }
    });
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
    
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return selected;
  }

}


/*********************************************************************
 * $Log: PassportAuswahlDialog.java,v $
 * Revision 1.2  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/21 21:48:24  web0
 * @B bug 80
 *
 **********************************************************************/