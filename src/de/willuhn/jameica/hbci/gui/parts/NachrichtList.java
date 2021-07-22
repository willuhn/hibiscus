/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
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
   * ct.
   * @param action
   * @throws RemoteException
   */
  public NachrichtList(Action action) throws RemoteException
  {
    this(init(), action);
  }

  /**
   * ct.
   * @param list Liste der Nachrichten.
   * @param action
   */
  public NachrichtList(GenericIterator list, Action action)
  {
    super(list,action);
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
          item.setFont(n.isGelesen() ? Font.DEFAULT.getSWTFont() : Font.BOLD.getSWTFont());
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
        return i18n.tr("{0} [BLZ: {1}]", new String[] {HBCIProperties.getNameForBank(blz),blz});
      }
    });
    addColumn(i18n.tr("Nachricht"), "nachricht", new Formatter()
    {
      public String format(Object o)
      {
        if (o == null)
          return null;
        String s = (String) o;
        if (s.indexOf('\n') != -1)
          return s;
        // Ist das eine Sparkassen-Eigenart, dass die Nachrichten Festbreite haben?
        // Na gut, dann nehmen wir die alle ueberfluessigen Leerzeichen raus und brechen hart um.
        s = s.replaceAll("( {1,})"," ");
        s = s.replaceAll("(.{77})","$1\n");
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
    HBCIDBService service = (HBCIDBService) Settings.getDBService();

    DBIterator list = service.createList(Nachricht.class);
    list.setOrder("ORDER BY gelesen, blz, " + service.getSQLTimestamp("datum") + " desc");
    return list;
  }
}


/**********************************************************************
 * $Log: NachrichtList.java,v $
 * Revision 1.7  2011/06/30 16:29:41  willuhn
 * @N Unterstuetzung fuer neues UnreadCount-Feature
 *
 * Revision 1.6  2008/12/17 22:53:39  willuhn
 * @R t o d o  tag entfernt
 *
 * Revision 1.5  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.4  2006/11/16 22:29:46  willuhn
 * @N Bug 331
 *
 * Revision 1.3  2006/03/23 23:44:58  willuhn
 * @N Umbruch der System-Nachrichten
 *
 * Revision 1.2  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/