/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.parts.KontoList;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Box zur Anzeige der Konten.
 */
public class Konten extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Konten-Übersicht");
  }

  @Override
  public int getDefaultIndex()
  {
    return 1;
  }

  @Override
  public boolean getDefaultEnabled()
  {
    return false;
  }

  @Override
  public int getHeight()
  {
    return 150;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    KontoList l = new KontoList(KontoUtil.getKonten(KontoFilter.ACTIVE),new KontoNew());
    l.setShowFilter(false);
    l.paint(parent);
  }

  @Override
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }

}
