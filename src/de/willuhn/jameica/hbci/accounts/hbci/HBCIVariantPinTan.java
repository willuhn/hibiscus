/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.hbci.controller.HBCIVariantPinTanController;
import de.willuhn.jameica.hbci.accounts.hbci.views.HBCIVariantPinTanStep1;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung der HBCI-Variante fuer PIN/TAN.
 */
@Lifecycle(Type.CONTEXT)
public class HBCIVariantPinTan implements HBCIVariant
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public String getName()
  {
    return i18n.tr("PIN/TAN");
  }

  @Override
  public InfoPanel getInfo()
  {
    InfoPanel info = new InfoPanel();
    info.setTitle(this.getName());
    info.setText(i18n.tr("Verwenden Sie dieses Verfahren, wenn Sie PIN/TAN (z.Bsp. smsTAN oder chipTAN mit TAN-Generator) nutzen m�chten."));
    info.setComment(i18n.tr("Wenn Sie nicht sicher sind, welches Verfahren Ihre Bank verwendet, w�hlen Sie im Zweifel PIN/TAN.\nDas PIN/TAN-Verfahren wird von den meisten Banken unterst�tzt."));
    info.setUrl("http://www.willuhn.de/wiki/doku.php?id=support:list:tan-verfahren");
    info.setIcon("hbci-pintan.png");
    return info;
  }

  @Override
  public void create() throws ApplicationException
  {
    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    GUI.startView(HBCIVariantPinTanStep1.class,bs.get(HBCIVariantPinTanController.class));
  }

}


