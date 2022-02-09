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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * zeigt eine Liste mit neu hinzugekommenen Umsaetzen an.
 */
public class NeueUmsaetze extends AbstractBox
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public boolean getDefaultEnabled()
  {
    return true;
  }

  @Override
  public int getDefaultIndex()
  {
    return 6;
  }

  @Override
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Neue Umsätze");
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    GenericIterator list = de.willuhn.jameica.hbci.messaging.NeueUmsaetze.getNeueUmsaetze();
    UmsatzList umsaetze = new UmsatzList(list,new UmsatzDetail());
    umsaetze.addColumn(new KontoColumn());
    umsaetze.setFilterVisible(false);
    umsaetze.paint(parent);
  }

  @Override
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }
  
  @Override
  public int getHeight()
  {
    return 180;
  }
}
