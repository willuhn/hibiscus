/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UmsatzTypDetail.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/11/23 23:24:17 $
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
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.controller.UmsatzTypControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Detail-Ansicht einer Umsatz-Kategorie.
 */
public class UmsatzTypDetail extends AbstractView
{
  
  /**
   * ct.
   */
  public UmsatzTypDetail()
  {
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


    final UmsatzTypControl control = new UmsatzTypControl(this);

		GUI.getView().setTitle(i18n.tr("Umsatz-Kategorie"));
		
    LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Bezeichnung"), control.getName());
    group.addLabelPair(i18n.tr("Suchbegriff"), control.getPattern());
    group.addCheckbox(control.getRegex(),i18n.tr("Suchbegriff ist ein regulärer Ausdruck"));
    group.addSeparator();
    group.addLabelPair(i18n.tr("Art des Umsatzes"), control.getArt());

    
    ButtonArea buttons = new ButtonArea(getParent(),3);
	  buttons.addButton(i18n.tr("Zurück"),    new Back(),null,true);
    buttons.addButton(i18n.tr("Löschen"),   new DBObjectDelete(),control.getCurrentObject());
    buttons.addButton(i18n.tr("Speichern"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true);
  }
}


/**********************************************************************
 * $Log: UmsatzTypDetail.java,v $
 * Revision 1.2  2006/11/23 23:24:17  willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 *
 * Revision 1.1  2006/11/23 17:25:37  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 **********************************************************************/