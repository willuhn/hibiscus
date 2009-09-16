/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzTypTree.java,v $
 * $Revision: 1.10 $
 * $Date: 2009/09/16 22:34:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.menus.UmsatzList;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzGroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert einen fertig konfigurierten Tree mit Umsaetzen in deren Kategorien.
 */
public class UmsatzTypTree extends TreePart implements Extension
{
  private static boolean registered = false;
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private static Hashtable<String,Color> colorCache = new Hashtable<String,Color>();

  /**
   * ct.
   * @param list eine Liste mit Objekten des Typs <code>Umsatz</code>
   * @throws RemoteException
   */
  public UmsatzTypTree(GenericIterator list) throws RemoteException
  {
    super(init(list), new UmsatzDetail());
    
    // BUGZILLA 512
    if (!registered)
    {
      ExtensionRegistry.register(this,UmsatzList.class.getName());
      registered = true;
    }
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setMulti(true);
    this.setContextMenu(new UmsatzList());
    this.setFormatter(new TreeFormatter() {
    
      public void format(TreeItem item)
      {
        if (item == null || item.getData() == null)
          return;
        try
        {
          GenericObject i = (GenericObject) item.getData();
          Object value = i.getAttribute("betrag");
          if (value == null || !(value instanceof Double))
            return;
          Double betrag = (Double) value;
          if (betrag == null)
            return;
          
          // Mal checken, ob wir eine benutzerdefinierte Farbe haben
          UmsatzTyp ut = null;
          if (i instanceof UmsatzGroup)
            ut = ((UmsatzGroup)i).getUmsatzTyp();
          else if (i instanceof Umsatz)
            ut = ((Umsatz)i).getUmsatzTyp();
          
          if (ut != null)
          {
            if (ut.isCustomColor())
            {
              int[] color = ut.getColor();
              if (color != null && color.length == 3)
              {
                RGB rgb = new RGB(color[0],color[1],color[2]);
                Color c = colorCache.get(rgb.toString());
                if (c == null)
                {
                  c = new Color(GUI.getDisplay(),rgb);
                  colorCache.put(rgb.toString(),c);
                }
                item.setForeground(c);
                return;
              }
            }
          }
          
          if (betrag.doubleValue() < 0)
            item.setForeground(Settings.getBuchungSollForeground());
          else
            item.setForeground(Settings.getBuchungHabenForeground());
        }
        catch (Exception e)
        {
          Logger.error("error while formatting item",e);
        }
      }
    
    });
    this.addColumn(i18n.tr("Bezeichnung"),      "name");
    this.addColumn(i18n.tr("Verwendungszweck"), "zweck");
    this.addColumn(i18n.tr("Datum"),            "datum_pseudo", new DateFormatter(HBCI.DATEFORMAT));
    this.addColumn(i18n.tr("Betrag"),           "betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
  }

  /**
   * @param list Liste der Umsaetze.
   * @return kombinierter Tree aus Kategorien und Umsaetzen.
   * @throws RemoteException
   */
  private static GenericIterator init(GenericIterator list) throws RemoteException
  {
    ////////////////////////////////////////////////////////////////
    // Wir ordnen alle Umsaetze den Kategorien zu. Man koennte
    // alternativ auch die UmsatzTypen laden und sich dann von denen
    // die Umsaetze holen. Allerdings wuerde das viel mehr SQL-Queries
    // ausloesen und zum anderen koennte man dann nicht die Umsaetze
    // finden, die nirgends zugeordnet sind.

    final Object placeholder = new Object();
    HashMap lookup = new HashMap();
    lookup.put(placeholder,new UmsatzGroup(null)); // Pseudo-Kategorie "Nicht zugeordnet"
    
    while (list.hasNext())
    {
      Umsatz u        = (Umsatz) list.next();
      UmsatzTyp ut    = u.getUmsatzTyp();
      UmsatzGroup kat = (UmsatzGroup) lookup.get(ut == null ? placeholder : ut);
      if (kat == null)
      {
        kat = new UmsatzGroup(ut); // haben wir noch nicht. Also neu anlegen
        lookup.put(ut,kat);
      }
      kat.add(u);
    }
    ////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////
    // Jetzt kopieren wir das noch in ein Array, damit wir es
    // nach Nummer sortieren koennen
    List items = Arrays.asList(lookup.values().toArray());
    Collections.sort(items);
    ////////////////////////////////////////////////////////////////

    return PseudoIterator.fromArray((GenericObject[])items.toArray(new GenericObject[items.size()]));
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  public void extend(Extendable extendable)
  {
    if (extendable == null || !(extendable instanceof UmsatzList))
      return;
    UmsatzList l = (UmsatzList) extendable;
    l.addItem(ContextMenuItem.SEPARATOR);
    l.addItem(new GroupItem(i18n.tr("Kategorie bearbeiten..."),new UmsatzTypNew()));
    l.addItem(new ContextMenuItem(i18n.tr("Neue Kategorie anlegen..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        new UmsatzTypNew().handleAction(null);
      }
    }));
  }
  
  /**
   * Menu-Item fuer Umsatzgruppen.
   */
  private class GroupItem extends CheckedContextMenuItem
  {
    /**
     * ct.
     * @param name
     * @param action
     */
    private GroupItem(String name, Action action)
    {
      super(name,action);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.CheckedContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o != null && (o instanceof UmsatzGroup))
        return super.isEnabledFor(o);
      return false;
    }
  }
}

/*******************************************************************************
 * $Log: UmsatzTypTree.java,v $
 * Revision 1.10  2009/09/16 22:34:32  willuhn
 * @B im Umsatz-Tree wurde nicht geprueft, ob die benutzerdefinierte Farbe aktiviert ist
 *
 * Revision 1.9  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.8  2008/12/04 22:03:34  willuhn
 * @N BUGZILLA 665
 *
 * Revision 1.7  2007/12/04 23:59:00  willuhn
 * @N Bug 512
 *
 * Revision 1.6  2007/11/01 21:07:27  willuhn
 * @N Spalten von Tabellen und mehrspaltigen Trees koennen mit mit Drag&Drop umsortiert werden. Die Sortier-Reihenfolge wird automatisch gespeichert und wiederhergestellt
 *
 * Revision 1.5  2007/08/28 09:47:09  willuhn
 * @N Bug 395
 *
 * Revision 1.4  2007/08/12 22:02:10  willuhn
 * @C BUGZILLA 394 - restliche Umstellungen von Valuta auf Buchungsdatum
 *
 * Revision 1.3  2007/04/20 09:50:11  willuhn
 * @B use placeholder as key instead of null
 *
 * Revision 1.2  2007/04/18 08:54:21  willuhn
 * @N UmsatzGroup to fetch items from UmsatzTypTree
 *
 * Revision 1.1  2007/03/22 22:36:42  willuhn
 * @N Contextmenu in Trees
 * @C Kategorie-Baum in separates TreePart ausgelagert
 *
 * Revision 1.6  2007/03/22 14:23:56  willuhn
 * @N Redesign Kategorie-Tree - ist jetzt erheblich schneller und enthaelt eine Pseudo-Kategorie "Nicht zugeordnet"
 *
 * Revision 1.5  2007/03/21 18:47:36  willuhn
 * @N Neue Spalte in Kategorie-Tree
 * @N Sortierung des Kontoauszuges wie in Tabelle angezeigt
 * @C Code cleanup
 *
 * Revision 1.4  2007/03/10 07:16:37  jost
 * Neu: Nummer für die Sortierung der Umsatz-Kategorien
 *
 * Revision 1.3  2007/03/08 18:56:39  willuhn
 * @N Mehrere Spalten in Kategorie-Baum
 *
 * Revision 1.2  2007/03/07 10:29:41  willuhn
 * @B rmi compile fix
 * @B swt refresh behaviour
 *
 * Revision 1.1  2007/03/06 20:06:08  jost
 * Neu: Umsatz-Kategorien-Übersicht
 *
 ******************************************************************************/
