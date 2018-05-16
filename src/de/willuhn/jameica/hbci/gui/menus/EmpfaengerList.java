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
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerExport;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerImport;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.gui.action.SepaConvertAddress;
import de.willuhn.jameica.hbci.gui.action.SepaDauerauftragNew;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftNew;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Empfaenger-Adressen
 * angehaengt werden kann.
 */
public class EmpfaengerList extends ContextMenu implements Extendable
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	/**
	 * Erzeugt das Kontext-Menu fuer eine Liste von Empfaengern.
	 */
	public EmpfaengerList()
	{
		addItem(new CheckedSingleContextMenuItem(i18n.tr("Öffnen"),new EmpfaengerNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neue Adresse..."), new ENeu(),"contact-new.png"));
    addItem(new CheckedHibiscusAddressContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Neue Überweisung..."),new AuslandsUeberweisungNew(),"ueberweisung.png"));
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Neue Lastschrift..."),new SepaLastschriftNew(),"lastschrift.png"));
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Neuer Dauerauftrag..."),new SepaDauerauftragNew(),"dauerauftrag.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new EmpfaengerExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new EmpfaengerImport(),"document-open.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedHibiscusAddressContextMenuItem(i18n.tr("Nach SEPA konvertieren..."), new SepaConvertAddress(),"internet-web-browser.png"));
    
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
   * Ueberschrieben, um nur "echte" Hibiscus-Adressen aus der DB zuzulassen.
   */
  private class CheckedHibiscusAddressContextMenuItem extends CheckedContextMenuItem
  {
    /**
     * @param text Anzuzeigender Text.
     * @param action Aktion.
     * @param icon optionales Icon.
     */
    private CheckedHibiscusAddressContextMenuItem(String text, Action action, String icon)
    {
      super(text,action,icon);
    }
    
    /**
     * @see de.willuhn.jameica.gui.parts.CheckedContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      // erstmal checken, ob ueberhaupt was ausgewaehlt wurde
      if (!super.isEnabledFor(o))
        return false;
      
      // Einzelner Datensatz?
      if (o instanceof HibiscusAddress)
        return true;
      
      // Liste von Datensaetzen?
      if ((Address[].class.isAssignableFrom(o.getClass())))
      {
        // Checken, ob wirklich nur Datensaetze aus der Hibiscus-Datenbank drin stehen
        // und keine "virtuellen"
        Address[] list = (Address[]) o;
        for (Address a:list)
        {
          if (!(a instanceof HibiscusAddress))
            return false;
        }
        
        return true; // Sieht gut aus
      }
      
      // nichts von dem
      return false;
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
 * Revision 1.21  2011/09/27 16:39:10  willuhn
 * @B XML-Export von Adressen funktionierte nicht mehr
 *
 * Revision 1.20  2008/12/19 12:16:05  willuhn
 * @N Mehr Icons
 * @C Reihenfolge der Contextmenu-Eintraege vereinheitlicht
 *
 * Revision 1.19  2008/12/19 01:12:09  willuhn
 * @N Icons in Contextmenus
 *
 * Revision 1.18  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 **********************************************************************/