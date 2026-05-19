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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectDeletedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines fix und fertig vorkonfigurierten Trees mit den existiernden Umsatz-Typen.
 */
public class UmsatzTypTree extends TreePart
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private DelayedListener delayed = new DelayedListener(new UpdateListener());
  private CheckboxInput includeChildren = null;
  private TextInput filterText = null;
  
  private Set<String> visibleIDs = null;

  /**
   * Initialisiert die Liste der Root-Elemente.
   * @return Liste der Root-Elemente.
   * @throws RemoteException
   */
  private final static GenericIterator init() throws RemoteException
  {
    return UmsatzTypUtil.getRootElements();
  }

  /**
   * ct.
   * @param action
   * @throws RemoteException
   */
  public UmsatzTypTree(Action action) throws RemoteException
  {
    super(init(), action);
    addColumn(i18n.tr("Bezeichnung"),"name");
    addColumn(i18n.tr("Reihenfolge"),"nummer"); // BUGZILLA 554/988
    addColumn(i18n.tr("Suchbegriff"),"pattern"); // BUGZILLA 756
    addColumn(i18n.tr("Umsatzart"),"umsatztyp",new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return i18n.tr("egal");
        return UmsatzTypUtil.getNameForType(((Integer) o).intValue());
      }
    });
    addColumn(i18n.tr("Konto"),"dummy");
    addColumn(i18n.tr("Kommentar"),"kommentar");

    this.setFormatter(new TreeFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TreeFormatter#format(org.eclipse.swt.widgets.TreeItem)
       */
      public void format(TreeItem item)
      {
        if (item == null)
          return;

        try
        {
          UmsatzTyp ut = (UmsatzTyp) item.getData();
          if (ut == null)
            return;

          final String kat = ut.getKontoKategorie();
          final Konto k = ut.getKonto();
          if (k != null)
            item.setText(4,KontoUtil.toString(k));
          else if (kat != null)
            item.setText(4,kat);

          ColorUtil.setForeground(item,-1,ut);
        }
        catch (Exception e)
        {
          Logger.error("unable to apply custom color",e);
        }
      }
    });

    this.setMulti(true);
    this.setRememberColWidths(true);
    this.setRememberOrder(true);
    this.setRememberState(true);
    this.setContextMenu(new de.willuhn.jameica.hbci.gui.menus.UmsatzTypList());
  }
  
  /**
   * Liefert die Checkbox, mit der eingestellt werden kann, ob die Kind-Elemente von Treffern angezeigt werden sollen.
   * @return die Checkbox.
   */
  private CheckboxInput getIncludeChildren()
  {
    if (this.includeChildren != null)
      return this.includeChildren;
    
    this.includeChildren = new CheckboxInput(Boolean.TRUE);
    this.includeChildren.setName(i18n.tr("Unterkategorien von Treffern anzeigen"));
    this.includeChildren.addListener(e -> this.refreshView());
    return this.includeChildren;
  }
  
  /**
   * Liefert ein Eingabefeld für einen Suchbegriff.
   * @return Eingabefeld.
   */
  private TextInput getFilterText()
  {
    if (this.filterText != null)
      return this.filterText;
    
    this.filterText = new TextInput("",50,i18n.tr("Suchbegriff"));
    this.filterText.addListener(e -> this.refreshView());
    return this.filterText;
  }

  /**
   * Aktualisiert die Anzeige.
   */
  private void refreshView()
  {
    try
    {
      this.visibleIDs = this.createVisibleIDs();
      this.setList(this.getFilteredRootElements());
    }
    catch (Exception e)
    {
      Logger.error("unable to refresh category tree",e);
    }
  }


  /**
   * Überschrieben, um die Liste zu filtern.
   * @see de.willuhn.jameica.gui.parts.TreePart#getChildren(java.lang.Object)
   */
  protected List getChildren(Object o)
  {
    final List children = super.getChildren(o);
    if (children == null || this.visibleIDs == null)
      return children;

    final List filtered = new LinkedList();
    for (Object child:children)
    {
      if (!(child instanceof UmsatzTyp))
        continue;

      try
      {
        final String id = ((UmsatzTyp) child).getID();
        if (id != null && this.visibleIDs.contains(id))
          filtered.add(child);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to apply tree filter",re);
      }
    }
    return filtered;
  }

  /**
   * Klappt alle Elemente auf oder zu.
   * @param expanded true, wenn alle Elemente aufgeklappt sein sollen.
   */
  public void setAllExpanded(boolean expanded)
  {
    try
    {
      final List items = this.getItems();
      if (items == null || items.isEmpty())
        return;

      for (Object item:items)
      {
        if (item instanceof GenericObject)
          this.setExpanded((GenericObject)item,expanded,true);
      }
    }
    catch (RemoteException re)
    {
      Logger.error("unable to update expand state",re);
    }
  }

  /**
   * Liefert die gefilterte Liste der Root-Elemente.
   * @return die gefilterte Liste der Root-Elemente.
   * @throws RemoteException
   */
  private List getFilteredRootElements() throws RemoteException
  {
    final List all = PseudoIterator.asList(init());
    if (this.visibleIDs == null)
      return all;

    final List result = new LinkedList();
    for (Object o:all)
    {
      if (!(o instanceof UmsatzTyp))
        continue;

      final String id = ((UmsatzTyp)o).getID();
      if (id != null && this.visibleIDs.contains(id))
        result.add(o);
    }
    return result;
  }

  /**
   * Liefert das Set von IDs mit den anzuzeigenden Elementen.
   * @return das Set mit den anzuzeigenden Elementen oder NULL, wenn nichts gefiltert werden soll.
   * @throws RemoteException
   */
  private Set<String> createVisibleIDs() throws RemoteException
  {
    String query = (String) this.getFilterText().getValue();
    if (StringUtils.trimToNull(query) == null)
      return null;
    
    query = query.toLowerCase();

    final List<UmsatzTyp> all = new ArrayList<UmsatzTyp>();
    final Map<String,String> parentByChildID = new HashMap<>();
    final Map<String,List<String>> childrenByParentID = new HashMap<>();

    final GenericIterator<UmsatzTyp> it = UmsatzTypUtil.getAll();
    while (it.hasNext())
    {
      final UmsatzTyp t = it.next();
      final String id = t.getID();
      if (id == null)
        continue;

      all.add(t);

      final Object p = t.getAttribute("parent_id");
      String parentID = null;
      if (p instanceof GenericObject)
        parentID = ((GenericObject)p).getID();
      else if (p != null)
        parentID = p.toString();

      parentByChildID.put(id,parentID);
      if (parentID != null)
      {
        List<String> children = childrenByParentID.get(parentID);
        if (children == null)
        {
          children = new LinkedList<String>();
          childrenByParentID.put(parentID,children);
        }
        children.add(id);
      }
    }

    final Set<String> visible = new HashSet<String>();
    final boolean b = ((Boolean)this.getIncludeChildren().getValue()).booleanValue();
    for (UmsatzTyp t:all)
    {
      String name = t.getName();
      if (name == null || !name.toLowerCase().contains(query))
        continue;

      String id = t.getID();
      if (id == null)
        continue;

      visible.add(id);
      this.addParents(id,parentByChildID,visible);
      if (b)
        this.addChildren(id,childrenByParentID,visible);
    }

    return visible;
  }

  /**
   * Fügt die Eltern-Elemente hinzu.
   * @param id die ID des Kind-Elements.
   * @param parentByChildID die Lookup-Map für die Eltern.
   * @param target das Ziel-Set.
   */
  private void addParents(String id, Map<String,String> parentByChildID, Set<String> target)
  {
    String parent = parentByChildID.get(id);
    int max = 200;
    while (parent != null && max-- > 0)
    {
      if (!target.add(parent))
        break;
      parent = parentByChildID.get(parent);
    }
  }

  /**
   * Fügt die Kind-Elemente hinzu.
   * @param id die ID des Eltern-Elements.
   * @param childrenByParentID die Lookup-Map für die Kinder.
   * @param target das Ziel-Set.
   */
  private void addChildren(String id, Map<String,List<String>> childrenByParentID, Set<String> target)
  {
    Queue<String> queue = new ArrayDeque<String>();
    queue.add(id);

    int max = 10000;
    while (!queue.isEmpty() && max-- > 0)
    {
      String current = queue.poll();
      List<String> children = childrenByParentID.get(current);
      if (children == null)
        continue;

      for (String child:children)
      {
        if (target.add(child))
          queue.add(child);
      }
    }
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TreePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final Container c = new SimpleContainer(parent);
    final TextInput t = this.getFilterText();
    c.addPart(t);
    
    t.getControl().addKeyListener(new KeyAdapter() {
      /**
       * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
       */
      @Override
      public void keyReleased(KeyEvent e)
      {
        delayed.handleEvent(null);
      }
    });
    c.addInput(this.getIncludeChildren());

    super.paint(parent);
    final MessageConsumer mc = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(mc);
    parent.addDisposeListener(new DisposeListener()
    {
      /**
       * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
       */
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
  }

  /**
   * Hilfsklasse, um ueber das Loeschen von Kategorien benachrichtigt zu werden.
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ObjectDeletedMessage.class,ImportMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      ObjectMessage msg = (ObjectMessage) message;
      GenericObject o = msg.getObject();
      if (!(o instanceof UmsatzTyp))
        return;

      delayed.handleEvent(null);
    }
  }

  /**
   * Listener, der das Aktualisieren des Tree übernimmt.
   */
  private class UpdateListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      GUI.getDisplay().asyncExec(new Runnable()
      {
        public void run()
        {
          refreshView();
        }
      });
    }
  }
}
