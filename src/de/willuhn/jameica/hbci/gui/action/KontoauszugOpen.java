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

import java.io.File;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Oeffnen eines Kontoauszuges.
 */
public class KontoauszugOpen implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Kontoauszug))
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie den zu �ffnenden Kontoauszug"));
    
    Kontoauszug k = (Kontoauszug) context;
    
    File file = KontoauszugPdfUtil.getFile(k);
    if (!file.exists() || !file.canRead())
      throw new ApplicationException(i18n.tr("Datei existiert nicht oder ist nicht lesbar"));
    
    new Program().handleAction(file);
    
    // Als gelesen markieren, sobald er geoeffnet wurde
    KontoauszugPdfUtil.markRead(true,k);
  }

}


