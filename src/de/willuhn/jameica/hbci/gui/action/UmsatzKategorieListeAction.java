/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/UmsatzKategorieListeAction.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/04/29 10:18:46 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.util.Date;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UmsatzTypTreeControl;
import de.willuhn.jameica.hbci.gui.dialogs.UmsatzKategorieListeDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action für die Ausgabe eine Umsatz-Kategorie-Liste
 */
public class UmsatzKategorieListeAction implements Action
{
  /**
   * Erwartet ein Objekt vom Typ <code>GenericIterator</code>
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class)
        .getResources().getI18N();

    if (context == null)
      throw new ApplicationException(i18n.tr("Anwendungsfehler"));

    try
    {
      UmsatzTypTreeControl control = (UmsatzTypTreeControl) context;
      List list = control.getTree().getItems();
      Konto k = (Konto) control.getKontoAuswahl().getValue();
      Date start = (Date) control.getStart().getValue();
      Date end = (Date) control.getEnd().getValue();
      UmsatzKategorieListeDialog d = new UmsatzKategorieListeDialog(list, k,
          start, end);
      d.open();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while writing report", e);
      GUI.getStatusBar().setErrorText(
          i18n.tr("Fehler bei der Erstellung der Liste"));
    }
  }

}

/*******************************************************************************
 * $Log: UmsatzKategorieListeAction.java,v $
 * Revision 1.1  2007/04/29 10:18:46  jost
 * Neu: PDF-Ausgabe der UmsÃ¤tze nach Kategorien
 *
 ******************************************************************************/
