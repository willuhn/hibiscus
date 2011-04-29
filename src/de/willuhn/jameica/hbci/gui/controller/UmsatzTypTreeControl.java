/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzTypTreeControl.java,v $
 * $Revision: 1.17 $
 * $Date: 2011/04/29 07:41:56 $
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
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

  private SelectInput kontoAuswahl = null;
  private DateInput start          = null;
  private DateInput end            = null;
  
  private UmsatzTree tree          = null;
  private UmsatzTypVerlauf chart   = null;
  private boolean expanded         = false;
  
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(UmsatzTypTreeControl.class);

  static
  {
    settings.setStoreWhenRead(true);
  }

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

    // Standardmaessig verwenden wir das aktuelle Jahr als Bemessungszeitraum
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH,Calendar.JANUARY);
    cal.set(Calendar.DATE,1);

    Date d = DateUtil.startOfDay(cal.getTime());
    try
    {
      String s = settings.getString("laststart",null);
      if (s != null && s.length() > 0)
        d = HBCI.DATEFORMAT.parse(s);
    }
    catch (Exception e)
    {
      // ignore
    }
    this.start = new DateInput(d, HBCI.DATEFORMAT);
    this.start.setComment(i18n.tr("Fr¸hestes Valuta-Datum"));
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

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH,Calendar.DECEMBER);
    cal.set(Calendar.DATE,31);

    Date d = DateUtil.endOfDay(cal.getTime());
    try
    {
      String s = settings.getString("lastend",null);
      if (s != null && s.length() > 0)
        d = HBCI.DATEFORMAT.parse(s);
    }
    catch (Exception e)
    {
      // ignore
    }
    this.end = new DateInput(d, HBCI.DATEFORMAT);
    this.end.setComment(i18n.tr("Sp‰testes Valuta-Datum"));
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
    tree.setKonto((Konto) getKontoAuswahl().getValue());
    tree.setUmsatzTree(getTree().getItems());
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
    Konto konto = (Konto) (Konto) getKontoAuswahl().getValue();

    Date von = (Date) getStart().getValue();
    Date bis = (Date) getEnd().getValue();
    // Wir merken uns die Werte fuer's naechste Mal
    settings.setAttribute("laststart", von == null ? null : HBCI.DATEFORMAT.format(von));
    settings.setAttribute("lastend",   bis == null ? null : HBCI.DATEFORMAT.format(bis));

    DBIterator umsaetze = UmsatzUtil.getUmsaetzeBackwards();
    if (konto != null)
      umsaetze.addFilter("konto_id = " + konto.getID());
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
 * Revision 1.17  2011/04/29 07:41:56  willuhn
 * @N BUGZILLA 781
 *
 * Revision 1.16  2011-04-12 21:16:47  willuhn
 * @N BUGZILLA 629 - statt FocusListener jetzt SelectionListener
 *
 * Revision 1.15  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.14  2010-06-01 12:12:19  willuhn
 * @C Umsaetze in "Kontoauszuege" und "Umsatze nach Kategorien" per Default in umgekehrt chronologischer Reihenfolge liefern - also neue zuerst
 *
 * Revision 1.13  2010/03/22 09:58:34  willuhn
 * @N Auch alle Kind-Kategorien mit zeichnen
 *
 * Revision 1.12  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 *
 * Revision 1.11  2009/11/06 09:50:31  willuhn
 * @B BUGZILLA 779
 *
 * Revision 1.10  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.9  2009/10/07 23:08:56  willuhn
 * @N BUGZILLA 745: Deaktivierte Konten in Auswertungen zwar noch anzeigen, jedoch mit "[]" umschlossen. Bei der Erstellung von neuen Auftraegen bleiben sie jedoch ausgeblendet. Bei der Gelegenheit wird das Default-Konto jetzt mit ">" markiert
 *
 * Revision 1.8  2009/10/05 23:08:40  willuhn
 * @N BUGZILLA 629 - wenn ein oder mehrere Kategorien markiert sind, werden die Charts nur fuer diese gezeichnet
 *
 * Revision 1.7  2009/01/12 00:46:50  willuhn
 * @N Vereinheitlichtes KontoInput in den Auswertungen
 *
 * Revision 1.6  2007/08/28 09:47:09  willuhn
 * @N Bug 395
 *
 * Revision 1.5  2007/08/12 22:02:10  willuhn
 * @C BUGZILLA 394 - restliche Umstellungen von Valuta auf Buchungsdatum
 *
 * Revision 1.4  2007/05/02 11:18:04  willuhn
 * @C PDF-Export von Umsatz-Trees in IO-API gepresst ;)
 *
 * Revision 1.3  2007/04/29 10:19:35  jost
 * Sortierung umgekehrt.
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
 * Neu: Nummer f√ºr die Sortierung der Umsatz-Kategorien
 *
 * Revision 1.3  2007/03/08 18:56:39  willuhn
 * @N Mehrere Spalten in Kategorie-Baum
 *
 * Revision 1.2  2007/03/07 10:29:41  willuhn
 * @B rmi compile fix
 * @B swt refresh behaviour
 *
 * Revision 1.1  2007/03/06 20:06:08  jost
 * Neu: Umsatz-Kategorien-√úbersicht
 *
 ******************************************************************************/
