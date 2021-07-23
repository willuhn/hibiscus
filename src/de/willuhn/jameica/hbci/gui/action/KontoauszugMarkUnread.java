/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
 * Action, um einen Kontoauszug als ungelesen zu markieren.
 */
public class KontoauszugMarkUnread implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
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
    
    KontoauszugPdfUtil.markRead(false, list);
  }

}


