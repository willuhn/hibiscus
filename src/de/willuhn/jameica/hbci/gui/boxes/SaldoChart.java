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
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Box zur Anzeige des Saldo-Charts.
 */
public class SaldoChart extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();;
  
  @Override
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Saldo-Verlauf");
  }

  @Override
  public int getDefaultIndex()
  {
    return 4;
  }

  @Override
  public boolean getDefaultEnabled()
  {
    return false;
  }

  @Override
  public int getHeight()
  {
    return 230;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    de.willuhn.jameica.hbci.gui.parts.SaldoChart chart = new de.willuhn.jameica.hbci.gui.parts.SaldoChart();
    chart.setTinyView(true);
    chart.paint(parent);
  }

  @Override
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }
}


/*********************************************************************
 * $Log: SaldoChart.java,v $
 * Revision 1.2  2010/08/12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.1  2010-08-11 16:06:05  willuhn
 * @N BUGZILLA 783 - Saldo-Chart ueber alle Konten
 *
 **********************************************************************/