/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/LoginDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/15 18:09:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Login;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog zur Eingabe von Login-Daten fuer Hibiscus.
 * @author willuhn
 */
public class LoginDialog extends AbstractDialog
{

  private I18N i18n;
  private Login login = null;

  /**
   * @param position
   */
  public LoginDialog(int position)
  {
    super(position);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("Hibiscus Login"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Benutzer-Daten"));
    
    group.addText(i18n.tr("Wenn Sie noch kein..."),true);
    Input username = new TextInput("", 20);
    Input password = new TextInput("", 20);

    group.addLabelPair(i18n.tr("Benutzername"),username);
    group.addLabelPair(i18n.tr("Passwort"),password);
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Neues Login erstellen"),null);
    buttons.addButton(i18n.tr("Login"),null,null,true);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return login;
  }

}


/*********************************************************************
 * $Log: LoginDialog.java,v $
 * Revision 1.1  2004/11/15 18:09:18  willuhn
 * @N Login fuer die gesamte Anwendung
 *
 **********************************************************************/