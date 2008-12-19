/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/EmpfaengerList.java,v $
 * $Revision: 1.19 $
 * $Date: 2008/12/19 01:12:09 $
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
import de.willuhn.jameica.hbci.gui.action.EmpfaengerExport;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerImport;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.rmi.Address;
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

		addItem(new SingleItem(i18n.tr("Öffnen"),new EmpfaengerNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neue Adresse..."), new ENeu(),"contact-new.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new SingleItem(i18n.tr("Neue Überweisung mit diesem Empfänger..."), new UeberweisungNew(),"stock_next.png"));
    addItem(new SingleItem(i18n.tr("Neue Lastschrift von diesem Konto einziehen..."), new LastschriftNew(),"stock_previous.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new EmpfaengerExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new EmpfaengerImport(),"document-open.png"));
    
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
     * @param text Anzuzeigender Text.
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
      return (o instanceof Address) && super.isEnabledFor(o);
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
 * Revision 1.19  2008/12/19 01:12:09  willuhn
 * @N Icons in Contextmenus
 *
 * Revision 1.18  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 **********************************************************************/