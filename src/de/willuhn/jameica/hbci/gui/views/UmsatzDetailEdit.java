/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UmsatzDetailEdit.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/04/08 15:19:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailEditControl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Bildet die Edit-Ansicht einer Buchung ab.
 */
public class UmsatzDetailEdit extends AbstractUmsatzDetail
{
  private UmsatzDetailControl control = null;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    super.bind();
    
    ButtonArea buttons = new ButtonArea();
    final Button newBooking=getNewBookingButton();
    if(newBooking!=null){
      buttons.addButton(newBooking);
    }
    buttons.addButton(i18n.tr("&Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        getControl().handleStore();
        if(newBooking!=null){
          newBooking.setEnabled(true);
        }
      }
    },null,true,"document-save.png");
    buttons.paint(getParent());
  }

  private Button getNewBookingButton(){
    Umsatz umsatz = getControl().getUmsatz();
    try
    {
      if(umsatz.isNewObject() && umsatz.getKonto().hasFlag(Konto.FLAG_OFFLINE)){
        Button button=new Button(i18n.tr("weiteren Umsatz anlegen"), new de.willuhn.jameica.hbci.gui.action.UmsatzDetailEdit(), umsatz.getKonto(), false, "emblem-documents.png");
        button.setEnabled(false);
        return button;
      }
    } catch (RemoteException e)
    {
      Logger.error("error while checking checking whether to add 'New Booking' button",e);
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.views.AbstractUmsatzDetail#getControl()
   */
  protected UmsatzDetailControl getControl()
  {
    if (this.control == null)
      this.control = new UmsatzDetailEditControl(this);
    return this.control;
  }
}


/**********************************************************************
 * $Log: UmsatzDetailEdit.java,v $
 * Revision 1.4  2011/04/08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.3  2009/05/28 10:45:18  willuhn
 * @N more icons
 *
 * Revision 1.2  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.1  2009/01/04 14:47:53  willuhn
 * @N Bearbeiten der Umsaetze nochmal ueberarbeitet - Codecleanup
 *
 * Revision 1.1  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 **********************************************************************/