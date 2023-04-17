/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import de.willuhn.datasource.rmi.Changeable;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Checkbox für die Option "In Adressbuch übernehmen".
 */
public class StoreAddressInput extends CheckboxInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

  /**
   * ct.
   * @param t der Auftrag.
   */
  public StoreAddressInput(Object t)
  {
    super(false);
    this.setName(i18n.tr("In Adressbuch übernehmen"));
    
    try
    {
      boolean enabled = settings.getBoolean("transfer.addressbook.autoadd",true);
      
      // Wenn die Option bereits ausgeschaltet wurde, brauchen wir uns die 
      // folgende Bedingung gar nicht mehr anschauen
      if (enabled && t != null)
      {
        // Nur bei neuen Transfers aktivieren
        // Checkbox nur setzen, wenn es eine neue Ueberweisung ist und noch kein Gegenkonto definiert ist.
        if ((t instanceof Transfer) && (t instanceof Changeable))
        {
          enabled &= (((Changeable)t).isNewObject()) && (((Transfer)t).getGegenkontoNummer() == null);
        }
      }
      this.setValue(enabled);
    }
    catch (Exception e)
    {
      Logger.error("unable to determine checkbox state",e);
    }
    
    // Wir merken uns den letzten Status
    this.addListener(e -> {
      try
      {
        final Boolean b = (Boolean) getValue();
        settings.setAttribute("transfer.addressbook.autoadd",b != null ? b.booleanValue() : true);
      }
      catch (Exception ex)
      {
        Logger.error("unable to update checkbox state",ex);
      }
    });
  }

}


