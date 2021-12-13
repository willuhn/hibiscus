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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoList;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;
import de.willuhn.util.I18N;

/**
 * Hilfe-Seite fuer den ersten Start.
 */
public class FirstStart extends AbstractBox
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public boolean isActive()
  {
    // Diese Box kann nur beim ersten Start ausgewaehlt/angezeigt werden.
    return Settings.isFirstStart();
  }
  
  @Override
  public boolean getDefaultEnabled()
  {
    // Diese Box kann nur beim ersten Start ausgewaehlt/angezeigt werden.
    return Settings.isFirstStart();
  }
  
  @Override
  public int getDefaultIndex()
  {
    return 0;
  }
  
  @Override
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Bank-Zugang einrichten");
  }
  
  @Override
  public boolean isEnabled()
  {
    // Diese Box kann nur beim ersten erfolgreichen Start ausgewaehlt/angezeigt werden.
    Manifest mf = Application.getPluginLoader().getManifest(HBCI.class);
    return  mf.isInstalled() && Settings.isFirstStart();
  }
  
  @Override
  public void paint(Composite parent) throws RemoteException
  {
    // Wir unterscheiden hier beim Layout nach Windows/OSX und Rest.
    // Unter Windows und OSX sieht es ohne Rahmen und ohne Hintergrund besser aus
    org.eclipse.swt.graphics.Color bg = null;
    int border = SWT.NONE;
    
    int os = Application.getPlatform().getOS();
    if (os != Platform.OS_WINDOWS && os != Platform.OS_WINDOWS_64 && os != Platform.OS_MAC)
    {
      bg = GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE);
      border = SWT.BORDER;
    }
    
    // 2-spaltige Anzeige. Links das Icon, rechts Text und Buttons
    Composite comp = new Composite(parent,border);
    comp.setBackground(bg);
    comp.setBackgroundMode(SWT.INHERIT_FORCE);
    comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    comp.setLayout(new GridLayout(2,false));
    
    // Linke Spalte mit dem Icon
    {
      GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
      gd.verticalSpan = 3;
      Label icon = new Label(comp,SWT.NONE);
      icon.setBackground(bg);
      icon.setLayoutData(gd);
      icon.setImage(SWTUtil.getImage("hibiscus-large.png"));
    }
    
    // Ueberschrift
    {
      Label title = new Label(comp,SWT.NONE);
      title.setBackground(bg);
      title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      title.setFont(Font.H2.getSWTFont());
      title.setText(i18n.tr("Sie starten Hibiscus zum ersten Mal."));
    }
    
    // Text
    {
      Label desc = new Label(comp,SWT.WRAP);
      desc.setBackground(bg);
      desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      desc.setText(i18n.tr("Bitte richten Sie zun�chst einen Bank-Zugang (Chipkarte, Schl�sseldatei oder PIN/TAN) ein.\n\n" +
          "Wechseln Sie anschlie�end zur Konten-�bersicht und pr�fen Sie die angelegten Konten. " +
          "Falls sie nicht automatisch angelegt wurden, dann erstellen Sie das Konto bitte manuell."));
    }
      
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Bank-Zugang einrichten"),new PassportDetail(),null,true,"system-users.png");
    buttons.addButton(i18n.tr("Konten-�bersicht"),new KontoList(),null,false,"wallet-open.png");
    buttons.paint(comp);
  }

  @Override
  public int getHeight()
  {
    return 180;
  }
}
