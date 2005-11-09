/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/NachrichtList.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/11/09 01:13:53 $
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

import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit System-Nachrichten.
 */
public class NachrichtList extends TablePart implements Part
{
  private I18N i18n = null;

  /**
   * @param action
   * @throws RemoteException
   */
  public NachrichtList(Action action) throws RemoteException
  {
    super(init(), action);
    this.setMulti(true);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        Nachricht n = (Nachricht) item.getData();
        try
        {
          if (!n.isGelesen())
          {
            // Ungelesene Nachrichten farbig
            item.setForeground(Settings.getUeberfaelligForeground());
          }
        }
        catch (RemoteException e)
        {
          Logger.error("error while checking for message read status",e);
        }
      }
    });
    addColumn(i18n.tr("Datum"),"datum",new DateFormatter(HBCI.DATEFORMAT));
    addColumn(i18n.tr("Bank"),"blz", new Formatter()
    {
      public String format(Object o)
      {
        if (o == null)
          return null;
        String blz = o.toString();
        return i18n.tr("{0} [BLZ: {1}]", new String[] {HBCIUtils.getNameForBLZ(blz),blz});
      }
    });
    addColumn(i18n.tr("Nachricht"),"nachricht", new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return null;
        String s = (String) o;
        // TODO Ist das eine Sparkassen-Eigenart, dass die Nachrichten Festbreite haben?
        // Na gut, wir brechen hart nach 100 Zeichen um, wenn keine Zeilenumbrueche drin sind
        if (s.indexOf('\n') != -1)
          return s;
        s = s.replaceAll("(.{100})","$1\n");
        return s;
      }
    });
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.NachrichtList());
  }

  /**
   * Liefert die sortierte Nachrichtenliste.
   * Das ist nur deshalb in einer extra Funktion ausgegliedert, weil der
   * Aufruf des super-Konstruktors zwingend in der ersten Zeile erfolgen muss.
   * @return Liste.
   * @throws RemoteException
   */
  private static DBIterator init() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(Nachricht.class);
    list.setOrder("ORDER BY gelesen, blz, TONUMBER(datum) desc");
    return list;
  }
}


/**********************************************************************
 * $Log: NachrichtList.java,v $
 * Revision 1.2  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/