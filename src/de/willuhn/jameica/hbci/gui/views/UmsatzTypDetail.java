/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UmsatzTypDetail.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/06/08 08:12:48 $
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
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
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
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    final UmsatzTypControl control = new UmsatzTypControl(this);

		GUI.getView().setTitle(i18n.tr("Umsatz-Kategorie"));
		
    LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Bezeichnung"), control.getName());
    
    group.addCheckbox(control.getCustomColor(),i18n.tr("Benutzerdefinierte Farbe"));
    group.addLabelPair(i18n.tr("Farbe"), control.getColor());
    
    group.addLabelPair(i18n.tr("Übergeordnete Kategorie"), control.getParent());
    group.addLabelPair(i18n.tr("Reihenfolge"), control.getNummer());
    group.addLabelPair(i18n.tr("Suchbegriff"), control.getPattern());
    group.addCheckbox(control.getRegex(),i18n.tr("Suchbegriff ist ein regulärer Ausdruck"));
    group.addSeparator();
    group.addLabelPair(i18n.tr("Art des Umsatzes"), control.getArt());

    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Löschen"),   new DBObjectDelete(),control.getCurrentObject(),false,"user-trash-full.png");
    buttons.addButton(i18n.tr("Speichern"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true,"document-save.png");
    
    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: UmsatzTypDetail.java,v $
 * Revision 1.9  2011/06/08 08:12:48  willuhn
 * @C BUGZILLA 988 "Nummer" in "Reihenfolge" geaendert
 *
 * Revision 1.8  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.7  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 **********************************************************************/