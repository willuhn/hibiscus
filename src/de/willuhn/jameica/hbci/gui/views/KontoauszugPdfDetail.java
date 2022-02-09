/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.internal.action.Back;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.KontoauszugPdfControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View zum Anzeigen der Details des Kontoauszuges.
 */
public class KontoauszugPdfDetail extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Elektronischer Kontoauszug"));
    
    final KontoauszugPdfControl control = new KontoauszugPdfControl(this);

    SimpleContainer cont = new SimpleContainer(getParent());
    cont.addInput(control.getKonto());

    ColumnLayout columns = new ColumnLayout(getParent(),2);
    SimpleContainer left = new SimpleContainer(columns.getComposite());

    left.addHeadline(i18n.tr("Von der Bank übertragene Angaben"));
    {
      left.addInput(control.getErstellungsdatum());
      left.addInput(control.getJahr());
      left.addInput(control.getNummer());
      left.addLabelPair(i18n.tr("Von"),new MultiInput(control.getVonDatum(),control.getBisDatum()));
    }
    
    left.addHeadline(i18n.tr("Abruf"));
    {
      left.addInput(control.getAbrufdatum());
      left.addInput(control.getQuittierungsdatum());
    }

    SimpleContainer right = new SimpleContainer(columns.getComposite(),true);
    right.addHeadline(i18n.tr("Angaben zum Namen"));
    right.addInput(control.getName1());
    right.addInput(control.getName2());
    right.addInput(control.getName3());
    right.addHeadline(i18n.tr("Notizen"));
    right.addPart(control.getKommentar());

    SimpleContainer bottom = new SimpleContainer(getParent());
    bottom.addInput(control.getDatei());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("&Öffnen"),new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleOpen();
      }
    },null,true,"application-pdf.png");
    buttons.addButton(i18n.tr("&Speichern"),new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true,"document-save.png");

    buttons.addButton(i18n.tr("Speichern und &Zurück"),new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        if (control.handleStore())
          new Back().handleAction(context);
      }
    },null,true,"go-previous.png");
    buttons.paint(getParent());
  }

}


