/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.Print;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.action.LastschriftExport;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.io.print.PrintSupportLastschriftList;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit Lastschriften gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class LastschriftList extends ContextMenu
{
	private I18N i18n	= null;

	/**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Lastschriften.
	 */
	public LastschriftList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new SingleItem(i18n.tr("Öffnen"), new LastschriftNew(),"document-open.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("In Adressbuch übernehmen"),new EmpfaengerAdd(),"contact-new.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Drucken..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(new PrintSupportLastschriftList(context));
      }
    },"document-print.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new LastschriftExport(),"document-save.png"));
	}

  /**
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class SingleItem extends CheckedContextMenuItem
  {
    /**
     * ct.
     * @param text Text.
     * @param action Aktion.
     * @param icon optionales Icon.
     */
    private SingleItem(String text, Action action, String icon)
    {
      super(text,action,icon);
    }
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Lastschrift[])
        return false;
      return super.isEnabledFor(o);
    }
  }
}

