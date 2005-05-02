/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/ProtokollList.java,v $
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
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit den Protokollen eines Kontos.
 */
public class ProtokollList extends TablePart implements Part
{

  private I18N i18n = null;

  /**
   * @param list
   * @param action
   */
  public ProtokollList(Konto konto, Action action) throws RemoteException
  {
    super(konto.getProtokolle(), action);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        de.willuhn.jameica.hbci.rmi.Protokoll p = (de.willuhn.jameica.hbci.rmi.Protokoll) item.getData();
        if (p == null) return;
        try {
          if (p.getTyp() == de.willuhn.jameica.hbci.rmi.Protokoll.TYP_ERROR)
          {
            item.setForeground(Color.ERROR.getSWTColor());
          }
          else if (p.getTyp() == de.willuhn.jameica.hbci.rmi.Protokoll.TYP_SUCCESS)
          {
            item.setForeground(Color.SUCCESS.getSWTColor());
          }
        }
        catch (RemoteException e)
        {
        }
      }
    });
    addColumn(i18n.tr("Datum"),"datum",new DateFormatter(HBCI.LONGDATEFORMAT));
    addColumn(i18n.tr("Kommentar"),"kommentar");
    disableSummary();
  }

}


/**********************************************************************
 * $Log: ProtokollList.java,v $
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/