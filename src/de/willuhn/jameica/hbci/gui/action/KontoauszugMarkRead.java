/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, um einen Kontoauszug als gelesen zu markieren.
 */
public class KontoauszugMarkRead implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Kontoauszug[] list = null;
    if (context instanceof Kontoauszug)
      list = new Kontoauszug[]{(Kontoauszug)context};
    else if (context instanceof Kontoauszug[])
      list = (Kontoauszug[]) context;
    
    if (list == null || list.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Kontoauszüge aus"));
    
    KontoauszugPdfUtil.markRead(true, list);
  }

}


