/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/FirstStart.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/03/29 15:30:31 $
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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoList;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.dialogs.PassportAuswahlDialog;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Hilfe-Seite fuer den ersten Start.
 */
public class FirstStart extends AbstractBox
{

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
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    return "Hibiscus: " + i18n.tr("Erste Schritte");
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
    final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    FormTextPart text = new FormTextPart();
    text.setText("<form><p><span color=\"header\" font=\"header\">" + i18n.tr("Herzlich willkommen") + "</span></p>" +
        "<p>" + i18n.tr("Sie starten Hibiscus zum ersten Mal. Bitte richten Sie " +
        "zunächst ein Sicherheitsmedium (Chipkarte, Schlüsseldiskette oder PIN/TAN) ein. " +
        "Wechseln Sie anschliessend zur Konten-Übersicht und rufen Sie die verfügbaren Konten ab " +
        "oder richten Sie diese manuell ein, falls Ihre Bank das automatische Abrufen der " +
        "HBCI-Konten nicht unterstützt.") + "</p></form>");
    
    text.paint(parent);
    
    ButtonArea buttons = new ButtonArea(parent,2);
    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        PassportAuswahlDialog d = new PassportAuswahlDialog(PassportAuswahlDialog.POSITION_CENTER);
        d.setTitle(i18n.tr("Auswahl des Sicherheitsmediums"));
        try
        {
          Passport p = (Passport) d.open();
          new PassportDetail().handleAction(p);
        }
        catch (OperationCanceledException oce)
        {
          // ignore
        }
        catch (Exception e)
        {
          Logger.error("error while opening passport dialog",e);
          GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Öffnen des Dialogs"));
        }
      }
    };
    Action a2 = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new KontoList().handleAction(null);
      }
    };
    buttons.addButton(i18n.tr("Sicherheitsmedium einrichten >>"),a,null,true);
    buttons.addButton(i18n.tr("Konten-Übersicht"),a2,null);
  }
}


/*********************************************************************
 * $Log: FirstStart.java,v $
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