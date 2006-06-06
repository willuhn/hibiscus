/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/EmpfaengerList.java,v $
 * $Revision: 1.16 $
 * $Date: 2006/06/06 22:41:26 $
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
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Empfaenger-Adressen
 * angehaengt werden kann.
 */
public class EmpfaengerList extends ContextMenu implements Extendable
{

	private I18N i18n;

	/**
	 * Erzeugt das Kontext-Menu fuer eine Liste von Empfaengern.
	 */
	public EmpfaengerList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new SingleItem(i18n.tr("Öffnen"),new EmpfaengerNew()));
		addItem(new SingleItem(i18n.tr("Neue Überweisung mit diesem Empfänger..."), new UeberweisungNew()));
		addItem(new SingleItem(i18n.tr("Neue Lastschrift von diesem Konto einziehen..."), new LastschriftNew()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete()));
    addItem(ContextMenuItem.SEPARATOR);
		addItem(new ContextMenuItem(i18n.tr("Neue Adresse..."), new ENeu()));
    
    // Wir geben das Context-Menu jetzt noch zur Erweiterung frei.
    ExtensionRegistry.extend(this);
	}

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  } 

  /**
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class SingleItem extends CheckedContextMenuItem
  {
    /**
     * @param text
     * @param action
     */
    private SingleItem(String text, Action action)
    {
      super(text,action);
    }
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Adresse[])
        return false;
      return super.isEnabledFor(o);
    }
  }

  /**
   * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Adresse
   * anzulegen - auch wenn der Focus auf einem existierenden liegt.
   */
  private class ENeu extends EmpfaengerNew
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
 * $Log: EmpfaengerList.java,v $
 * Revision 1.16  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.15  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.14  2005/05/30 12:01:03  web0
 * @R removed OP stuff
 *
 * Revision 1.13  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 * Revision 1.12  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 * Revision 1.11  2005/05/09 15:02:12  web0
 * @N mehrere Adressen gleichzeitig loeschen
 *
 * Revision 1.10  2005/05/09 12:24:20  web0
 * @N Changelog
 * @N Support fuer Mehrfachmarkierungen
 * @N Mehere Adressen en bloc aus Umsatzliste uebernehmen
 *
 * Revision 1.9  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.8  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.7  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.6  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.5  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.4  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.3  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/