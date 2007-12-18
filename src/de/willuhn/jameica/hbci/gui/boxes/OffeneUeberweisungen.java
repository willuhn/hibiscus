/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/OffeneUeberweisungen.java,v $
 * $Revision: 1.9 $
 * $Date: 2007/12/18 17:10:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.menus.UeberweisungList;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Box fuer offene Ueberweisungen.
 */
public class OffeneUeberweisungen extends AbstractBox implements Box
{

  private I18N i18n = null;
  
  /**
   * ct.
   */
  public OffeneUeberweisungen()
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Offene und fällige Überweisungen");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 4;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(Ueberweisung.class);
    list.addFilter("ausgefuehrt = 0");
    list.addFilter("(banktermin = 1 OR termin <= ?)", new Object[]{new java.sql.Date(new Date().getTime())});

    TablePart offeneUeberweisungen = new TablePart(list,new de.willuhn.jameica.hbci.gui.action.UeberweisungNew());
    offeneUeberweisungen.setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          Date current = new Date();
          Ueberweisung u = (Ueberweisung) item.getData();
          if (u.getTermin().before(current))
          {
            item.setForeground(Settings.getUeberfaelligForeground());
          }
        }
        catch (RemoteException e) { /*ignore */}
      }
    });
    offeneUeberweisungen.addColumn(i18n.tr("Konto"),"konto_id");
    offeneUeberweisungen.addColumn(i18n.tr("Empfängers"),"empfaenger_name");
    offeneUeberweisungen.addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
    offeneUeberweisungen.addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.LONGDATEFORMAT));

    offeneUeberweisungen.setContextMenu(new UeberweisungList());
    offeneUeberweisungen.setSummary(false);
    
    offeneUeberweisungen.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }

}


/*********************************************************************
 * $Log: OffeneUeberweisungen.java,v $
 * Revision 1.9  2007/12/18 17:10:22  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 * Revision 1.8  2007/12/14 11:16:47  willuhn
 * @N Box "Offene und faellige Ueberweisungen" wieder aktiviert (Mail von Falk vom 14.12.2007)
 *
 * Revision 1.6  2006/08/25 10:13:43  willuhn
 * @B Fremdschluessel NICHT mittels PreparedStatement, da die sonst gequotet und von McKoi nicht gefunden werden. BUGZILLA 278
 *
 * Revision 1.5  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.4  2006/06/29 23:10:33  willuhn
 * @R Box-System aus Hibiscus in Jameica-Source verschoben
 * @C keine eigene Startseite mehr, jetzt alles ueber Jameica-Boxsystem geregelt
 *
 * Revision 1.3  2006/03/20 00:35:53  willuhn
 * @N new box "Konten-Übersicht"
 *
 * Revision 1.2  2006/01/18 10:08:21  willuhn
 * @N Termin-Ueberweisungen werden immer auf der Startseite angezeigt
 *
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/