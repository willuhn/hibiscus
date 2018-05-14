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
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.NachrichtCopy;
import de.willuhn.jameica.hbci.gui.action.NachrichtMarkRead;
import de.willuhn.jameica.hbci.gui.action.NachrichtOpen;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von System-Nachrichten
 * angehaengt werden kann.
 */
public class NachrichtList extends ContextMenu
{

	private I18N i18n;

	/**
	 * Erzeugt das Kontext-Menu fuer eine Liste von Nachrichten.
	 */
	public NachrichtList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    addItem(new SingleItem(i18n.tr("Öffnen"), new NachrichtOpen(),"document-open.png"));
    addItem(new SingleItem(i18n.tr("In Zwischenablage kopieren"), new NachrichtCopy(),"edit-copy.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Als gelesen markieren"), new NachrichtMarkRead(),"emblem-default.png"));
    addItem(ContextMenuItem.SEPARATOR);
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete(),"user-trash-full.png"));
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
      if (o instanceof Nachricht[])
        return false;
      return super.isEnabledFor(o);
    }
  }

}


/**********************************************************************
 * $Log: NachrichtList.java,v $
 * Revision 1.5  2009/07/17 08:42:57  willuhn
 * @N Detail-Ansicht fuer Systemnachrichten der Bank
 * @N Systemnachrichten in Zwischenablage kopieren
 *
 * Revision 1.4  2008/12/19 12:16:05  willuhn
 * @N Mehr Icons
 * @C Reihenfolge der Contextmenu-Eintraege vereinheitlicht
 *
 * Revision 1.3  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.2  2005/06/03 17:14:20  web0
 * @B NPE
 *
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/