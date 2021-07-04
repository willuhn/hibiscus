/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.DateFromInput;
import de.willuhn.jameica.hbci.gui.input.DateToInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.RangeInput;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTree;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypVerlauf;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Umsatz-Kategorien-Auswertung
 */
public class UmsatzTypTreeControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static Map cache = new HashMap();

  private KontoInput kontoAuswahl  = null;
  private TextInput text           = null;
  private DateInput start          = null;
  private DateInput end            = null;
  private RangeInput range         = null;
  
  private UmsatzTree tree          = null;
  private UmsatzTypVerlauf chart   = null;
  private boolean expanded         = false;
  
  private Listener listener        = null;
  
  /**
   * ct.
   * 
   * @param view
   */
  public UmsatzTypTreeControl(AbstractView view)
  {
    super(view);
    
    // bei Ausloesungen ueber SWT-Events verzoegern wir
    // das Reload, um schnell aufeinanderfolgende Updates
    // zu buendeln.
    this.listener = new DelayedListener(new Listener() {
      public void handleEvent(Event event)
      {
        handleReload();
      }
    });
  }
  
  /**
   * Erzeugt einen Listener, der das Event nur dann weiterleitet, wenn an
   * der Auswahl etwas geaendert wurde.
   * @param input das Input-Feld, welches ueberwacht werden soll.
   * @return der Listener.
   */
  public Listener changedListener(final Input input)
  {
    return new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        if (input.hasChanged())
        {
          listener.handleEvent(event);
        }
      }
    };
  }

  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * 
   * @return Auswahlbox.
   * @throws RemoteException
   */
  public Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;
    
    this.kontoAuswahl = new KontoInput(null,KontoFilter.ALL);
    this.kontoAuswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    this.kontoAuswahl.setSupportGroups(true);
    this.kontoAuswahl.setComment(null);
    this.kontoAuswahl.setRememberSelection("auswertungen.umsatztree");
    this.kontoAuswahl.addListener(this.changedListener(this.kontoAuswahl));
    return this.kontoAuswahl;
  }
  
  /**
   * Liefert ein Eingabefeld fuer einen Suchbegriff.
   * @return Eingabefeld fuer einen Suchbegriff.
   */
  public TextInput getText()
  {
    if (this.text != null)
      return this.text;

    this.text = new TextInput((String)cache.get("kontoauszug.list.text"),HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
    this.text.setName(i18n.tr("Suchbegriff"));
    return this.text;
  }
  
  /**
   * Liefert eine Auswahl mit Zeit-Presets.
   * @return eine Auswahl mit Zeit-Presets.
   */
  public RangeInput getRange()
  {
    if (this.range != null)
      return this.range;
    
    this.range = new RangeInput(this.getStart(),this.getEnd(), Range.CATEGORY_AUSWERTUNG, "auswertungen.umsatztree.filter.range");
    this.range.addListener(this.changedListener(this.range));
    
    return this.range;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das Start-Datum.
   * 
   * @return Auswahl-Feld.
   */
  public Input getStart()
  {
    if (this.start != null)
      return this.start;

    this.start = new DateFromInput(null, "auswertungen.umsatztree.filter.from");
    this.start.setName(i18n.tr("Von"));
    this.start.setComment(null);
    this.start.addListener(this.changedListener(this.start));
    return this.start;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das End-Datum.
   * 
   * @return Auswahl-Feld.
   */
  public Input getEnd()
  {
    if (this.end != null)
      return this.end;

    this.end = new DateToInput(null, "auswertungen.umsatztree.filter.to");
    this.end.setName(i18n.tr("bis"));
    this.end.setComment(null);
    this.end.addListener(this.changedListener(this.end));
    return this.end;
  }
  
  /**
   * Liefert ein Container-Objekt zum Export des Umsatz-Tree samt Metadaten.
   * @return Umsatztree.
   * @throws RemoteException
   */
  public de.willuhn.jameica.hbci.io.UmsatzTree getUmsatzTree() throws RemoteException
  {
    de.willuhn.jameica.hbci.io.UmsatzTree tree = new de.willuhn.jameica.hbci.io.UmsatzTree();
    tree.setEnd((Date) getEnd().getValue());
    tree.setStart((Date) getStart().getValue());
    Object konto = getKontoAuswahl().getValue();
    if (konto != null && (konto instanceof Konto))
      tree.setTitle(((Konto) konto).getBezeichnung());
    else if (konto != null && (konto instanceof String))
      tree.setTitle((String) konto);
    
    Object o = getTree().getSelection();
    List<UmsatzTreeNode> selection = new LinkedList<UmsatzTreeNode>(); 
    if (o instanceof UmsatzTreeNode)
      selection.add((UmsatzTreeNode)o);
    else if (o instanceof UmsatzTreeNode[])
      selection.addAll(Arrays.asList((UmsatzTreeNode[])o));
    
    tree.setUmsatzTree(selection.size() > 0 ? selection : getTree().getItems());
    return tree;
  }

  
  /**
   * Liefert einen Baum von Umsatzkategorien mit den Umsaetzen.
   * @return Baum mit Umsatz-Kategorien.
   * @throws RemoteException
   */
  public TreePart getTree() throws RemoteException
  {
    if (this.tree != null)
      return this.tree;
    
    this.tree = new UmsatzTree(getUmsaetze());
    this.tree.setExpanded(this.expanded);
    return this.tree;
  }
  
  /**
   * Liefert die anzuzeigenden Umsaetze.
   * @return die anzuzeigenden Umsaetze.
   * @throws RemoteException
   */
  private DBIterator getUmsaetze() throws RemoteException
  {
    Object o    = getKontoAuswahl().getValue();

    Date von    = (Date) getStart().getValue();
    Date bis    = (Date) getEnd().getValue();
    String text = (String) getText().getValue();

    cache.put("kontoauszug.list.text",text);

    Konto k = (o instanceof Konto) ? (Konto) o : null;
    String kat = (o instanceof String) ? (String) o : null;
    
    return UmsatzUtil.find(k,kat,von,bis,text);
  }
  
  /**
   * Liefert die Chart-Ansicht der Kategorien.
   * @return die Chart-Ansicht.
   * @throws RemoteException
   */
  public UmsatzTypVerlauf getChart() throws RemoteException
  {
    if (this.chart != null)
      return this.chart;
    
    this.chart = new UmsatzTypVerlauf();
    this.chart.setData(getAllGroups(),(Date) getStart().getValue(),(Date) getEnd().getValue());
    return this.chart;
  }
  
  /**
   * Klappt alle Elemente auf oder zu.
   */
  public void handleExpand()
  {
    try
    {
      TreePart tree = getTree();
      List items = tree.getItems();
      for (Object item : items) {
        tree.setExpanded((GenericObject) item,!this.expanded,true);
      }
      this.expanded = !this.expanded;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to expand tree",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aufklappen/Zuklappen"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Aktualisiert den Tree.
   */
  public void handleReload()
  {
    try
    {
      getTree().setList(getUmsaetze());
      getTree().restoreState();
      handleRefreshChart();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to redraw tree",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Aktualisiert den Chart.
   */
  public void handleRefreshChart()
  {
    try
    {
      // 1. Wir holen uns die aktuell selektierten Objekte
      Object selection = getTree().getSelection();

      List l = null;
      
      if (selection != null && (selection instanceof UmsatzTreeNode[])) // Mehrere Kategorien markiert?
      {
        l = Arrays.asList((UmsatzTreeNode[])selection);
      }
      else if (selection != null && (selection instanceof UmsatzTreeNode)) // Eine Kategorie markiert?
      {
        l = new ArrayList();
        l.add(selection);
      }

      // keine brauchbare Selektrion.
      if (l == null)
        l = getAllGroups();

      getChart().setData(l, (Date) getStart().getValue(), (Date) getEnd().getValue());
      getChart().redraw();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to redraw chart",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Liefert alle Umsatzkategorien, die gerade angezeigt werden.
   * @return Liste aller Umsatz-Kategorien - also nicht nur die oberste Ebene.
   * @throws RemoteException
   */
  private List<GenericObjectNode> getAllGroups() throws RemoteException
  {
    List<GenericObjectNode> list = new ArrayList<GenericObjectNode>();
    List<GenericObjectNode> root = getTree().getItems();
    for (GenericObjectNode r:root)
    {
      _addGroup(r,list);
    }
    return list;
  }
  /**
   * Fuegt das Element und die Kind-Elemente zur Liste hinzu.
   * @param root das Root-Element.
   * @param target Ziel-Liste.
   * @throws RemoteException
   */
  private void _addGroup(GenericObjectNode root, List<GenericObjectNode> target) throws RemoteException
  {
    target.add(root);
    GenericIterator children = root.getChildren();
    while (children.hasNext())
    {
      GenericObject o = children.next();
      if (o instanceof GenericObjectNode)
        _addGroup((GenericObjectNode)o,target);
    }
  }
}
