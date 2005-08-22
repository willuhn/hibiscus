/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SammelLastschriftList.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/08/22 10:36:37 $
 * $Author: willuhn $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Sammel-Lastschriften.
 */
public class SammelLastschriftList extends TablePart implements Part
{
  private I18N i18n = null;

  /**
   * @param action
   * @throws RemoteException
   */
  public SammelLastschriftList(Action action) throws RemoteException
  {
    super(init(), action);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        SammelLastschrift l = (SammelLastschrift) item.getData();
        if (l == null)
          return;

        try {
          if (l.getTermin().before(new Date()) && !l.ausgefuehrt())
          {
            item.setForeground(Settings.getUeberfaelligForeground());
          }
        }
        catch (RemoteException e) { /*ignore */}
      }
    });
    addColumn(i18n.tr("Empfänger-Konto"),"konto_id");
    addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
    addColumn(i18n.tr("Anzahl Buchungen"),"anzahl");
    addColumn(i18n.tr("Summe"),"summe", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
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
  
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.SammelLastschriftList());
  }

  // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
  /**
   * Initialisiert die Liste der Sammel-Lastschriften.
   * @return Initialisiert die Liste der Sammel-Lastschriften.
   * @throws RemoteException
   */
  private static DBIterator init() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(SammelLastschrift.class);
    list.setOrder("ORDER BY TONUMBER(termin) DESC");
    return list;
  }

}


/**********************************************************************
 * $Log: SammelLastschriftList.java,v $
 * Revision 1.4  2005/08/22 10:36:37  willuhn
 * @N bug 115, 116
 *
 * Revision 1.3  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.2  2005/06/23 21:13:03  web0
 * @B bug 84
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/