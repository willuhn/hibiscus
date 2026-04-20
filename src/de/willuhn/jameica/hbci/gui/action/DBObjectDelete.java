/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBObjectNode;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectDeletedMessage;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Generische Action fuer das Loeschen von Datensaetzen.
 */
public class DBObjectDelete implements Action
{
  private I18N i18n = null;
  
  /**
   * ct.
   */
  public DBObjectDelete()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Erwartet ein Objekt vom Typ <code>DBObject</code> oder <code>DBObject[]</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null)
      throw new ApplicationException(i18n.tr("Keine zu löschenden Daten ausgewählt"));

    if (!(context instanceof DBObject) && !(context instanceof DBObject[]))
    {
      Logger.warn("wrong type to delete: " + context.getClass());
      return;
    }

    boolean array = (context instanceof DBObject[]);
    DBObject[] objects = array ? (DBObject[]) context : new DBObject[]{(DBObject)context};

    boolean isUmsatzTypSelection = this.isUmsatzTypSelection(objects);
    boolean hasAssignments = false;
    if (isUmsatzTypSelection)
    {
      try
      {
        hasAssignments = this.hasAssignedUmsaetze(objects);
      }
      catch (Exception e)
      {
        Logger.error("unable to detect assignment count for delete warning",e);
        hasAssignments = false;
      }
    }

    // Sicherheitsabfrage
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    if (array)
    {
      d.setTitle(i18n.tr("Daten löschen"));
      if (hasAssignments)
        d.setText(i18n.tr("Es existieren Umsaetze zu mindestens einer der ausgewaehlten Kategorien. Wirklich loeschen?"));
      else
        d.setText(i18n.tr("Wollen Sie diese {0} Datensätze wirklich löschen?",""+objects.length));
    }
    else
    {
      d.setTitle(i18n.tr("Daten löschen"));
      if (hasAssignments && objects[0] instanceof UmsatzTyp)
      {
        String name = this.getUmsatzTypName((UmsatzTyp) objects[0]);
        d.setText(i18n.tr("Es existieren Umsaetze zu {0}. Wirklich loeschen?",name));
      }
      else
      {
        d.setText(i18n.tr("Wollen Sie diesen Datensatz wirklich löschen?"));
      }
    }
    try {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
        return;
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting objects",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des Datensatzes"));
      return;
    }

    DBObject[] list = objects;
    
    Worker worker = new Worker(list);

// Das machen wir nicht mehr. Durch die dauernden Wechsel im Event Dispatcher wird das schweinelangsam
//    if (list.length > 100)
//      Application.getController().start(worker);
//    else
    worker.run(null);
  }

  private boolean isUmsatzTypSelection(DBObject[] objects)
  {
    if (objects == null || objects.length == 0)
      return false;

    for (DBObject o:objects)
    {
      if (!(o instanceof UmsatzTyp))
        return false;
    }

    return true;
  }

  private boolean hasAssignedUmsaetze(DBObject[] objects) throws RemoteException
  {
    if (objects == null)
      return false;

    // Fast-path fuer den haeufigsten Fall: direkt markierte Kategorie hat Umsaetze.
    for (DBObject o:objects)
    {
      UmsatzTyp typ = (UmsatzTyp) o;
      if (this.hasAssignedUmsaetze(typ))
        return true;
    }

    // Danach rekursiv auf Unterkategorien der Auswahl pruefen.
    Set<String> ids = this.collectAffectedKategorieIds(objects);
    for (String id:ids)
    {
      if (id == null)
        continue;

      UmsatzTyp typ = this.resolveUmsatzTyp(id);
      if (typ == null)
        continue;

      if (this.hasAssignedUmsaetze(typ))
        return true;
    }

    return false;
  }

  private boolean hasAssignedUmsaetze(UmsatzTyp typ) throws RemoteException
  {
    if (typ == null || typ.isNewObject())
      return false;

    return typ.getUmsaetze().hasNext();
  }

  private UmsatzTyp resolveUmsatzTyp(String id) throws RemoteException
  {
    if (id == null)
      return null;

    UmsatzTyp typ = (UmsatzTyp) de.willuhn.jameica.hbci.Settings.getDBService().createObject(UmsatzTyp.class,id);
    if (typ == null || typ.isNewObject())
      return null;

    return typ;
  }

  private Set<String> collectAffectedKategorieIds(DBObject[] objects) throws RemoteException
  {
    Set<String> selected = new HashSet<String>();
    for (DBObject o:objects)
    {
      UmsatzTyp typ = (UmsatzTyp) o;
      String id = typ.getID();
      if (id != null)
        selected.add(id);
    }

    if (selected.isEmpty())
      return selected;

    DBIterator<UmsatzTyp> all = Settings.getDBService().createList(UmsatzTyp.class);
    Map<String,List<String>> childrenByParent = new HashMap<String,List<String>>();
    while (all.hasNext())
    {
      UmsatzTyp t = all.next();
      String id = t.getID();
      Object parent = t.getAttribute("parent_id");
      if (id == null || parent == null)
        continue;

      String parentId = String.valueOf(parent);
      List<String> children = childrenByParent.get(parentId);
      if (children == null)
      {
        children = new LinkedList<String>();
        childrenByParent.put(parentId,children);
      }
      children.add(id);
    }

    LinkedList<String> queue = new LinkedList<String>(selected);
    while (!queue.isEmpty())
    {
      String current = queue.removeFirst();
      List<String> children = childrenByParent.get(current);
      if (children == null)
        continue;

      for (String child:children)
      {
        if (selected.add(child))
          queue.add(child);
      }
    }

    return selected;
  }
  
  private String getUmsatzTypName(UmsatzTyp typ)
  {
    if (typ == null)
      return "";

    try
    {
      String name = typ.getName();
      return name != null ? name : "";
    }
    catch (Exception e)
    {
      Logger.error("unable to resolve category name for delete warning",e);
      return "";
    }
  }

  /**
   * Damit koennen wir lange Loeschvorgaenge ggf. im Hintergrund laufen lassen
   */
  private class Worker implements BackgroundTask
  {
    private boolean cancel = false;
    private DBObject[] list = null;

    /**
     * ct.
     * @param list
     */
    private Worker(DBObject[] list)
    {
      this.list = list;
    }
    
    /**
     * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
     */
    public void interrupt()
    {
      this.cancel = true;
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
     */
    public boolean isInterrupted()
    {
      return this.cancel;
    }

    /**
     * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
     */
    public void run(ProgressMonitor monitor) throws ApplicationException
    {
      try
      {
        if (monitor != null)
          monitor.setStatusText(i18n.tr("Lösche {0} Datensätze",""+list.length));

        double factor = 100d / list.length;
        
        // Wenn es eine Liste von Node-Objekten ist, sortieren wir sie nach
        // Einrückungstiefe - unten beginnend. Das stellt sicher, dass wir
        // zuerst die Kinder löschen, bevor die Eltern gelöscht werden.
        if (list instanceof DBObjectNode[])
        {
          Arrays.sort((DBObjectNode[])list,(o1,o2) -> {
            try {
              return Integer.compare(o2.getPath().size(),o1.getPath().size());
            }
            catch (RemoteException re) {}
            return 0;
          });
        }
        
        for (int i=0;i<list.length;++i)
        {
          if (monitor != null && i % 4 == 0)
            monitor.setPercentComplete((int)((i+4) * factor));

          if (list[i].isNewObject())
            continue; // muss nicht geloescht werden

          // ok, wir loeschen das Objekt
          final String id = list[i].getID();
          if (id == null)
            continue;
          
          list[i].delete();
          Application.getMessagingFactory().sendMessage(new ObjectDeletedMessage(list[i],id));
        }
        
        if (monitor != null)
          monitor.setPercentComplete(100);
        
        String text = i18n.tr("Datensatz gelöscht.");
        if (list.length > 1)
          text = i18n.tr("{0} Datensätze gelöscht.",""+list.length);
        
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_SUCCESS));
        if (monitor != null)
        {
          monitor.setStatusText(text);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
        }

      }
      catch (RemoteException e)
      {
        Logger.error("error while deleting objects",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Löschen der Datensätze."), StatusBarMessage.TYPE_ERROR));

        if (monitor != null)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setStatusText(i18n.tr("Fehler beim Löschen der Daten"));
          monitor.log(e.toString());
        }
      }
      catch (ApplicationException ae)
      {
        if (monitor != null)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setStatusText(ae.getMessage());
        }
        throw ae;
      }
    }
  }
}
