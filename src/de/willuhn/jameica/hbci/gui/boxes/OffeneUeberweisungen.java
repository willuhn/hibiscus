/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/OffeneUeberweisungen.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/11/09 01:13:53 $
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
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Headline;
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
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return i18n.tr("Offene und fällige Überweisungen");
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 3;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(Ueberweisung.class);
    list.addFilter("ausgefuehrt = 0");
    list.addFilter("tonumber(termin) <= " + new Date().getTime());

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
    
    new Headline(parent,getName());
    offeneUeberweisungen.paint(parent);
  }

}


/*********************************************************************
 * $Log: OffeneUeberweisungen.java,v $
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/