/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/PassportPropertyDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/16 15:32:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.PassportPropertyList;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, der die UPD/BPD eines Passports anzeigt.
 */
public class PassportPropertyDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private HBCIPassport passport = null;

  /**
   * ct.
   * @param position
   * @param passport der Passport.
   */
  public PassportPropertyDialog(int position, HBCIPassport passport)
  {
    super(position);
    this.setTitle(i18n.tr("BPD/UPD"));
    this.setSize(560,400);
    this.passport = passport;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.passport;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // Dialog bei Druck auf ESC automatisch schliessen
    parent.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.ESC)
          throw new OperationCanceledException();
      }
    });

    Container container = new SimpleContainer(parent);
    container.addText(i18n.tr("Die folgende Liste enth‰lt die Bank-Parameter (BPD) und User-Parameter (UPD) dieses Sicherheitsmediums."),true);
    new PassportPropertyList(this.passport).paint(parent);
    
    ButtonArea buttons = new ButtonArea(parent,1);
    buttons.addButton("   " + i18n.tr("Schlieﬂen") + "   ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true);
  }

}


/**********************************************************************
 * $Log: PassportPropertyDialog.java,v $
 * Revision 1.1  2009/06/16 15:32:30  willuhn
 * *** empty log message ***
 *
 **********************************************************************/
