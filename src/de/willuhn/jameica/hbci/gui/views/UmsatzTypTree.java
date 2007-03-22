/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UmsatzTypTree.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/03/22 22:36:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.controller.UmsatzTypTreeControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View zur Anzeige der Umsatz-Kategorien.
 */
public class UmsatzTypTree extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();

    GUI.getView().setTitle(i18n.tr("Umsätze nach Kategorien"));

    final UmsatzTypTreeControl control = new UmsatzTypTreeControl(this);

    LabelGroup settings = new LabelGroup(getParent(), i18n.tr("Anzeige einschränken"));

    settings.addLabelPair(i18n.tr("Konto"), control.getKontoAuswahl());
    settings.addLabelPair(i18n.tr("Start-Datum"), control.getStart());
    settings.addLabelPair(i18n.tr("End-Datum"), control.getEnd());

    // Hilfs-Composite, damit wir dessen Inhalt sauber disposen koennen.
    final Composite comp = new Composite(getParent(),SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    comp.setLayout(new GridLayout());
    control.getTree().paint(comp);

    ButtonArea buttons = new ButtonArea(getParent(), 2);
    buttons.addButton(i18n.tr("Zurück"), new Back());
    buttons.addButton(i18n.tr("Aktualisieren"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleReload(comp);
      }
    },null,true);
  }

}
/*******************************************************************************
 * $Log: UmsatzTypTree.java,v $
 * Revision 1.1  2007/03/22 22:36:42  willuhn
 * @N Contextmenu in Trees
 * @C Kategorie-Baum in separates TreePart ausgelagert
 *
 * Revision 1.3  2007/03/21 18:47:36  willuhn
 * @N Neue Spalte in Kategorie-Tree
 * @N Sortierung des Kontoauszuges wie in Tabelle angezeigt
 * @C Code cleanup
 *
 * Revision 1.2  2007/03/07 10:29:41  willuhn
 * @B rmi compile fix
 * @B swt refresh behaviour
 *
 * Revision 1.1  2007/03/06 20:06:24  jost
 * Neu: Umsatz-Kategorien-Ãœbersicht
 *
 ******************************************************************************/
