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
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung der HBCI-Variante fuer Schluesseldatei.
 */
@Lifecycle(Type.CONTEXT)
public class HBCIVariantKey implements HBCIVariant
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public String getName()
  {
    return i18n.tr("Schl�sseldatei");
  }

  @Override
  public InfoPanel getInfo()
  {
    InfoPanel info = new InfoPanel();
    info.setTitle(this.getName());
    info.setText(i18n.tr("Verwenden Sie dieses Verfahren, wenn Sie eine Schl�sseldatei besitzen oder einen neuen INI-Brief erstellen m�chten."));
    info.setComment(i18n.tr("Dieses Verfahren wird nur von sehr wenigen Banken angeboten."));
    info.setUrl("http://www.willuhn.de/wiki/doku.php?id=support:list:banken:misc:rdh");
    info.setIcon("dialog-password.png");
    return info;
  }

  @Override
  public void create() throws ApplicationException
  {
    // TODO Auto-generated

  }

}


