/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.AccountProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung des Account-Providers fuer HBCI-Konten.
 */
public class HBCIAccountProvider implements AccountProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.accounts.AccountProvider#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("FinTS/HBCI-Bankzugang");
  }

  /**
   * @see de.willuhn.jameica.hbci.accounts.AccountProvider#getDescription()
   */
  @Override
  public String getDescription()
  {
    return i18n.tr("Verwenden Sie diese Option für die Anlage von Bankzugängen mit PIN/TAN-Verfahren, Schlüsseldatei oder Chipkarte.\n" +
                   "HBCI/FinTS ist das in Hibiscus hauptsächlich verwendete Verfahren.");
  }

  /**
   * @see de.willuhn.jameica.hbci.accounts.AccountProvider#getIcon()
   */
  @Override
  public String getIcon()
  {
    return "hibiscus-icon-64x64.png";
  }

}


