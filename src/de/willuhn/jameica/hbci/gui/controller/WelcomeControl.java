/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/WelcomeControl.java,v $
 * $Revision: 1.11 $
 * $Date: 2004/10/08 13:37:47 $
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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.menus.KontoList;
import de.willuhn.jameica.hbci.gui.menus.UeberweisungList;
import de.willuhn.jameica.hbci.gui.views.EmpfaengerNeu;
import de.willuhn.jameica.hbci.gui.views.KontoNeu;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Controller fuer die Startseite.
 */
public class WelcomeControl extends AbstractControl {

	private I18N i18n 											= null;
	private TablePart offeneUeberweisungen	= null;
	private FormTextPart quickLinks					= null;
	private TablePart kontoStats 						= null;
	 
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

		offeneUeberweisungen = new TablePart(list,this);
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
   * @throws RemoteException
   */
  public Part getQuickLinks() throws RemoteException
	{
		if (quickLinks != null)
			return quickLinks;

		StringBuffer buffer = new StringBuffer();
		buffer.append("<form><p/>");
		buffer.append("<p><span color=\"header\" font=\"header\">" + i18n.tr("Quicklinks") + "</span></p>");
		buffer.append("<li><a href=\"" + UeberweisungNeu.class.getName() + "\">" + i18n.tr("Neue Überweisung") + "</a></li>");
		buffer.append("<li><a href=\"" + EmpfaengerNeu.class.getName() + "\">" + i18n.tr("Neue Adresse") + "</a></li>");
		buffer.append("<li><a href=\"" + KontoNeu.class.getName() + "\">" + i18n.tr("Neues Konto") + "</a></li>");
		buffer.append("</form>");

		quickLinks= new FormTextPart(buffer.toString());
		quickLinks.addHyperlinkListener(new Listener() {
      public void handleEvent(Event event) {
      	GUI.startView(event.data.toString(),null);
      }
    });
		return quickLinks;
	}

	/**
	 * Liefert eine Kurzzusammenfassung der Konten.
   * @return Zusammenfassung der Konten.
   * @throws RemoteException
   */
  public Part getKontoStats() throws RemoteException
	{
		if (kontoStats != null)
			return kontoStats;

		DBIterator list = Settings.getDBService().createList(Konto.class);

		kontoStats = new TablePart(list,new KontoControl(null));
		kontoStats.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		kontoStats.addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
		kontoStats.addColumn(i18n.tr("Saldo"),"saldo");
		kontoStats.setFormatter(new TableFormatter()
		{
			public void format(TableItem item)
			{
				Konto k = (Konto) item.getData();
				try {
					item.setText(2,HBCI.DECIMALFORMAT.format(k.getSaldo()) + " " + k.getWaehrung());
				}
				catch (RemoteException e)
				{
					Logger.error("error while formatting saldo",e);
				}
			}
		});
    
		kontoStats.setContextMenu(new KontoList());
		return kontoStats;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
		GUI.startView(UeberweisungNeu.class.getName(),o);
  }

}


/**********************************************************************
 * $Log: WelcomeControl.java,v $
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