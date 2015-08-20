/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.scripting;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.AccountProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Account-Providers fuer Scripting-Konten.
 */
@Lifecycle(Type.CONTEXT)
public class ScriptingAccountProvider implements AccountProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.accounts.AccountProvider#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("Bank-Zugang per Scripting-Plugin");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.accounts.AccountProvider#getInfo()
   */
  @Override
  public InfoPanel getInfo()
  {
    InfoPanel info = new InfoPanel();
    info.setTitle(this.getName());
    info.setText(i18n.tr("Verwenden Sie diese Option für die Anlage von Bank-Zugängen per Scripting-Plugin."));
    info.setComment(i18n.tr("Per Scripting-Plugin erhalten Sie Zugriff auf Kreditkarten-, PayPal- und andere Konten."));
    info.setUrl("http://www.willuhn.de/wiki/doku.php?id=support:list:banken:scripting");
    info.setIcon("application-javascript-large.png");
    return info;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.accounts.AccountProvider#create()
   */
  @Override
  public void create() throws ApplicationException
  {
  }
}


