/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/EinnahmenAusgaben.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/06/04 15:58:14 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.EinnahmeAusgabeExport;
import de.willuhn.jameica.hbci.gui.controller.EinnahmeAusgabeControl;
import de.willuhn.jameica.system.Application;
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
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();

    GUI.getView().setTitle(i18n.tr("Einnahmen/Ausgaben"));

    final EinnahmeAusgabeControl control = new EinnahmeAusgabeControl(this);

    LabelGroup group = new LabelGroup(getParent(), i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Auswahl"), control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Startdatum"), control.getStart());
    group.addLabelPair(i18n.tr("Endedatum"), control.getEnd());

    final TablePart table = control.getTable();
    table.paint(this.getParent());

    ButtonArea buttons = new ButtonArea(getParent(), 1);
    buttons
        .addButton("Export", new EinnahmeAusgabeExport(), control.getWerte());
  }
}
/*******************************************************************************
 * $Log: EinnahmenAusgaben.java,v $
 * Revision 1.1  2007/06/04 15:58:14  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 *
 ******************************************************************************/
