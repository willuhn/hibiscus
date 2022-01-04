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
 * Implementierung der HBCI-Variante fuer Chipkarte.
 */
@Lifecycle(Type.CONTEXT)
public class HBCIVariantChipcard implements HBCIVariant
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public String getName()
  {
    return i18n.tr("DDV-Chipkarte");
  }

  @Override
  public InfoPanel getInfo()
  {
    InfoPanel info = new InfoPanel();
    info.setTitle(this.getName());
    info.setText(i18n.tr("Verwenden Sie dieses Verfahren, wenn Sie eine spezielle HBCI-Chipkarte besitzen."));
    info.setComment(i18n.tr("Sie ben�tigen hierf�r zus�tzlich ein Chipkarten-Leseger�t mit USB-Anschluss.\nFalls Sie ChipTAN USB nutzen m�chten, w�hlen Sie stattdessen PIN/TAN."));
    info.setUrl("http://www.willuhn.de/wiki/doku.php?id=support:list:kartenleser");
    info.setIcon("gcr-smart-card.png");
    return info;
  }

  @Override
  public void create() throws ApplicationException
  {
    // TODO Auto-generated

  }

}


