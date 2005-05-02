/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/KontoList.java,v $
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

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste aller Konten.
 */
public class KontoList extends TablePart implements Part
{

  private I18N i18n;

  /**
   * @param action
   * @throws RemoteException
   */
  public KontoList(Action action) throws RemoteException
  {
    super(Settings.getDBService().createList(Konto.class), action);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    addColumn(i18n.tr("Kontonummer"),"kontonummer");
    addColumn(i18n.tr("Bankleitzahl"),"blz");
    addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
    addColumn(i18n.tr("Kontoinhaber"),"name");
    addColumn(i18n.tr("Saldo"),"saldo");
    setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        Konto k = (Konto) item.getData();
        try {
          item.setText(4,HBCI.DECIMALFORMAT.format(k.getSaldo()) + " " + k.getWaehrung());
        }
        catch (RemoteException e)
        {
          Logger.error("error while formatting saldo",e);
        }
      }
    });
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.KontoList());
  }

}


/**********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/