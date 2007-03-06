/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/Kategorien.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/03/06 20:06:24 $
 * $Author: jost $
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
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.controller.KategorienControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View zur Anzeige der Umsatz-Kategorien.
 */
public class Kategorien extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();

    GUI.getView().setTitle(i18n.tr("Umsatz-Kategorien"));

    final KategorienControl control = new KategorienControl(this);

    LabelGroup settings = new LabelGroup(getParent(), i18n.tr("Filter"));

    settings.addLabelPair(i18n.tr("Konto"), control.getKontoAuswahl());
    settings.addLabelPair(i18n.tr("Start-Datum"), control.getStart());
    settings.addLabelPair(i18n.tr("Ende-Datum"), control.getEnd());

    ButtonArea buttons1 = new ButtonArea(settings.getComposite(), 1);

    buttons1.addButton(i18n.tr("Anzeigen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          TreePart tree = control.getTree();
          tree.paint(getParent());
        }
        catch (RemoteException e)
        {
          e.printStackTrace();
        }
      }
    });

    TreePart tree = new TreePart(control.getTreeData(), null);
    tree.paint(getParent());

    ButtonArea buttons2 = new ButtonArea(getParent(), 1);
    buttons2.addButton(i18n.tr("Zurück"), new Back(), null, true);
  }

}
/*******************************************************************************
 * $Log: Kategorien.java,v $
 * Revision 1.1  2007/03/06 20:06:24  jost
 * Neu: Umsatz-Kategorien-Ãœbersicht
 *
 ******************************************************************************/
