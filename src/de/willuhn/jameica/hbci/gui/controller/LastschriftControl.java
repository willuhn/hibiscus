/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/LastschriftControl.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/02/04 18:27:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.menus.LastschriftList;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Transfer;

/**
 * Ueberschreiben wir von UeberweisungControl, weil es fast das
 * gleiche ist.
 */
public class LastschriftControl extends AbstractBaseUeberweisungControl
{

	private TablePart table = null;
  private Lastschrift transfer = null;
	
  /**
   * ct.
   * @param view
   */
  public LastschriftControl(AbstractView view)
  {
    super(view);
  }


  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractTransferControl#getTransfer()
   */
  public Transfer getTransfer() throws RemoteException
  {
    if (transfer != null)
      return transfer;

    transfer = (Lastschrift) getCurrentObject();
    if (transfer != null)
      return transfer;
      
    transfer = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
    return transfer;
  }

  /**
   * Liefert eine Liste existierender Lastschriften.
   * @return Liste der Lastschriften.
   * @throws RemoteException
   */
  public Part getLastschriftListe() throws RemoteException
  {
		if (table != null)
			return table;

		DBIterator list = Settings.getDBService().createList(Lastschrift.class);

		table = new TablePart(list,new LastschriftNew());
		table.setFormatter(new TableFormatter() {
			public void format(TableItem item) {
        Lastschrift l = (Lastschrift) item.getData();
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
		table.addColumn(i18n.tr("Zahlungspflichtiger"),"empfaenger_name");
		table.addColumn(i18n.tr("Belastetes Konto"),"empfaenger_konto");
		table.addColumn(i18n.tr("Verwendungszweck"),"zweck");
		table.addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
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
	
		table.setContextMenu(new LastschriftList());
		return table;
  }


}


/**********************************************************************
 * $Log: LastschriftControl.java,v $
 * Revision 1.4  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.3  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/01/19 00:33:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 **********************************************************************/