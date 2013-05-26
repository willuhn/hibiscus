/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/ProtokollList.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/01/20 17:13:21 $
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
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.util.DateUtil;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit den Protokollen eines Kontos.
 */
public class ProtokollList extends AbstractFromToList
{
  private KontoInput kontoAuswahl = null;
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
   * Ueberschrieben, weil der User das hier nicht auswaehlen koennen soll.
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#getKonto()
   */
  public KontoInput getKonto() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;
    
    this.kontoAuswahl = new KontoInput(this.konto,KontoFilter.ALL);
    this.kontoAuswahl.setEnabled(false);
    this.kontoAuswahl.setComment(null);
    return this.kontoAuswahl;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractFromToList#getList(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date, java.util.Date, java.lang.String)
   */
  protected DBIterator getList(Object konto, Date from, Date to, String text) throws RemoteException
  {
    if (konto == null || !(konto instanceof Konto))
        return null;

    DBIterator list = ((Konto) konto).getProtokolle();
    if (from != null) list.addFilter("datum >= ?", new Object[]{new java.sql.Date(DateUtil.startOfDay(from).getTime())});
    if (to   != null) list.addFilter("datum <= ?", new Object[]{new java.sql.Date(DateUtil.endOfDay(to).getTime())});
    if (text != null && text.length() > 0)
    {
      list.addFilter("LOWER(kommentar) like ?", new Object[]{"%" + text.toLowerCase() + "%"});
    }
    
    return list;
  }
}


/**********************************************************************
 * $Log: ProtokollList.java,v $
 * Revision 1.6  2011/01/20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.5  2010-08-16 11:13:52  willuhn
 * @N In den Auftragslisten kann jetzt auch nach einem Text gesucht werden
 *
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