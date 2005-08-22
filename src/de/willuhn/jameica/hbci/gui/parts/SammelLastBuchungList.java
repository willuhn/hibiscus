/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/Attic/SammelLastBuchungList.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/08/22 12:23:18 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Tabelle mit Buchungen einer Sammel-Lastschrift.
 */
public class SammelLastBuchungList extends TablePart
{
  private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  // BUGZILLA 107 http://www.willuhn.de/bugzilla/show_bug.cgi?id=107
  /**
   * ct.
   * @param list Liste von Buchungen (SammelLastBuchung).
   * @param action Aktion, die beim Klick ausgefuehrt werden soll.
   */
  public SammelLastBuchungList(final DBIterator list, Action action)
  {
    super(list,action);
    addColumn(i18n.tr("Lastschrift"),"slastschrift_id", new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof SammelLastschrift))
          return null;
        try
        {
          SammelLastschrift s = (SammelLastschrift) o;
          return i18n.tr("{0}: {1}", new String[]{HBCI.DATEFORMAT.format(s.getTermin()),s.getBezeichnung()});
        }
        catch (RemoteException e)
        {
          Logger.error("unable to read name of sammellastschrift",e);
          return i18n.tr("Lastschrift nicht ermittelbar");
        }
      }
    });
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Kontoinhaber"),"gegenkonto_name");
    addColumn(i18n.tr("Kontonummer"),"gegenkonto_nr");
    addColumn(i18n.tr("Bankleitzahl"),"gegenkonto_blz");
    addColumn(i18n.tr("Betrag"),"this",new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof SammelLastBuchung))
          return null;
        try
        {
          SammelLastBuchung b = (SammelLastBuchung) o;
          SammelLastschrift s = b.getSammelLastschrift();
          String curr = HBCIProperties.CURRENCY_DEFAULT_DE;
          if (s != null)
            curr = s.getKonto().getWaehrung();
          return new CurrencyFormatter(curr,HBCI.DECIMALFORMAT).format(new Double(b.getBetrag()));
        }
        catch (RemoteException e)
        {
          Logger.error("unable to read lastschrift");
          return i18n.tr("Betrag nicht ermittelbar");
        }
      }
    });

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          SammelLastBuchung b = (SammelLastBuchung) item.getData();
          if (b.getSammelLastschrift().ausgefuehrt())
          {
            item.setForeground(Color.COMMENT.getSWTColor());
          }
        }
        catch (RemoteException e) {
          Logger.error("unable to read sammellastschrift",e);
        }
      }
    });
  }
  
  /**
   * ct.
   * @param l die Lastschrift, fuer die die Buchungen angezeigt werden sollen.
   * @param action Aktion, die beim Klick ausgefuehrt werden soll.
   * @throws RemoteException
   */
  public SammelLastBuchungList(final SammelLastschrift l, Action action) throws RemoteException
  {
    super(l.getBuchungen(), action);
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Kontoinhaber"),"gegenkonto_name");
    addColumn(i18n.tr("Kontonummer"),"gegenkonto_nr");
    addColumn(i18n.tr("Bankleitzahl"),"gegenkonto_blz");
    Konto k = l.getKonto();
    String curr = k != null ? k.getWaehrung() : "";
    addColumn(i18n.tr("Betrag"),"betrag",new CurrencyFormatter(curr,HBCI.DECIMALFORMAT));

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          if (l.ausgefuehrt())
          {
            item.setForeground(Color.COMMENT.getSWTColor());
          }
        }
        catch (RemoteException e) { /*ignore */}
      }
    });
  }

}


/*********************************************************************
 * $Log: SammelLastBuchungList.java,v $
 * Revision 1.2  2005/08/22 12:23:18  willuhn
 * @N bug 107
 *
 * Revision 1.1  2005/08/02 20:09:33  web0
 * @B bug 106
 *
 **********************************************************************/