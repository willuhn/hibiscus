/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzTypTree.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/03/22 22:36:42 $
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.menus.UmsatzList;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Liefert einen fertig konfigurierten Tree mit Umsaetzen in deren Kategorien.
 */
public class UmsatzTypTree extends TreePart
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @param list eine Liste mit Objekten des Typs <code>Umsatz</code>
   * @throws RemoteException
   */
  public UmsatzTypTree(GenericIterator list) throws RemoteException
  {
    super(init(list), new UmsatzDetail());

    this.setRememberColWidths(true);
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
    this.addColumn(i18n.tr("Valuta"),           "valuta", new DateFormatter(HBCI.DATEFORMAT));
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

    HashMap lookup = new HashMap();
    lookup.put(null,new Kategorie(null)); // Pseudo-Kategorie "Nicht zugeordnet"
    
    while (list.hasNext())
    {
      Umsatz u      = (Umsatz) list.next();
      UmsatzTyp ut  = u.getUmsatzTyp();
      Kategorie kat = (Kategorie) lookup.get(ut);
      if (kat == null)
      {
        kat = new Kategorie(ut); // haben wir noch nicht. Also neu anlegen
        lookup.put(ut,kat);
      }
      kat.umsaetze.add(u);
    }
    ////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////
    // Jetzt kopieren wir das noch in ein Array, damit wir es
    // nach Nummer sortieren koennen
    ArrayList sort = new ArrayList();
    Iterator it = lookup.keySet().iterator();
    while (it.hasNext())
      sort.add(lookup.get(it.next()));
    Collections.sort(sort);
    ////////////////////////////////////////////////////////////////

    return PseudoIterator.fromArray((GenericObject[])sort.toArray(new GenericObject[sort.size()]));
  }

  /**
   * Hilfsklasse, um eine Umsatzkategorie als GenericObjectNode abzubilden.
   */
  private static class Kategorie implements GenericObjectNode, Comparable
  {
    private UmsatzTyp typ = null;
    private ArrayList umsaetze = new ArrayList();
    
    /**
     * ct.
     * @param typ
     */
    private Kategorie(UmsatzTyp typ)
    {
      this.typ = typ;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getChildren()
     */
    public GenericIterator getChildren() throws RemoteException
    {
      return PseudoIterator.fromArray((GenericObject[])umsaetze.toArray(new GenericObject[umsaetze.size()]));
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getParent()
     */
    public GenericObjectNode getParent() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPath()
     */
    public GenericIterator getPath() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
     */
    public GenericIterator getPossibleParents() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
     */
    public boolean hasChild(GenericObjectNode node) throws RemoteException
    {
      for (int i=0;i<this.umsaetze.size();++i)
      {
        GenericObject o = (GenericObject) this.umsaetze.get(i);
        if (o.equals(node))
          return true;
      }
      return false;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || !(other instanceof Kategorie))
        return false;
      return this.getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      if (this.typ == null && "name".equalsIgnoreCase(arg0))
        return  i18n.tr("Nicht zugeordnet");
     
      if ("betrag".equalsIgnoreCase(arg0))
      {
        // Rechnen wir manuell zusammen, damit der vom User eingegebene Datumsbereich
        // uebereinstimmt. Wir koennten zwar auch typ.getUmsatz(Date,Date) aufrufen,
        // allerdings wuerde das intern nochmal alle Umsaetze laden und haufen
        // zusaetzliche SQL-Queries ausloesen
        double betrag = 0.0d;
        for (int i=0;i<this.umsaetze.size();++i)
        {
          Umsatz u = (Umsatz) this.umsaetze.get(i);
          betrag+= u.getBetrag();
        }
        return new Double(betrag);
      }
      
      return this.typ == null ? null : this.typ.getAttribute(arg0);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return this.typ == null ? new String[]{"name"} : this.typ.getAttributeNames();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.typ == null ? "<unassigned>" : this.typ.getID();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return this.typ == null ? "name" : this.typ.getPrimaryAttribute();
    }

    /**
     * Implementiert, damit wir nach dem Feld "nummer" sortieren koennen.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
      if (o == null || !(o instanceof Kategorie))
        return -1;
      
      if (this.typ == null)
        return -1; // Wir sind "Nicht zugeordnet" - und die steht immer oben
      
      Kategorie other = (Kategorie) o;
      if (other.typ == null)
        return 1; // Die sind "Nicht zugeordnet" - wir ordnen uns unter
      
      try
      {
        String n1 = this.typ.getNummer();
        String n2 = other.typ.getNummer();

        if (n1 == null)
          return -1;
        if (n2 == null)
          return -1;
        return n1.compareTo(n2);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determine umsatztyp number",re);
      }
      return 0;
      
    }
    
  }
}

/*******************************************************************************
 * $Log: UmsatzTypTree.java,v $
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
