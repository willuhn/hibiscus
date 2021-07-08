/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.controller.UmsatzTypControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
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

    ColumnLayout columns = new ColumnLayout(getParent(),2);

    Container left = new SimpleContainer(columns.getComposite());

    left.addHeadline(i18n.tr("Eigenschaften"));
    left.addLabelPair(i18n.tr("Bezeichnung"), control.getName());
    left.addLabelPair(i18n.tr("Übergeordnete Kategorie"), control.getParent());

    left.addHeadline(i18n.tr("Zuordnung der Umsätze"));
    left.addInput(control.getKonto());
    left.addLabelPair(i18n.tr("Reihenfolge"), control.getNummer());
    left.addLabelPair(i18n.tr("Art des Umsatzes"), control.getArt());
    left.addLabelPair(i18n.tr("Suchbegriff"), control.getPattern());
    left.addCheckbox(control.getRegex(),i18n.tr("Suchbegriff ist ein regulärer Ausdruck"));

    Container right = new SimpleContainer(columns.getComposite(),true);
    right.addHeadline(i18n.tr("Notizen"));
    right.addPart(control.getKommentar());

    right.addHeadline(i18n.tr("Darstellung"));
    right.addInput(control.getSkipReport());
    right.addCheckbox(control.getCustomColor(),i18n.tr("Benutzerdefinierte Farbe"));
    right.addLabelPair(i18n.tr("Farbe"), control.getColor());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Löschen"),   new DBObjectDelete(),control.getCurrentObject(),false,"user-trash-full.png");
    buttons.addButton(i18n.tr("Duplizieren..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (control.handleStore())
        {
          try
          {
            new Duplicate().handleAction(control.getUmsatzTyp());
          }
          catch (RemoteException re)
          {
            Logger.error("unable to duplicate data",re);
            throw new ApplicationException(i18n.tr("Duplizieren fehlgeschlagen: {0}",re.getMessage()));
          }
        }
      }
    },null,false,"edit-copy.png");
    buttons.addButton(i18n.tr("&Speichern"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true,"document-save.png");

    buttons.paint(getParent());
  }
}
