/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzTypTreeControl.java,v $
 * $Revision: 1.21 $
 * $Date: 2012/04/05 21:44:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.DateFromInput;
import de.willuhn.jameica.hbci.gui.input.DateToInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTree;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypVerlauf;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Umsatz-Kategorien-Auswertung
 */
public class UmsatzTypTreeControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private KontoInput kontoAuswahl  = null;
  private DateInput start          = null;
  private DateInput end            = null;
  
  private UmsatzTree tree          = null;
  private UmsatzTypVerlauf chart   = null;
  private boolean expanded         = false;
  
  /**
   * ct.
   * 
   * @param view
   */
  public UmsatzTypTreeControl(AbstractView view)
  {
    super(view);
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
    this.kontoAuswahl.setRememberSelection("auswertungen.umsatztree");
    this.kontoAuswahl.setSupportGroups(true);
    this.kontoAuswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    return this.kontoAuswahl;
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

    this.start = new DateFromInput(null,"umsatzlist.filter.from");
    this.start.setComment(i18n.tr("Frühestes Datum"));
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

    this.end = new DateToInput(null,"umsatzlist.filter.to");
    this.end.setComment(i18n.tr("Spätestes Datum"));
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

    Date von = (Date) getStart().getValue();
    Date bis = (Date) getEnd().getValue();

    DBIterator umsaetze = UmsatzUtil.getUmsaetzeBackwards();
    if (o != null && (o instanceof Konto))
      umsaetze.addFilter("konto_id = " + ((Konto) o).getID());
    else if (o != null && (o instanceof String))
      umsaetze.addFilter("konto_id in (select id from konto where kategorie = ?)", (String) o);
    if (von != null) umsaetze.addFilter("datum >= ?", new Object[]{new java.sql.Date(DateUtil.startOfDay(von).getTime())});
    if (bis != null) umsaetze.addFilter("datum <= ?", new Object[]{new java.sql.Date(DateUtil.endOfDay(bis).getTime())});
    
    return umsaetze;
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
      for (int i=0;i<items.size();++i)
      {
        tree.setExpanded((GenericObject)items.get(i),!this.expanded,true);
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

/*******************************************************************************
 * $Log: UmsatzTypTreeControl.java,v $
 * Revision 1.21  2012/04/05 21:44:18  willuhn
 * @B BUGZILLA 1219
 *
 * Revision 1.20  2011/12/18 23:20:20  willuhn
 * @N GUI-Politur
 *
 * Revision 1.19  2011-08-05 11:21:58  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 * Revision 1.18  2011-05-19 08:41:53  willuhn
 * @N BUGZILLA 1038 - generische Loesung
 *
 * Revision 1.17  2011-04-29 07:41:56  willuhn
 * @N BUGZILLA 781
 *
 * Revision 1.16  2011-04-12 21:16:47  willuhn
 * @N BUGZILLA 629 - statt FocusListener jetzt SelectionListener
 *
 * Revision 1.15  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 ******************************************************************************/
