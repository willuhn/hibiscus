/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/EinnahmenAusgaben.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/01/20 10:51:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.EinnahmeAusgabeExport;
import de.willuhn.jameica.hbci.gui.controller.EinnahmeAusgabeControl;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View zur Anzeige der Sparquote.
 */
public class EinnahmenAusgaben extends AbstractView
{
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    GUI.getView().setTitle(i18n.tr("Einnahmen/Ausgaben"));

    final EinnahmeAusgabeControl control = new EinnahmeAusgabeControl(this);

    LabelGroup group = new LabelGroup(getParent(), i18n.tr("Anzeige einschränken"));
    group.addLabelPair(i18n.tr("Konto"), control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Start-Datum"), control.getStart());
    group.addLabelPair(i18n.tr("End-Datum"), control.getEnd());

    ButtonArea buttons = new ButtonArea(getParent(), 3);
    buttons.addButton(new Back(false));
    buttons.addButton(i18n.tr("Exportieren..."), new EinnahmeAusgabeExport(), control.getWerte());
    buttons.addButton(i18n.tr("Aktualisieren"), new Action()
    {
    
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          control.handleReload();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to reload data",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Daten"),StatusBarMessage.TYPE_ERROR));
        }
      }
    
    },null,true);

    final TablePart table = control.getTable();
    table.paint(this.getParent());

  }
}
/*******************************************************************************
 * $Log: EinnahmenAusgaben.java,v $
 * Revision 1.5  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.4  2008/04/06 23:21:43  willuhn
 * @C Bug 575
 * @N Der Vereinheitlichung wegen alle Buttons in den Auswertungen nach oben verschoben. Sie sind dann naeher an den Filter-Controls -> ergonomischer
 *
 * Revision 1.3  2007/07/16 12:01:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2007/06/04 17:37:00  willuhn
 * @D javadoc
 * @C java 1.4 compatibility
 * @N table colorized
 *
 * Revision 1.1  2007/06/04 15:58:14  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 *
 ******************************************************************************/
