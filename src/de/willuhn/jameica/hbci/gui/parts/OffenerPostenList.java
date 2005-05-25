/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/Attic/OffenerPostenList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/25 00:42:04 $
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
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.OffenerPosten;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit offenen Posten.
 */
public class OffenerPostenList extends TablePart implements Part
{
  private I18N i18n = null;

  /**
   * @param action
   * @throws RemoteException
   */
  public OffenerPostenList(Action action) throws RemoteException
  {
    super(Settings.getDBService().createList(OffenerPosten.class), action);
    this.setMulti(true);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        try
        {
          OffenerPosten p = (OffenerPosten) item.getData();
          if (p.isOffen())
            item.setForeground(Settings.getUeberfaelligForeground());
        }
        catch (Exception e)
        {
          Logger.error("unable to format OP entry",e);
        }
      }
    });
    addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
    addColumn(i18n.tr("Noch Offen"),"umsatz_id",new Formatter()
    {
      public String format(Object o)
      {
        return o == null ? i18n.tr("Ja") : i18n.tr("Nein");
      }
    });
    addColumn(i18n.tr("Kriterien"),"filter");
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.OffenerPostenList());
  }

}

/**********************************************************************
 * $Log: OffenerPostenList.java,v $
 * Revision 1.1  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 **********************************************************************/