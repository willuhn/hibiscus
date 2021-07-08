/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.AccountProvider;
import de.willuhn.jameica.hbci.accounts.AccountService;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller zum Anlegen eines neuen Accounts.
 */
public class AccountNewController extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private List<InfoPanel> providers = null;

  /**
   * ct.
   * @param view
   */
  public AccountNewController(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert eine Liste mit Info-Panels zu den verfuegbaren Account-Providern.
   * @return eine Liste mit Info-Panels zu den verfuegbaren Account-Providern.
   */
  public synchronized List<InfoPanel> getAccountProviders()
  {
    if (this.providers != null)
      return this.providers;

    this.providers = new ArrayList<InfoPanel>();
    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    AccountService service = bs.get(AccountService.class);

    List<AccountProvider> list = service.getProviders();
    for (final AccountProvider p:list)
    {
      final InfoPanel panel = p.getInfo();
      final Button button = new Button(i18n.tr("Bank-Zugang anlegen..."),new Action() {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          Logger.info("starting creation of new bank account. type: " + p.getName());
          p.create();
        }
      },null,false,"go-next.png");
      panel.addButton(button);
      this.providers.add(panel);
    }

    return this.providers;
  }
}
