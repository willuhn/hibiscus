/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/EinnahmenAusgaben.java,v $
 * $Revision: 1.10 $
 * $Date: 2011/04/08 15:19:14 $
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
import java.util.List;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.EinnahmeAusgabeExport;
import de.willuhn.jameica.hbci.gui.controller.EinnahmeAusgabeControl;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View zur Anzeige der Auswertung "Einnahmen/Ausgaben".
 */
public class EinnahmenAusgaben extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Einnahmen/Ausgaben"));

    final EinnahmeAusgabeControl control = new EinnahmeAusgabeControl(this);

    LabelGroup group = new LabelGroup(getParent(), i18n.tr("Anzeige einschränken"));
    group.addInput(control.getKontoAuswahl());
    group.addInput(control.getStart());
    group.addInput(control.getEnd());

    ButtonArea buttons = new ButtonArea();
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
    
    },null,true,"view-refresh.png");
    buttons.addButton(i18n.tr("Exportieren..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          List data = control.getTable().getItems();
          new EinnahmeAusgabeExport().handleAction(data.toArray(new EinnahmeAusgabe[data.size()]));
        }
        catch (RemoteException re)
        {
          Logger.error("unable to export data",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Exportieren: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
        }
      }
    },null,false,"document-save.png");
    buttons.paint(getParent());
    
    control.getTable().paint(this.getParent());
  }
}
/*******************************************************************************
 * $Log: EinnahmenAusgaben.java,v $
 * Revision 1.10  2011/04/08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.9  2010-08-24 17:38:05  willuhn
 * @N BUGZILLA 896
 *
 * Revision 1.8  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.7  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.6  2009/04/05 21:16:22  willuhn
 * @B BUGZILLA 716
 *
 * Revision 1.5  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 ******************************************************************************/
