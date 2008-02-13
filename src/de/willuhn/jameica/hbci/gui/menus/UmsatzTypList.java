/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/UmsatzTypList.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/02/13 23:44:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypExport;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypImport;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Umsatz-Kategorien
 * angehaengt werden kann.
 */
public class UmsatzTypList extends ContextMenu implements Extendable
{

	private I18N i18n;

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Umsaetzen.
	 */
	public UmsatzTypList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new OpenItem());
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete()));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new ContextMenuItem(i18n.tr("Neue Umsatz-Kategorie..."), new UNeu()));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new UmsatzTypExport()));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new UmsatzTypImport()));
    // Wir geben das Context-Menu jetzt noch zur Erweiterung frei.
    ExtensionRegistry.extend(this);

	}

  /**
   * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Umsatzkategorie
   * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class UNeu extends UmsatzTypNew
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
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class OpenItem extends CheckedContextMenuItem
  {
    private OpenItem()
    {
      super(i18n.tr("Öffnen"),new UmsatzTypNew());
    }

    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof UmsatzTyp[])
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
 * $Log: UmsatzTypList.java,v $
 * Revision 1.3  2008/02/13 23:44:27  willuhn
 * @R Hibiscus-Eigenformat (binaer-serialisierte Objekte) bei Export und Import abgeklemmt
 * @N Import und Export von Umsatz-Kategorien im XML-Format
 * @B Verzaehler bei XML-Import
 *
 * Revision 1.2  2007/04/26 12:20:42  willuhn
 * @N Menu-Eintrag "Neue Umsatz-Kategorie..."
 *
 * Revision 1.1  2006/11/23 17:25:38  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 **********************************************************************/