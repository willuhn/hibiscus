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
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste aller Umsaetze an.
 */
public class Umsaetze extends AbstractBox
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 10;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Umsätze der letzten Tage");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final GenericIterator<Umsatz> list = UmsatzUtil.getUmsaetzeBackwards(UmsatzDaysInput.getDefaultDays());
    UmsatzList umsaetze = new UmsatzList(list,new UmsatzDetail());
    umsaetze.addColumn(new KontoColumn());
    umsaetze.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  public int getHeight()
  {
    return 500;
  }
}
