/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/WelcomeControl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/04/19 22:53:52 $
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
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Startseite.
 */
public class WelcomeControl extends AbstractControl {

	private I18N i18n = null;
	private FormTextPart welcomeText = null;
	 
  /**
   * @param view
   */
  public WelcomeControl(AbstractView view) {
    super(view);
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert eine Tabelle mit allen offenen Ueberweisungen.
	 * @return Tabelle.
	 * @throws RemoteException
	 */
	public TablePart getOffeneUeberweisungen() throws RemoteException
	{
		DBIterator list = Settings.getDatabase().createList(Ueberweisung.class);
		list.addFilter("ausgefuehrt = 0");

		TablePart table = new TablePart(list,this);
		table.setFormatter(new TableFormatter() {
      public void format(TableItem item) {
				try {
					Date current = new Date();
					Ueberweisung u = (Ueberweisung) item.getData();
					if (u.getTermin().after(current))
						item.setBackground(Settings.getBuchungSollBackground());
				}
				catch (RemoteException e)
				{ /* ignore */}
      }
    });
		table.addColumn(i18n.tr("Konto"),"konto_id");
		table.addColumn(i18n.tr("Kto. des Empfängers"),"empfaenger_konto");
		table.addColumn(i18n.tr("BLZ des Empfängers"),"empfaenger_blz");
		table.addColumn(i18n.tr("Name des Empfängers"),"empfaenger_name");
		table.addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter("",HBCI.DECIMALFORMAT));
		table.addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.LONGDATEFORMAT));
		return table;
	}

	/**
	 * Liefert einen formatierten Welcome-Text.
   * @return Welcome-Text.
   * @throws RemoteException
   */
  public FormTextPart getWelcomeText() throws RemoteException
	{
		if (welcomeText != null)
			return welcomeText;

		StringBuffer buffer = new StringBuffer();
		buffer.append("<form>");
		buffer.append("<p><span color=\"header\" font=\"header\">" + i18n.tr("Quicklinks") + "</span></p>");
		buffer.append("<li><a href=\"" + UeberweisungNeu.class.getName() + "\">" + i18n.tr("Neue Überweisung") + "</a></li>");
		buffer.append("</form>");

		welcomeText = new FormTextPart(buffer.toString());
		welcomeText.addHyperlinkListener(new Listener() {
      public void handleEvent(Event event) {
      	GUI.startView(event.data.toString(),null);
      }
    });
		return welcomeText;
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