/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SammelLastschriftControl.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/02/28 18:40:49 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.gui.menus.SammelLastschriftList;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der Sammellastschriften".
 * @author willuhn
 */
public class SammelLastschriftControl extends AbstractControl
{

  private SammelLastschrift lastschrift = null;

  private I18N i18n = null;

  private TablePart table = null;

  /**
   * ct.
   * @param view
   */
  public SammelLastschriftControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert die aktuelle Sammel-Lastschrift oder erstellt eine neue.
   * @return Sammel-Lastschrift.
   * @throws RemoteException
   */
  public SammelLastschrift getLastschrift() throws RemoteException
  {
    if (lastschrift != null)
      return lastschrift;

    if (getCurrentObject() != null)
    {
      lastschrift = (SammelLastschrift) getCurrentObject();
      return lastschrift;
    }

    lastschrift = (SammelLastschrift) Settings.getDBService().createObject(SammelLastschrift.class,null);
    return lastschrift;
  }

  /**
   * Liefert eine Tabelle mit den existierenden Sammellastschriften.
   * @return Liste der Sammellastschriften.
   * @throws RemoteException
   */
  public TablePart getListe() throws RemoteException
  {
    if (table != null)
      return table;

    DBIterator list = Settings.getDBService().createList(SammelLastschrift.class);

    table = new TablePart(list,new SammelLastschriftNew());
    table.setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        SammelLastschrift l = (SammelLastschrift) item.getData();
        if (l == null)
          return;

        try {
          if (l.getTermin().before(new Date()) && !l.ausgefuehrt())
          {
            item.setForeground(Settings.getUeberfaelligForeground());
          }
        }
        catch (RemoteException e) { /*ignore */}
      }
    });
    table.addColumn(i18n.tr("Empfänger-Konto"),"konto_id");
    table.addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
    table.addColumn(i18n.tr("Buchungen"),"dummy",new Formatter()
    {
      public String format(Object o)
      {
        // Ueber dieses Pseudo-Attribut zeigen wir alle Buchungen der
        // Sammellastschrift an.
        try
        {
          StringBuffer sb = new StringBuffer();
          DBIterator di = getLastschrift().getBuchungen();
          while (di.hasNext())
          {
            SammelLastBuchung b = (SammelLastBuchung) di.next();
            String[] params = new String[]
            {
              b.getZweck(),
              b.getGegenkontoName(),
              HBCI.DECIMALFORMAT.format(b.getBetrag()),
              getLastschrift().getKonto().getWaehrung()
            };
            sb.append(i18n.tr("[{0}] {1}, Betrag {2} {3}",params));
            if (di.hasNext())
              sb.append("\n");
          }
          return sb.toString();
        }
        catch (RemoteException e)
        {
          Logger.error("error while reading buchungen",e);
          return i18n.tr("Buchungen nicht lesbar");
        }
      }
    });
    table.addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.LONGDATEFORMAT));
    table.addColumn(i18n.tr("Status"),"ausgefuehrt",new Formatter() {
      public String format(Object o) {
        try {
          int i = ((Integer) o).intValue();
          return i == 1 ? i18n.tr("ausgeführt") : i18n.tr("offen");
        }
        catch (Exception e) {}
        return ""+o;
      }
    });
  
    table.setContextMenu(new SammelLastschriftList());
    return table;
  }

}

/*****************************************************************************
 * $Log: SammelLastschriftControl.java,v $
 * Revision 1.2  2005/02/28 18:40:49  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/