/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/KontoList.java,v $
 * $Revision: 1.12 $
 * $Date: 2006/10/09 16:56:07 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.gui.action.KontoDelete;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.action.KontoFetchSaldo;
import de.willuhn.jameica.hbci.gui.action.KontoResetAuszugsdatum;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.UmsatzList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Konto-Listen angehaengt
 * werden kann.
 */
public class KontoList extends ContextMenu implements Extendable
{

  private I18N i18n;

  /**
   * Erzeugt ein Kontext-Menu fuer eine Liste von Konten.
   */
  public KontoList()
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources()
        .getI18N();

    addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"), new KontoNew()));
    addItem(new ContextMenuItem(i18n.tr("Neues Konto..."), new KNeu()));
    addItem(new CheckedContextMenuItem(i18n.tr("Kontoauszüge anzeigen..."),
        new UmsatzList()));
    addItem(new CheckedContextMenuItem(i18n.tr("Saldo aktualisieren..."),
        new KontoFetchSaldo()));
    addItem(new CheckedContextMenuItem(i18n
        .tr("Kontoauszugsdatum zurücksetzen"), new KontoResetAuszugsdatum()));

    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new KontoDelete()));
    addItem(ContextMenuItem.SEPARATOR);

    addItem(new ContextMenuItem(i18n.tr("Neue Überweisung..."),
        new UeberweisungNew()));
    addItem(new ContextMenuItem(i18n.tr("Neuer Dauerauftrag..."),
        new DauerauftragNew()));
    addItem(new ContextMenuItem(i18n.tr("Neue Lastschrift..."),
        new LastschriftNew()));
  }

  /**
   * Ueberschreiben wir, um <b>grundsaetzlich</b> ein neues Konto anzulegen -
   * auch wenn der Focus auf einem existierenden liegt.
   */
  private class KNeu extends KontoNew
  {
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      super.handleAction(null);
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

/*******************************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.12  2006/10/09 16:56:07  jost
 * Bug #284
 * Revision 1.11 2005/08/01 23:27:42 web0 *** empty log
 * message ***
 * 
 * Revision 1.10 2005/06/03 17:14:20 web0
 * 
 * @B NPE
 * 
 * Revision 1.9 2005/01/19 00:16:04 willuhn
 * @N Lastschriften
 * 
 * Revision 1.8 2004/11/13 17:02:04 willuhn
 * @N Bearbeiten des Zahlungsturnus
 * 
 * Revision 1.7 2004/10/25 17:58:56 willuhn
 * @N Haufen Dauerauftrags-Code
 * 
 * Revision 1.6 2004/10/20 12:08:18 willuhn
 * @C MVC-Refactoring (new Controllers)
 * 
 * Revision 1.5 2004/10/18 23:38:17 willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 * 
 * Revision 1.4 2004/08/18 23:13:51 willuhn
 * @D Javadoc
 * 
 * Revision 1.3 2004/07/25 17:15:06 willuhn
 * @C PluginLoader is no longer static
 * 
 * Revision 1.2 2004/07/21 23:54:31 willuhn *** empty log message ***
 * 
 * Revision 1.1 2004/07/20 21:48:00 willuhn
 * @N ContextMenus
 * 
 ******************************************************************************/
