/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UmsatzDetail.java,v $
 * $Revision: 1.40 $
 * $Date: 2011/05/27 06:33:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.action.UmsatzMarkChecked;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportUmsatzList;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;

/**
 * Bildet die Detailansicht einer Buchung ab.
 */
public class UmsatzDetail extends AbstractUmsatzDetail
{
  private Button checked = null;
  
  private UmsatzDetailControl control = null;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    super.bind();

    ButtonArea buttons = new ButtonArea();
    
    Umsatz u = getControl().getUmsatz();
    
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportUmsatzList(u)));
    
    this.checked = new Button(i18n.tr("Geprüft"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        new UmsatzMarkChecked(Umsatz.FLAG_CHECKED,true).handleAction(context);
        checked.setEnabled(false); // nur einmal moeglich
      }
    },u,false,"emblem-default.png");
    checked.setEnabled(!u.hasFlag(Umsatz.FLAG_NOTBOOKED) && !u.hasFlag(Umsatz.FLAG_CHECKED));
    buttons.addButton(checked);
    
    Button ab = null;
    final Address found = getControl().getAddressbookEntry();
    if (found != null)
    {
      ab = new Button(i18n.tr("In Adressbuch öffnen"),new de.willuhn.jameica.hbci.gui.action.EmpfaengerNew(),found,false,"contact-new.png");
    }
    else
    {
      ab = new Button(i18n.tr("In Adressbuch übernehmen"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          new EmpfaengerAdd().handleAction(getControl().getUmsatz());
        }
      },null,false,"contact-new.png");
    }
    buttons.addButton(ab);

    Button edit = new Button(i18n.tr("Bearbeiten"),new de.willuhn.jameica.hbci.gui.action.UmsatzDetailEdit(),u,false,"text-x-generic.png");
    edit.setEnabled((u.getFlags() & Umsatz.FLAG_NOTBOOKED) == 0);
    buttons.addButton(edit);
    
    Button store = new Button(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        getControl().handleStore();
      }
    },null,false,"document-save.png");
    store.setEnabled((u.getFlags() & Umsatz.FLAG_NOTBOOKED) == 0);
    buttons.addButton(store);

    buttons.paint(getParent());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.views.AbstractUmsatzDetail#getControl()
   */
  protected UmsatzDetailControl getControl()
  {
    if (this.control == null)
      this.control = new UmsatzDetailControl(this);
    return this.control;
  }
}


/**********************************************************************
 * $Log: UmsatzDetail.java,v $
 * Revision 1.40  2011/05/27 06:33:30  willuhn
 * @C Button-Reihenfolge
 *
 * Revision 1.39  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 **********************************************************************/