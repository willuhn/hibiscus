/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/WelcomeControl.java,v $
 * $Revision: 1.17 $
 * $Date: 2005/03/31 23:05:46 $
 * $Author: web0 $
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
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.menus.UeberweisungList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Startseite.
 */
public class WelcomeControl extends AbstractControl {

	private I18N i18n 											= null;
	private TablePart offeneUeberweisungen	= null;
	private FormTextPart quickLinks					= null;
	 
  /**
   * @param view
   */
  public WelcomeControl(AbstractView view) {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert eine Tabelle mit allen offenen Ueberweisungen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public Part getOffeneUeberweisungen() throws RemoteException
	{
		if (offeneUeberweisungen != null)
			return offeneUeberweisungen;

		DBIterator list = Settings.getDBService().createList(Ueberweisung.class);
		list.addFilter("ausgefuehrt = 0");

		offeneUeberweisungen = new TablePart(list,new de.willuhn.jameica.hbci.gui.action.UeberweisungNew());
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

		return offeneUeberweisungen;
	}

  /**
	 * Liefert einen formatierten Welcome-Text.
   * @return Welcome-Text.
   * @throws Exception
   */
  public Part getQuickLinks() throws Exception
	{
		if (quickLinks != null)
			return quickLinks;

		StringBuffer buffer = new StringBuffer();
		buffer.append("<form><p/>");
		buffer.append("<p><span color=\"header\" font=\"header\">" + i18n.tr("Ihre Konten im Überblick") + "</span></p>");

    DBIterator i = Settings.getDBService().createList(Konto.class);
    while (i.hasNext())
    {
      Konto k = (Konto) i.next();
      buffer.append("<li>");
      buffer.append(k.getBezeichnung());
      buffer.append(" [" + i18n.tr("Kto-Nr.") + ": <a href=\"" + KontoNew.class.getName() + "\">" + k.getKontonummer() + "</a>] ");
      buffer.append(i18n.tr("Saldo") + ": " + HBCI.DECIMALFORMAT.format(k.getSaldo()) + k.getWaehrung());
      if (k.getSaldoDatum() != null)
        buffer.append(" [" + i18n.tr("aktualisiert am") + ": " + HBCI.DATEFORMAT.format(k.getSaldoDatum()) + "]");
      buffer.append("</li>");
      
    }
		buffer.append("</form>");

		quickLinks= new FormTextPart(buffer.toString());
		return quickLinks;
	}
}


/**********************************************************************
 * $Log: WelcomeControl.java,v $
 * Revision 1.17  2005/03/31 23:05:46  web0
 * @N geaenderte Startseite
 * @N klickbare Links
 *
 * Revision 1.16  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.15  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.13  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.11  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.9  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.8  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/20 23:31:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.5  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.4  2004/04/21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/04/19 22:53:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.1  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 **********************************************************************/