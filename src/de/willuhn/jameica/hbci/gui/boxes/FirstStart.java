/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/FirstStart.java,v $
 * $Revision: 1.12 $
 * $Date: 2011/06/09 10:07:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    // Diese Box kann nur beim ersten Start ausgewaehlt/angezeigt werden.
    return Settings.isFirstStart();
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    // Diese Box kann nur beim ersten Start ausgewaehlt/angezeigt werden.
    return Settings.isFirstStart();
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Bank-Zugang einrichten");
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isEnabled()
   */
  public boolean isEnabled()
  {
    // Diese Box kann nur beim ersten erfolgreichen Start ausgewaehlt/angezeigt werden.
    Manifest mf = Application.getPluginLoader().getManifest(HBCI.class);
    return  mf.isInstalled() && Settings.isFirstStart();
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
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
      icon.setImage(SWTUtil.getImage("hibiscus-icon-64x64.png"));
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
      desc.setText(i18n.tr("Bitte richten Sie zunächst einen Bank-Zugang (Chipkarte, Schlüsseldiskette oder PIN/TAN) ein.\n\n" +
          "Wechseln Sie anschließend zur Konten-Übersicht und prüfen Sie die angelegten Konten. " +
          "Falls sie nicht automatisch angelegt wurden, dann erstellen Sie das Konto bitte manuell."));
    }
      
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Bank-Zugang einrichten"),new PassportDetail(),null,true,"seahorse-preferences.png");
    buttons.addButton(i18n.tr("Konten-Übersicht"),new KontoList(),null,false,"system-file-manager.png");
    buttons.paint(comp);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  public int getHeight()
  {
    return 160;
  }
}


/*********************************************************************
 * $Log: FirstStart.java,v $
 * Revision 1.12  2011/06/09 10:07:45  willuhn
 * @C Rahmen und Hintergrundfarbe nur unter Windows/OSX anzeigen
 *
 * Revision 1.11  2011-06-08 13:37:00  willuhn
 * @N Neuer First-Start-Assistent
 *
 * Revision 1.10  2011-05-03 11:07:39  willuhn
 * @N Styling-Fixes fuer Windows (Background)
 *
 * Revision 1.9  2011-04-29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.8  2010-09-29 22:03:05  willuhn
 * @N Kann ja noch weiter verkuerzt werden ;)
 *
 * Revision 1.7  2010-09-29 22:01:43  willuhn
 * @R Dialog nicht noetig - macht die Action intern ohnehin auch nochmal
 *
 * Revision 1.6  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.5  2008/04/15 16:16:34  willuhn
 * @B BUGZILLA 584
 *
 * Revision 1.4  2007/12/29 18:45:37  willuhn
 * @N Hoehe von Boxen explizit konfigurierbar
 *
 * Revision 1.3  2007/03/29 15:30:31  willuhn
 * @N Uebersichtlichere Darstellung der Systemstart-Meldungen
 * @C FirstStart-View bei Initialisierungsfehler nicht anzeigen
 *
 * Revision 1.2  2007/03/02 14:49:14  willuhn
 * @R removed old firststart view
 * @C do not show boxes on first start
 *
 * Revision 1.1  2006/06/29 23:10:33  willuhn
 * @R Box-System aus Hibiscus in Jameica-Source verschoben
 * @C keine eigene Startseite mehr, jetzt alles ueber Jameica-Boxsystem geregelt
 *
 * Revision 1.4  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.3  2005/10/17 15:12:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/10/17 15:11:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/10/17 14:15:01  willuhn
 * @N FirstStart
 *
 **********************************************************************/