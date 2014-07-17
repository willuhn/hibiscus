/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/UmsatzList.java,v $
 * $Revision: 1.38 $
 * $Date: 2011/05/06 09:03:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.internal.action.Print;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.action.FlaggableChange;
import de.willuhn.jameica.hbci.gui.action.UmsatzAssign;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzExport;
import de.willuhn.jameica.hbci.gui.action.UmsatzImport;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.io.print.PrintSupportUmsatzList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Umsaetzen
 * angehaengt werden kann.
 */
public class UmsatzList extends ContextMenu implements Extendable
{

	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erzeugt ein Kontext-Menu fuer eine Liste von Umsaetzen.
   */
  public UmsatzList()
  {
    this(null);
  }

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Umsaetzen.
   * @param konto optionale Angabe des Kontos.
	 */
	public UmsatzList(final Konto konto)
	{
		addItem(new OpenItem());
    addItem(new UmsatzItem(i18n.tr("Löschen..."), new DBObjectDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new UmsatzItem(i18n.tr("In Adressbuch übernehmen"),new EmpfaengerAdd(),"contact-new.png"));
    addItem(new UmsatzItem(i18n.tr("Als neue Überweisung anlegen..."),new AuslandsUeberweisungNew(),"stock_next.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new UmsatzBookedItem(i18n.tr("als \"geprüft\" markieren..."),new FlaggableChange(Umsatz.FLAG_CHECKED,true),"emblem-default.png","ALT+G"));
    addItem(new UmsatzBookedItem(i18n.tr("als \"ungeprüft\" markieren..."),new FlaggableChange(Umsatz.FLAG_CHECKED,false),"edit-undo.png","CTRL+ALT+G"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new UmsatzItem(i18n.tr("Drucken..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(new PrintSupportUmsatzList(context));
      }
    },"document-print.png"));
    addItem(new UmsatzItem(i18n.tr("Exportieren..."),new UmsatzExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new UmsatzImport()
    {

      public void handleAction(Object context) throws ApplicationException
      {
        super.handleAction(konto != null ? konto : context);
      }
      
    }
    ,"document-open.png"));
    
    // BUGZILLA 512 / 1115
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new UmsatzBookedItem(i18n.tr("Kategorie zuordnen..."),new UmsatzAssign(),"x-office-spreadsheet.png","ALT+K"));
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Kategorie bearbeiten..."),new UmsatzTypNew(),"document-open.png")
    {
      public boolean isEnabledFor(Object o)
      {
        // Wen es ein Umsatz ist, dann nur aktivieren, wenn der Umsatz eine Kategorie hat
        if (o instanceof Umsatz)
        {
          try
          {
            return ((Umsatz)o).getUmsatzTyp() != null;
          }
          catch (RemoteException re)
          {
            Logger.error("unable to check umsatztyp",re);
          }
        }
        
        // Ansonsten wie gehabt
        return super.isEnabledFor(o);
      }
      
    });
    addItem(new ContextMenuItem(i18n.tr("Neue Kategorie anlegen..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        // BUGZILLA 926
        UmsatzTyp ut = null;
        if (context != null && (context instanceof Umsatz))
        {
          try
          {
            Umsatz u = (Umsatz) context;
            ut = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
            ut.setName(u.getGegenkontoName());
            ut.setPattern(u.getZweck());
          }
          catch (Exception e)
          {
            Logger.error("error while preparing category",e);
          }
        }
        new UmsatzTypNew().handleAction(ut);
      }
    },"text-x-generic.png"));

    // Wir geben das Context-Menu jetzt noch zur Erweiterung frei.
    ExtensionRegistry.extend(this);

	}
	
  /**
   * Pruefen, ob es sich wirklich um einen Umsatz handelt.
   */
  private class UmsatzItem extends CheckedContextMenuItem
  {
    /**
     * ct.
     * @param text Label.
     * @param action Action.
     * @param icon optionales Icon.
     */
    public UmsatzItem(String text, Action action, String icon)
    {
      super(text,action,icon);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.CheckedContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if ((o instanceof Umsatz) || (o instanceof Umsatz[]))
        return super.isEnabledFor(o);
      return false;
    }
    
  }

  /**
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class OpenItem extends UmsatzItem
  {
    private OpenItem()
    {
      super(i18n.tr("Öffnen"),new UmsatzDetail(),"document-open.png");
    }
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Umsatz)
        return super.isEnabledFor(o);
      return false;
    }
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }

  /**
   * Ueberschrieben, um nur fuer gebuchte Umsaetze zu aktivieren
   */
  private class UmsatzBookedItem extends UmsatzItem
  {
    /**
     * ct.
     * @param text Label.
     * @param action Action.
     * @param icon optionales Icon.
     * @param shortcut Shortcut.
     */
    public UmsatzBookedItem(String text, Action action, String icon, String shortcut)
    {
      super(text,action,icon);
      this.setShortcut(shortcut);
    }
    
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if ((o instanceof Umsatz) || (o instanceof Umsatz[]))
      {
        Umsatz[] umsaetze = null;
        
        if (o instanceof Umsatz)
          umsaetze = new Umsatz[]{(Umsatz) o};
        else
          umsaetze = (Umsatz[]) o;

        try
        {
          for (int i=0;i<umsaetze.length;++i)
          {
            if ((umsaetze[i].getFlags() & Umsatz.FLAG_NOTBOOKED) != 0)
              return false;
          }
        }
        catch (RemoteException re)
        {
          Logger.error("unable to check for not-booked entries",re);
        }
        return super.isEnabledFor(o);
      }
      return false;
    }
  }
  

}


/**********************************************************************
 * $Log: UmsatzList.java,v $
 * Revision 1.38  2011/05/06 09:03:54  willuhn
 * @C Labels geaendert
 *
 * Revision 1.37  2011-04-13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.36  2011-04-13 08:48:01  willuhn
 * @N Loeschen von Vormerkbuchungen zulassen
 *
 * Revision 1.35  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 * Revision 1.34  2009/09/15 00:23:35  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.33  2009/02/24 22:42:33  willuhn
 * @N Da vorgemerkte Umsaetze jetzt komplett geloescht werden, wenn sie neu abgerufen werden, duerfen sie auch nicht mehr geaendert werden (also auch keine Kategorie und kein Kommentar)
 *
 * Revision 1.32  2009/02/12 18:37:18  willuhn
 * @N Erster Code fuer vorgemerkte Umsaetze
 *
 * Revision 1.31  2009/02/04 23:06:24  willuhn
 * @N BUGZILLA 308 - Umsaetze als "geprueft" markieren
 *
 * Revision 1.30  2008/12/19 12:16:05  willuhn
 * @N Mehr Icons
 * @C Reihenfolge der Contextmenu-Eintraege vereinheitlicht
 *
 * Revision 1.29  2007/12/04 23:59:00  willuhn
 * @N Bug 512
 *
 * Revision 1.28  2007/03/22 22:36:42  willuhn
 * @N Contextmenu in Trees
 * @C Kategorie-Baum in separates TreePart ausgelagert
 *
 * Revision 1.27  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
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