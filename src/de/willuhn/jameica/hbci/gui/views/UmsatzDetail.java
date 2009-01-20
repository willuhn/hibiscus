/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UmsatzDetail.java,v $
 * $Revision: 1.34 $
 * $Date: 2009/01/20 10:51:45 $
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
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.util.ApplicationException;

/**
 * Bildet die Detailansicht einer Buchung ab.
 */
public class UmsatzDetail extends AbstractUmsatzDetail
{
  private UmsatzDetailControl control = null;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    super.bind();

    ButtonArea buttons = new ButtonArea(getParent(),4);
    buttons.addButton(new Back());
    buttons.addButton(i18n.tr("Bearbeiten"),new de.willuhn.jameica.hbci.gui.action.UmsatzDetailEdit(),getCurrentObject());
    buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    });
    
    Button ab = null;

    final Address found = control.getAddressbookEntry();
    if (found != null)
    {
      ab = new Button(i18n.tr("Gegenkonto In Adressbuch öffnen"),new de.willuhn.jameica.hbci.gui.action.EmpfaengerNew(),found);
    }
    else
    {
      ab = new Button(i18n.tr("Gegenkonto in Adressbuch übernehmen"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          new EmpfaengerAdd().handleAction(control.getUmsatz());
        }
      });
    }
    buttons.addButton(ab);
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
 * Revision 1.34  2009/01/20 10:51:45  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.33  2009/01/04 14:47:53  willuhn
 * @N Bearbeiten der Umsaetze nochmal ueberarbeitet - Codecleanup
 *
 * Revision 1.32  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 * Revision 1.31  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.30  2008/11/25 00:13:47  willuhn
 * @N Erweiterte Verwendungswecke anzeigen
 * @N Notizen nicht mehr in einem separaten Tab sondern in der rechten Spalte anzeigen
 *
 * Revision 1.29  2007/06/15 11:20:32  willuhn
 * @N Saldo in Kontodetails via Messaging sofort aktualisieren
 * @N Mehr Details in den Namen der Synchronize-Jobs
 * @N Layout der Umsatzdetail-Anzeige ueberarbeitet
 *
 * Revision 1.28  2007/04/24 17:52:17  willuhn
 * @N Bereits in den Umsatzdetails erkennen, ob die Adresse im Adressbuch ist
 * @C Gross-Kleinschreibung in Adressbuch-Suche
 **********************************************************************/