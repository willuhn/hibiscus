/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.menus.UmsatzList;
import de.willuhn.jameica.hbci.messaging.NeueUmsaetze;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Liefert einen fertig konfigurierten Tree mit Umsaetzen in deren Kategorien.
 */
public class UmsatzTree extends TreePart
{
  private final static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private static Hashtable<String,Color> colorCache = new Hashtable<String,Color>();
  
  /**
   * ct.
   * @param list eine Liste mit Objekten des Typs <code>Umsatz</code>
   * @throws RemoteException
   */
  public UmsatzTree(GenericIterator list) throws RemoteException
  {
    super(list, new UmsatzDetail());
    
    try
    {
      BeanUtil.invoke(this,"addFeature",new Object[]{"de.willuhn.jameica.gui.parts.table.FeatureShortcut"});
    }
    catch (Exception e)
    {
      Logger.warn("Shortcut feature not available in this jameica version");
    }
    
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setRememberState(true);
    this.setMulti(true);
    this.setSummary(true);
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
          
          // Mal checken, ob wir eine benutzerdefinierte Farbe haben
          UmsatzTyp ut = null;
          if (i instanceof UmsatzTreeNode)
            ut = ((UmsatzTreeNode)i).getUmsatzTyp();
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

          item.setForeground(ColorUtil.getForeground(betrag.doubleValue()));

          // neue Umsaetze fett drucken, vorgemerkte grau
          if (i instanceof Umsatz)
          {
            Umsatz u = (Umsatz) i;
            if ((u.getFlags() & Umsatz.FLAG_NOTBOOKED) != 0)
              item.setForeground(de.willuhn.jameica.gui.util.Color.COMMENT.getSWTColor());

            item.setFont(NeueUmsaetze.isNew(u) ? Font.BOLD.getSWTFont() : Font.DEFAULT.getSWTFont());
          }
        }
        catch (Exception e)
        {
          Logger.error("error while formatting item",e);
        }
      }
    
    });
    this.addColumn(i18n.tr("Bezeichnung"),      "name");
    if (settings.getBoolean("usage.list.all",false))
      addColumn(i18n.tr("Verwendungszweck"),    "mergedzweck");
    else
      addColumn(i18n.tr("Verwendungszweck"),    Tag.SVWZ.name());
    this.addColumn(i18n.tr("Datum"),            "datum_pseudo", new DateFormatter(HBCI.DATEFORMAT));
    this.addColumn(i18n.tr("Betrag"),           "betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    this.addColumn(i18n.tr("Notiz"),            "kommentar");

    this.setContextMenu(new UmsatzList());
  }

  
  /**
   * @see de.willuhn.jameica.gui.parts.TreePart#setList(de.willuhn.datasource.GenericIterator)
   */
  public void setList(GenericIterator list)
  {
    try
    {
      ////////////////////////////////////////////////////////////////
      // Wir ordnen alle Umsaetze den Kategorien zu. Man koennte
      // alternativ auch die UmsatzTypen laden und sich dann von denen
      // die Umsaetze holen. Allerdings wuerde das viel mehr SQL-Queries
      // ausloesen, zum anderen koennte man dann nicht die Umsaetze
      // finden, die nirgends zugeordnet sind. Und es waere um einiges
      // schwieriger, nur die Umsaetze anzuzeigen, die den aktuellen
      // Filter-Kriterien entsprechen.

      Map<String,UmsatzTreeNode> lookup = new HashMap<String,UmsatzTreeNode>();
      lookup.put(null,new UmsatzTreeNode(null)); // Pseudo-Kategorie "Nicht zugeordnet"
      
      while (list.hasNext())
      {
        Umsatz u = (Umsatz) list.next();
        getNode(lookup,u.getUmsatzTyp()).add(u);
      }
      ////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////
      // Jetzt kopieren wir das noch in ein Array, damit wir es
      // nach Nummer sortieren koennen. Wir nehmen aber nur die Root-Elemente
      Iterator<UmsatzTreeNode> it = lookup.values().iterator();
      List<UmsatzTreeNode> items = new ArrayList<UmsatzTreeNode>();
      while (it.hasNext())
      {
        UmsatzTreeNode u = it.next();
        if (u.getParent() == null)
          items.add(u);
      }
      Collections.sort(items);
      ////////////////////////////////////////////////////////////////

      super.setList(PseudoIterator.fromArray((GenericObject[])items.toArray(new GenericObject[items.size()])));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to build tree",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Erzeugen der Baum-Ansicht: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Liefert den passenden Knoten fuer den Umsatz.
   * Fehlende Knoten werden automatisch erstellt.
   * @param lookup Lookup-Tabelle fuer bereits vorhandene Knoten.
   * @param ut die Kategorie, fuer die der Knoten erstellt werden soll.
   * @return der Knoten.
   * @throws RemoteException
   */
  private UmsatzTreeNode getNode(Map<String,UmsatzTreeNode> lookup,UmsatzTyp ut) throws RemoteException
  {
    UmsatzTreeNode node = lookup.get(ut != null ? ut.getID() : null);
    if (node != null)
      return node; // haben wir schon.
    
    // Neu anlegen
    node = new UmsatzTreeNode(ut);
    lookup.put(ut.getID(),node);
    
    // Parents checken
    UmsatzTyp parent = (UmsatzTyp) ut.getParent();
    if (parent != null)
    {
      UmsatzTreeNode np = getNode(lookup,parent);
      node.setParent(np);
      np.getSubGroups().add(node);
    }
    
    return node;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#getSummary()
   */
  protected String getSummary()
  {
    try
    {
      Object o = this.getSelection();
      if(o instanceof Umsatz){
        o =new Umsatz[]{(Umsatz) o};
      }
      int size = this.size();

      // nichts markiert oder nur einer, dann liefern wir nur die Anzahl der Umsaetze
      if (o == null || size == 1 || !(o instanceof Umsatz[]))
      {
        if (size == 1)
          return i18n.tr("1 Umsatz");
        else
          return i18n.tr("{0} Ums�tze",Integer.toString(size));
      }
      
      // Andernfalls berechnen wir die Summe
      double sum = 0.0d;
      Umsatz[] list = (Umsatz[]) o;
      String curr = null;
      for (Umsatz u:list)
      {
        if (curr == null)
          curr = u.getKonto().getWaehrung();
        sum += u.getBetrag();
      }
      if (curr == null)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

      return i18n.tr("{0} Ums�tze, {1} markiert, Summe: {2} {3}",new String[]{Integer.toString(size),
                                                                              Integer.toString(list.length),
                                                                              HBCI.DECIMALFORMAT.format(sum),
                                                                              curr});
    }
    catch (Exception e)
    {
      Logger.error("error while updating summary",e);
    }
    return "";//super.getSummary();
  }
}
