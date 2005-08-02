/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/Attic/SammelLastBuchungList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/08/02 20:09:33 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Tabelle mit Buchungen einer Sammel-Lastschrift.
 */
public class SammelLastBuchungList extends TablePart
{
  private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @param l die Lastschrift, fuer die die Buchungen angezeigt werden sollen.
   * @param action Aktion, die beim Klick ausgefuehrt werden soll.
   * @throws RemoteException
   */
  public SammelLastBuchungList(final SammelLastschrift l, Action action) throws RemoteException
  {
    super(l.getBuchungen(), action);
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Kontoinhaber"),"gegenkonto_name");
    addColumn(i18n.tr("Kontonummer"),"gegenkonto_nr");
    addColumn(i18n.tr("Bankleitzahl"),"gegenkonto_blz");
    Konto k = l.getKonto();
    String curr = k != null ? k.getWaehrung() : "";
    addColumn(i18n.tr("Betrag"),"betrag",new CurrencyFormatter(curr,HBCI.DECIMALFORMAT));

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          if (l.ausgefuehrt())
          {
            item.setForeground(Color.COMMENT.getSWTColor());
          }
        }
        catch (RemoteException e) { /*ignore */}
      }
    });
  }

}


/*********************************************************************
 * $Log: SammelLastBuchungList.java,v $
 * Revision 1.1  2005/08/02 20:09:33  web0
 * @B bug 106
 *
 **********************************************************************/