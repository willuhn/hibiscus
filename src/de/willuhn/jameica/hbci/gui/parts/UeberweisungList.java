/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UeberweisungList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/02 23:56:45 $
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
import java.util.Date;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Ueberweisungen.
 */
public class UeberweisungList extends TablePart implements Part
{
  private I18N i18n = null;

  /**
   * @param action
   * @throws RemoteException
   */
  public UeberweisungList(Action action) throws RemoteException
  {
    super(Settings.getDBService().createList(Ueberweisung.class), action);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        Ueberweisung u = (Ueberweisung) item.getData();
        if (u == null)
          return;

        try {
          if (u.getTermin().before(new Date()) && !u.ausgefuehrt())
          {
            item.setForeground(Settings.getUeberfaelligForeground());
          }
        }
        catch (RemoteException e) { /*ignore */}
      }
    });
    addColumn(i18n.tr("Konto"),"konto_id");
    addColumn(i18n.tr("Empfängername"),"empfaenger_name");
    addColumn(i18n.tr("Empfängerkonto"),"empfaenger_konto");
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.LONGDATEFORMAT));
    addColumn(i18n.tr("Status"),"ausgefuehrt",new Formatter() {
      public String format(Object o) {
        try {
          int i = ((Integer) o).intValue();
          return i == 1 ? i18n.tr("ausgeführt") : i18n.tr("offen");
        }
        catch (Exception e) {}
        return ""+o;
      }
    });
  
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UeberweisungList());
  }

}


/**********************************************************************
 * $Log: UeberweisungList.java,v $
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/