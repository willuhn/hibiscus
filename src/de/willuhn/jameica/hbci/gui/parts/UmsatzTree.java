/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.gui.parts.table.Feature.Event;
import de.willuhn.jameica.gui.parts.table.FeatureShortcut;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.menus.UmsatzList;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
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
  
  private AtomicInteger umsatzCount;
  private AtomicInteger groupCount;
  
  /**
   * ct.
   * @param list eine Liste mit Objekten des Typs <code>Umsatz</code>
   * @throws RemoteException
   */
  public UmsatzTree(GenericIterator list) throws RemoteException
  {
    super(list, new UmsatzDetail());
    this.addFeature(new FeatureShortcut());
    this.addFeature(new FeatureSummary());
    
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setRememberState(true);
    this.setMulti(true);
    
    final boolean bold = Settings.getBoldValues();
    
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
          
          if (bold)
            item.setFont(3,Font.BOLD.getSWTFont());

          // neue Umsaetze fett drucken, vorgemerkte grau
          if (i instanceof Umsatz)
          {
            Umsatz u = (Umsatz) i;
            if (u.hasFlag(Umsatz.FLAG_NOTBOOKED))
              item.setForeground(de.willuhn.jameica.gui.util.Color.COMMENT.getSWTColor());

            item.setFont(NeueUmsaetze.isNew(u) ? Font.BOLD.getSWTFont() : Font.DEFAULT.getSWTFont());
          }

          // Mal checken, ob wir eine benutzerdefinierte Farbe haben
          UmsatzTyp ut = null;
          if (i instanceof UmsatzTreeNode)
            ut = ((UmsatzTreeNode)i).getUmsatzTyp();
          
          ColorUtil.setForeground(item,-1,ut);
          ColorUtil.setForeground(item,3,betrag.doubleValue());
        }
        catch (Exception e)
        {
          Logger.error("error while formatting item",e);
        }
      }
    
    });
    
    this.addColumn(i18n.tr("Bezeichnung"),      "name");
    if (settings.getBoolean("usage.display.all",false))
      addColumn(i18n.tr("Verwendungszweck"),    "mergedzweck");
    else
      addColumn(i18n.tr("Verwendungszweck"),    Tag.SVWZ.name());
    this.addColumn(i18n.tr("Datum"),            "datum_pseudo", new DateFormatter(HBCI.DATEFORMAT));
    this.addColumn(i18n.tr("Betrag"),           "betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    this.addColumn(i18n.tr("Notiz"),            "kommentar");
    this.addColumn(new KontoColumn());

    this.setContextMenu(new UmsatzList());
    
    this.addSelectionListener(new Listener() {
      @Override
      public void handleEvent(org.eclipse.swt.widgets.Event event)
      {
        featureEvent(Feature.Event.REFRESH,null);
      }
    });
  }

  
  /**
   * @see de.willuhn.jameica.gui.parts.TreePart#setList(de.willuhn.datasource.GenericIterator)
   */
  public void setList(GenericIterator list)
  {
    this.groupCount = new AtomicInteger(0);
    this.umsatzCount = new AtomicInteger(0);
    
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
        
        UmsatzTreeNode node = getNode(lookup,u.getUmsatzTyp());
        if (node != null)
        {
          node.add(u);
          this.umsatzCount.incrementAndGet();
        }
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
      
      try
      {
        Collections.sort(items);
      }
      catch (Exception e)
      {
        Logger.warn("unable to sort categories: " + e.getMessage());
      }
      ////////////////////////////////////////////////////////////////

      super.setList(PseudoIterator.fromArray((GenericObject[])items.toArray(new GenericObject[0])));
      this.featureEvent(Feature.Event.REFRESH,null);
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
    
    // "ut" null gibt es. Das ist der Fall, wenn ein Umsatz keiner Kategorie zugeordnet ist.
    // Wir wollen hier aber geziehlt die raus haben, die nicht in den Auswertungen erscheinen sollen.
    if (ut != null && ut.hasFlag(UmsatzTyp.FLAG_SKIP_REPORTS))
      return null;
    
    // Neu anlegen
    node = new UmsatzTreeNode(ut);
    this.groupCount.incrementAndGet();
    lookup.put(ut.getID(),node);
    
    // Parents checken
    UmsatzTyp parent = (UmsatzTyp) ut.getParent();
    if (parent != null)
    {
      UmsatzTreeNode np = getNode(lookup,parent);
      if (np == null) // Die uebergeordnete Kategorie soll schon nicht angezeigt werden - dann koennen wir hier auch draussen bleiben
        return null;
      
      node.setParent(np);
      np.getSubGroups().add(node);
    }
    
    return node;
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.TreePart#createFeatureEventContext(de.willuhn.jameica.gui.parts.table.Feature.Event, java.lang.Object)
   */
  @Override
  protected Context createFeatureEventContext(Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e, data);
    
    if (this.hasEvent(FeatureSummary.class,e))
      ctx.addon.put(FeatureSummary.CTX_KEY_TEXT,this.getSummary());
    
    return ctx;
  }
  
  /**
   * Liefert den Summen-Text.
   * @return der Summen-Text.
   */
  private String getSummary()
  {
    try
    {
      Object o = this.getSelection();

      // nichts markiert, dann liefern wir nur die Anzahl der Umsaetze und Kategorien
      if (o == null || !(o instanceof Umsatz[]))
        return i18n.tr("Kategorien: {0}, Umsätze: {1}",Integer.toString(this.groupCount != null ? this.groupCount.intValue() : 0),Integer.toString(this.umsatzCount != null ? this.umsatzCount.intValue() : 0));
      
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

      //@formatter:off
      return i18n.tr("{0} Umsätze, {1} markiert, Summe: {2} {3}", Integer.toString(this.umsatzCount != null ? this.umsatzCount.intValue() : null),
                                                                  Integer.toString(list.length),
                                                                  HBCI.DECIMALFORMAT.format(sum),
                                                                  curr);
      //@formatter:on
    }
    catch (Throwable t)
    {
      Logger.error("error while updating summary",t);
    }
    return null;
  }
}
