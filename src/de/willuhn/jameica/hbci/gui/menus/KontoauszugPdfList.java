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

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoFetchKontoauszug;
import de.willuhn.jameica.hbci.gui.action.KontoKontoauszugReceipt;
import de.willuhn.jameica.hbci.gui.action.KontoauszugDelete;
import de.willuhn.jameica.hbci.gui.action.KontoauszugDetail;
import de.willuhn.jameica.hbci.gui.action.KontoauszugExport;
import de.willuhn.jameica.hbci.gui.action.KontoauszugImport;
import de.willuhn.jameica.hbci.gui.action.KontoauszugMarkRead;
import de.willuhn.jameica.hbci.gui.action.KontoauszugMarkUnread;
import de.willuhn.jameica.hbci.gui.action.KontoauszugMove;
import de.willuhn.jameica.hbci.gui.action.KontoauszugNew;
import de.willuhn.jameica.hbci.gui.action.KontoauszugOpen;
import de.willuhn.jameica.hbci.gui.action.KontoauszugSave;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit den Kontoauszuegen im PDF-Format gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class KontoauszugPdfList extends ContextMenu
{
	private final static I18N i18n	= Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von SEPA-Dauerauftraegen.
	 */
	public KontoauszugPdfList()
	{
		addItem(new CheckedSingleContextMenuItem(i18n.tr("Öffnen"),              new KontoauszugOpen(),"application-pdf.png"));
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Bearbeiten"),          new KontoauszugDetail(),"document-open.png"));
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Speichern unter..."),  new KontoauszugSave(),"document-save.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."),                new KontoauszugDelete(),"user-trash-full.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Dateien verschieben..."),    new KontoauszugMove(),"edit-copy.png"));
		addItem(ContextMenuItem.SEPARATOR);
    addItem(new SCCheckedContextMenuItem(i18n.tr("Als gelesen markieren"),   new KontoauszugMarkRead(),"emblem-default.png","ALT+G"));
    addItem(new SCCheckedContextMenuItem(i18n.tr("Als ungelesen markieren"), new KontoauszugMarkUnread(),"edit-undo.png","CTRL+ALT+G"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new ContextMenuItem(i18n.tr("Kontoauszüge abrufen..."),          new KontoFetchKontoauszug(),"mail-send-receive.png"));
    addItem(new UnsentCheckedContextMenuItem(i18n.tr("Empfangsquittung senden..."), new KontoKontoauszugReceipt(),"mail-forward.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new ContextMenuItem(i18n.tr("Kontoauszug manuell anlegen..."),   new KontoauszugNew(),"document-new.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),            new KontoauszugExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),                   new KontoauszugImport(),"document-open.png"));
	}
	
	/**
	 * Nimmt den Shortcut noch mit im Construktor auf.
	 */
	private class SCCheckedContextMenuItem extends CheckedContextMenuItem
	{
    /**
     * ct.
     * @param text
     * @param a
     * @param icon
     * @param shortcut
     */
    public SCCheckedContextMenuItem(String text, Action a, String icon, String shortcut)
    {
      super(text, a, icon);
      this.setShortcut(shortcut);
    }
	  
	}
	
	private class UnsentCheckedContextMenuItem extends CheckedSingleContextMenuItem
	{
	  /**
     * ct.
     * @param text
     * @param a
     * @param icon
     */
    public UnsentCheckedContextMenuItem(String text, Action a, String icon)
    {
      super(text, a, icon);
    }

	  @Override
	  public boolean isEnabledFor(Object o)
	  {
	    if (!(o instanceof Kontoauszug))
	      return false;
	    
	    try
	    {
	      Kontoauszug k = (Kontoauszug) o;
	      return k.getQuittungscode() != null && k.getQuittiertAm() == null && super.isEnabledFor(o);
	    }
	    catch (RemoteException re)
	    {
	      Logger.error("unable to check state",re);
	      return false;
	    }
	  }
	}
}
