/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/NewKeysDialog.java,v $
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
 * Dialog, der den neu erzeugten Schluessel anzeigt und den Benutzer
 * auffordert, den Ini-Brief an seine Bank zu senden.
 */
public class NewKeysDialog extends AbstractDialog
{

  /**
   */
  public NewKeysDialog(HBCIPassport p)
  {
    super(NewKeysDialog.POSITION_CENTER);
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
 * $Log: NewKeysDialog.java,v $
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/