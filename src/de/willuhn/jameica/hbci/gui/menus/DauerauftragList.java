/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/DauerauftragList.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/04/11 16:48:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.Print;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DauerauftragDelete;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.gui.action.KontoFetchDauerauftraege;
import de.willuhn.jameica.hbci.io.print.PrintSupportDauerauftrag;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit Dauerauftraegen gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class DauerauftragList extends ContextMenu
{
	private I18N i18n	= null;

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Dauerauftraegen.
	 */
	public DauerauftragList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"),            new DauerauftragNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neuer Dauerauftrag..."),    new DNeu(),"text-x-generic.png"));
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."),        new DauerauftragDelete(),"user-trash-full.png"));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new ContextMenuItem(i18n.tr("Daueraufträge abrufen..."), new KontoFetchDauerauftraege(),"mail-send-receive.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Drucken..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(new PrintSupportDauerauftrag((Dauerauftrag) context));
      }
    },"document-print.png"));
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> einen neuen Dauerauftrag
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class DNeu extends DauerauftragNew
	{
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
    	super.handleAction(null);
    }
	}
}


/**********************************************************************
 * $Log: DauerauftragList.java,v $
 * Revision 1.6  2011/04/11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 * Revision 1.5  2011-03-07 10:33:53  willuhn
 * @N BUGZILLA 999
 *
 * Revision 1.4  2008/12/19 01:12:09  willuhn
 * @N Icons in Contextmenus
 *
 * Revision 1.3  2006/08/07 14:45:18  willuhn
 * @B typos
 *
 * Revision 1.2  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/