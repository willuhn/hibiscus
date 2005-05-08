/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzList.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/05/08 17:48:51 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Umsaetzen.
 */
public class UmsatzList extends TablePart implements Part
{

  private I18N i18n;

  /**
   * @param konto
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(Konto konto, Action action) throws RemoteException
  {
    this(konto,0,action);
  }

  /**
   * @param konto
   * @param days
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(Konto konto, int days, Action action) throws RemoteException
  {
    this(konto.getUmsaetze(days), action);
  }

  /**
   * @param list
   * @param action
   * @throws RemoteException
   */
  public UmsatzList(GenericIterator list, Action action) throws RemoteException
  {
    super(list, action);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        Umsatz u = (Umsatz) item.getData();
        if (u == null) return;
        try {
          if (u.getBetrag() < 0.0)
          {
            item.setForeground(Settings.getBuchungSollForeground());
          }
          else
          {
            item.setForeground(Settings.getBuchungHabenForeground());
          }
          // Waehrung des Kontos dranpappen
          item.setText(3,item.getText(3) + " " + u.getKonto().getWaehrung());
          item.setText(4,item.getText(4) + " " + u.getKonto().getWaehrung());
        }
        catch (RemoteException e)
        {
        }
      }
    });

    // BUGZILLA 23 http://www.willuhn.de/bugzilla/show_bug.cgi?id=23
    addColumn(i18n.tr("Gegenkonto"),                "empfaenger_name");
    addColumn(i18n.tr("Verwendungszweck"),          "zweck");
    addColumn(i18n.tr("Valuta"),                    "valuta", new DateFormatter(HBCI.DATEFORMAT));
    // BUGZILLA 66 http://www.willuhn.de/bugzilla/show_bug.cgi?id=66
    addColumn(i18n.tr("Saldo zu diesem Zeitpunkt"), "saldo",  new CurrencyFormatter("",HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Betrag"),                    "betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));

    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UmsatzList());
  }
}


/**********************************************************************
 * $Log: UmsatzList.java,v $
 * Revision 1.2  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/