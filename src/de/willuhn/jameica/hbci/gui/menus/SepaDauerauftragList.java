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
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.action.KontoFetchSepaDauerauftraege;
import de.willuhn.jameica.hbci.gui.action.SepaDauerauftragDelete;
import de.willuhn.jameica.hbci.gui.action.SepaDauerauftragNew;
import de.willuhn.jameica.hbci.io.print.PrintSupportSepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit SEPA-Dauerauftraegen gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class SepaDauerauftragList extends ContextMenu
{
	private final static I18N i18n	= Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von SEPA-Dauerauftraegen.
	 */
	public SepaDauerauftragList()
	{
		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"),            new SepaDauerauftragNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neuer Dauerauftrag..."),    new DNeu(),"text-x-generic.png"));
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."),        new SepaDauerauftragDelete(),"user-trash-full.png"));
		addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Duplizieren..."), new Duplicate(),"edit-copy.png"));
    addItem(ContextMenuItem.SEPARATOR);
		addItem(new ContextMenuItem(i18n.tr("Daueraufträge abrufen..."), new KontoFetchSepaDauerauftraege(),"mail-send-receive.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Drucken..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(new PrintSupportSepaDauerauftrag((SepaDauerauftrag) context));
      }
    },"document-print.png"));
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> einen neuen SEPA-Dauerauftrag
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class DNeu extends SepaDauerauftragNew
	{
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
    	super.handleAction(null);
    }
	}
}
