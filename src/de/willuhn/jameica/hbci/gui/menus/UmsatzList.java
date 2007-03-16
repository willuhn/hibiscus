/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/UmsatzList.java,v $
 * $Revision: 1.25 $
 * $Date: 2007/03/16 13:14:28 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.UmsatzAssign;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzExport;
import de.willuhn.jameica.hbci.gui.action.UmsatzImport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Umsaetzen
 * angehaengt werden kann.
 */
public class UmsatzList extends ContextMenu implements Extendable
{

	private I18N i18n;
  
  private TablePart table;

  /**
   * Erzeugt ein Kontext-Menu fuer eine Liste von Umsaetzen.
   */
  public UmsatzList(TablePart table)
  {
//    this(null);
    this.table = table;
  }

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Umsaetzen.
   * @param konto optionale Angabe des Kontos.
	 */
	public UmsatzList(final Konto konto)
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new OpenItem());

		addItem(new CheckedContextMenuItem(i18n.tr("Gegenkonto in Adressbuch übernehmen"),new EmpfaengerAdd()));
    // BUGZILLA 315
    addItem(new CheckedContextMenuItem(i18n.tr("Als neue Überweisung anlegen..."),new UeberweisungNew()));
    addItem(new CheckedContextMenuItem(i18n.tr("Umsatz-Kategorie zuordnen..."),new Action() {
    
      public void handleAction(Object context) throws ApplicationException
      {
        new UmsatzAssign(table).handleAction(context);
        GUI.getCurrentView().reload();
      }
    
    }));

    // BUGZILLA #70 http://www.willuhn.de/bugzilla/show_bug.cgi?id=70
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete()));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new UmsatzExport()));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new UmsatzImport()
    {

      public void handleAction(Object context) throws ApplicationException
      {
        super.handleAction(context == null ? konto : context);
      }
      
    }
    ));
    
    // Wir geben das Context-Menu jetzt noch zur Erweiterung frei.
    ExtensionRegistry.extend(this);

	}

  /**
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class OpenItem extends CheckedContextMenuItem
  {
    private OpenItem()
    {
      super(i18n.tr("Öffnen"),new UmsatzDetail());
    }
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Umsatz[])
        return false;
      return super.isEnabledFor(o);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }
}


/**********************************************************************
 * $Log: UmsatzList.java,v $
 * Revision 1.25  2007/03/16 13:14:28  jost
 * Austausch der Tabellenzeile nach Umsattyp-Zuordnung
 *
 * Revision 1.24  2007/02/21 11:58:52  willuhn
 * @N Bug 315
 *
 * Revision 1.23  2006/11/30 23:48:40  willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 *
 * Revision 1.22  2006/10/09 23:49:39  willuhn
 * @N extendable
 *
 * Revision 1.21  2006/10/05 16:42:28  willuhn
 * @N CSV-Import/Export fuer Adressen
 *
 * Revision 1.20  2006/08/02 17:49:44  willuhn
 * @B Bug 255
 * @N Erkennung des Kontos beim Import von Umsaetzen aus dem Kontextmenu heraus
 *
 * Revision 1.19  2006/06/08 17:40:59  willuhn
 * @N Vorbereitungen fuer DTAUS-Import von Sammellastschriften und Umsaetzen
 *
 * Revision 1.18  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.17  2006/04/20 08:44:21  willuhn
 * @C s/Childs/Children/
 *
 * Revision 1.16  2006/04/04 21:57:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2006/04/04 06:47:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2006/01/23 12:16:57  willuhn
 * @N Update auf HBCI4Java 2.5.0-rc5
 *
 * Revision 1.13  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 * Revision 1.12  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.11  2005/06/07 22:41:09  web0
 * @B bug 70
 *
 * Revision 1.10  2005/06/02 22:57:34  web0
 * @N Export von Konto-Umsaetzen
 *
 * Revision 1.9  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.8  2005/05/09 12:24:20  web0
 * @N Changelog
 * @N Support fuer Mehrfachmarkierungen
 * @N Mehere Adressen en bloc aus Umsatzliste uebernehmen
 *
 * Revision 1.7  2005/04/16 13:34:01  web0
 * *** empty log message ***
 *
 * Revision 1.6  2005/03/01 22:05:13  web0
 * @B fixed help pages
 *
 * Revision 1.5  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.4  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/