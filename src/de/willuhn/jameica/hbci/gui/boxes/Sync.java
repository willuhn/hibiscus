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
import de.willuhn.jameica.hbci.gui.parts.SynchronizeList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Box zum Synchronisieren der Konten.
 */
public class Sync extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private SynchronizeList list = null;

  @Override
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Konten synchronisieren");
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    // BUGZILLA 433
    this.list = new SynchronizeList();
    list.paint(parent);
  }

  @Override
  public int getDefaultIndex()
  {
    return 2;
  }

  @Override
  public boolean getDefaultEnabled()
  {
    return true;
  }

  @Override
  public int getHeight()
  {
    return 200;
  }

  @Override
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }

}
