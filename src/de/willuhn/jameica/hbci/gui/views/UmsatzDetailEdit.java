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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailEditControl;
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
    buttons.addButton(i18n.tr("&Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        getControl().handleStore();
      }
    },null,true,"document-save.png");
    buttons.paint(getParent());
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