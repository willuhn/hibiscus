/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/FirstStart.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/10/17 15:12:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoList;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.dialogs.PassportAuswahlDialog;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Hilfe-Seite fuer den ersten Start.
 */
public class FirstStart extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    GUI.getView().setTitle(i18n.tr("Hibiscus - Erste Schritte"));

    FormTextPart text = new FormTextPart();
    text.setText("<form><p>" + i18n.tr("Sie starten Hibiscus zum ersten Mal. Bitte richten Sie " +
        "zunächst ein Sicherheitsmedium (Chipkarte, Schlüsseldiskette oder PIN/TAN) ein. " +
        "Wechseln Sie anschliessend zur Konten-Übersicht und rufen Sie die verfügbaren Konten ab " +
        "oder richten Sie diese manuell ein, falls Ihre Bank das automatische Abrufen der " +
        "HBCI-Konten nicht unterstützt.") + "</p></form>");
    text.paint(getParent());
    
    ButtonArea buttons = new ButtonArea(getParent(),2);
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
    buttons.addButton(i18n.tr("Sicherheitsmedium einrichten"),a,null,true);
    buttons.addButton(i18n.tr("Konten-Übersicht"),a2,null);
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/*********************************************************************
 * $Log: FirstStart.java,v $
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