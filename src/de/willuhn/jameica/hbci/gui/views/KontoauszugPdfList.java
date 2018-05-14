/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.KontoauszugPdfControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * View mit der Liste der Kontoauszuege im PDF-Format.
 */
public class KontoauszugPdfList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Elektronische Kontoauszüge"));
    
    final KontoauszugPdfControl control = new KontoauszugPdfControl(this);
    final de.willuhn.jameica.hbci.gui.parts.KontoauszugPdfList list = control.getList();
    
    list.paint(getParent());
  }
  
}
