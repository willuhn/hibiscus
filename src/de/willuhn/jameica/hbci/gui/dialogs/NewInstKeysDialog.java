/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/NewInstKeysDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/02 16:15:52 $
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
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;

/**
 * Dialog, welcher dem Benutzer die neu uebertragenen Instituts-Schluessel
 * zur Verifizierung anzeigt.
 */
public class NewInstKeysDialog extends AbstractDialog
{

  /**
   * ct.
   * @param p Passport, fuer den die Schluessel angezeigt werden sollen.
   */
  public NewInstKeysDialog(HBCIPassport p)
  {
    super(NewInstKeysDialog.POSITION_CENTER);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}


/**********************************************************************
 * $Log: NewInstKeysDialog.java,v $
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/