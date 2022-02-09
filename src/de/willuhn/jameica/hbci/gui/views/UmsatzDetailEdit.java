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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.Back;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailEditControl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.ApplicationException;

/**
 * Bildet die Edit-Ansicht einer Buchung ab.
 */
public class UmsatzDetailEdit extends AbstractUmsatzDetail
{
  private UmsatzDetailEditControl control = null;

  @Override
  public void bind() throws Exception
  {
    super.bind();
    
    final UmsatzDetailEditControl control = this.getControl();
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("&Speichern"),new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true,"document-save.png");

    buttons.addButton(i18n.tr("Speichern und &Zur�ck"),new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        if (getControl().handleStore())
          new Back().handleAction(context);
      }
    },null,true,"go-previous.png");

    Konto k = control.getUmsatz().getKonto();
    if (k != null && k.hasFlag(Konto.FLAG_OFFLINE))
    {
      buttons.addButton(i18n.tr("Speichern und &weiteren Umsatz anlegen"),new Action()
      {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          control.handleNext();
        }
      },null,false,"go-next.png");
    }
    
    buttons.paint(getParent());
  }

  @Override
  protected UmsatzDetailEditControl getControl()
  {
    if (this.control == null)
      this.control = new UmsatzDetailEditControl(this);
    return this.control;
  }
}
