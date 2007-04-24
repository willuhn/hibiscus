/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/ProtokollList.java,v $
 * $Revision: 1.4 $
 * $Date: 2007/04/24 16:55:00 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit den Protokollen eines Kontos.
 */
public class ProtokollList extends AbstractFromToList
{

  private Konto konto = null;
  
  /**
   * ct.
   * @param konto
   * @param action
   */
  public ProtokollList(Konto konto, Action action)
  {
    super(action);
    this.konto = konto;
    
    this.setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        Protokoll p = (Protokoll) item.getData();
        if (p == null)
          return;
        try
        {
          int type = p.getTyp();
          switch (type)
          {
            case Protokoll.TYP_ERROR:
              item.setForeground(Color.ERROR.getSWTColor());
              break;
            case Protokoll.TYP_SUCCESS:
              item.setForeground(Color.SUCCESS.getSWTColor());
              break;
            default:
              // none
          }
        }
        catch (RemoteException e)
        {
        }
      }
    });
    this.addColumn(i18n.tr("Datum"),"datum",new DateFormatter(HBCI.LONGDATEFORMAT));
    this.addColumn(i18n.tr("Kommentar"),"kommentar");
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#getList(java.util.Date, java.util.Date)
   */
  protected GenericIterator getList(Date from, Date to) throws RemoteException
  {
    DBIterator list = konto.getProtokolle();
    if (from != null) list.addFilter("datum >= ?", new Object[]{new java.sql.Date(HBCIProperties.startOfDay(from).getTime())});
    if (to   != null) list.addFilter("datum <= ?", new Object[]{new java.sql.Date(HBCIProperties.endOfDay(to).getTime())});
    return list;
  }
}


/**********************************************************************
 * $Log: ProtokollList.java,v $
 * Revision 1.4  2007/04/24 16:55:00  willuhn
 * @N Aktualisierte Daten nur bei geaendertem Datum laden
 *
 * Revision 1.3  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.2  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/