/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.AccountProvider;
import de.willuhn.jameica.hbci.accounts.AccountService;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Erstellen eines neuen Bankzugangs.
 */
public class AccountNew implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    // Checken, ob wir mehrere Account-Provider haben. Wenn es nur einen gibt,
    // koennen wir direkt auf die zweite Seite springen.
    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    AccountService service = bs.get(AccountService.class);

    List<AccountProvider> list = service.getProviders();

    if (list.size() == 0)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Die Installation unterstützt keine Bank-Zugänge"),StatusBarMessage.TYPE_ERROR));
      return;
    }

    // Wir haben mehrere Account-Provider. Dann auf zu Seite 1.
    if (list.size() > 1)
    {
      GUI.startView(de.willuhn.jameica.hbci.gui.views.AccountNew.class,null);
      return;
    }

    // Wir haben nur einen Account-Provider. Dann direkt zu Seite 2.
    list.get(0).create();
  }

}
