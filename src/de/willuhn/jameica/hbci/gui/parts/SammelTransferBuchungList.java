/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SammelTransferBuchungList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:50 $
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
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Tabelle mit Buchungen eines Sammel-Auftrages.
 */
public class SammelTransferBuchungList extends TablePart
{
  private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  // BUGZILLA 107 http://www.willuhn.de/bugzilla/show_bug.cgi?id=107
 
  /**
   * ct.
   * @param list Liste von Buchungen (SammelTransferBuchung).
   * @param action Aktion, die beim Klick ausgefuehrt werden soll.
   */
  public SammelTransferBuchungList(final DBIterator list, Action action)
  {
    super(list,action);
    addColumn(i18n.tr("Auftrag"),"this", new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof SammelTransferBuchung))
          return null;
        try
        {
          SammelTransferBuchung sb = (SammelTransferBuchung) o;
          SammelTransfer s = sb.getSammelTransfer();
          if (s == null)
            return null;
          return i18n.tr("{0}: {1}", new String[]{HBCI.DATEFORMAT.format(s.getTermin()),s.getBezeichnung()});
        }
        catch (RemoteException e)
        {
          Logger.error("unable to read name of sammeltransfer",e);
          return i18n.tr("Zugehöriger Sammel-Auftrag nicht ermittelbar");
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
        if (o == null || !(o instanceof SammelTransferBuchung))
          return null;
        try
        {
          SammelTransferBuchung b = (SammelTransferBuchung) o;
          SammelTransfer s = b.getSammelTransfer();
          String curr = HBCIProperties.CURRENCY_DEFAULT_DE;
          if (s != null)
            curr = s.getKonto().getWaehrung();
          return new CurrencyFormatter(curr,HBCI.DECIMALFORMAT).format(new Double(b.getBetrag()));
        }
        catch (RemoteException e)
        {
          Logger.error("unable to read sammeltransfer");
          return i18n.tr("Betrag nicht ermittelbar");
        }
      }
    });

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          SammelTransferBuchung b = (SammelTransferBuchung) item.getData();
          if (b.getSammelTransfer().ausgefuehrt())
          {
            item.setForeground(Color.COMMENT.getSWTColor());
          }
        }
        catch (RemoteException e) {
          Logger.error("unable to read sammeltransfer",e);
        }
      }
    });
  }
  
  /**
   * ct.
   * @param a der Sammel-Auftrag, fuer den die Buchungen angezeigt werden sollen.
   * @param action Aktion, die beim Klick ausgefuehrt werden soll.
   * @throws RemoteException
   */
  public SammelTransferBuchungList(final SammelTransfer a, Action action) throws RemoteException
  {
    super(a.getBuchungen(), action);
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Kontoinhaber"),"gegenkonto_name");
    addColumn(i18n.tr("Kontonummer"),"gegenkonto_nr");
    addColumn(i18n.tr("Bankleitzahl"),"gegenkonto_blz");
    Konto k = a.getKonto();
    String curr = k != null ? k.getWaehrung() : "";
    addColumn(i18n.tr("Betrag"),"betrag",new CurrencyFormatter(curr,HBCI.DECIMALFORMAT));

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          if (a.ausgefuehrt())
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
 * $Log: SammelTransferBuchungList.java,v $
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/