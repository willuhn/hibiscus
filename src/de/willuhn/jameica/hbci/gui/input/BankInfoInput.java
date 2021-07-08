/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.List;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Such-Eingabefeld zum Suchen einer Bank anhand Name, BLZ, BIC oder Ort.
 */
public class BankInfoInput extends SearchInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @param query der initiale Suchbegriff.
   */
  public BankInfoInput(String query)
  {
    this.setName(i18n.tr("Bank"));
    this.setSearchString(i18n.tr("BLZ, BIC, Name oder Ort der Bank..."));
    this.setStartAt(3);
    this.setDelay(700);

    if (query != null && query.length() > 0)
    {
      List result = startSearch(query);
      if (result != null && result.size() > 0)
      {
        this.setValue(result.get(0));
      }
    }
  }

  /**
   * @see de.willuhn.jameica.gui.input.SearchInput#startSearch(java.lang.String)
   */
  @Override
  public List startSearch(String text)
  {
    return HBCIUtils.searchBankInfo(text);
  }

}
