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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Spezial-Action fuer das Loeschen von Umsatzkategorien mit Warnhinweis,
 * wenn Umsaetze an der Auswahl oder deren Unterkategorien haengen.
 */
public class UmsatzTypDelete extends DBObjectDelete
{
  /**
   * @see de.willuhn.jameica.hbci.gui.action.DBObjectDelete#confirmDelete(de.willuhn.datasource.rmi.DBObject[])
   */
  protected boolean confirmDelete(DBObject[] list) throws ApplicationException
  {
    if (!this.isUmsatzTypSelection(list))
      return super.confirmDelete(list);

    boolean hasAssignments = false;
    try
    {
      hasAssignments = this.hasAssignedUmsaetze(list);
    }
    catch (Exception e)
    {
      Logger.error("unable to detect assignment count for delete warning",e);
      hasAssignments = false;
    }

    if (!hasAssignments)
      return super.confirmDelete(list);

    String title = this.getI18N().tr("Daten l\u00F6schen");
    String text = null;
    if (list.length > 1)
      text = this.getI18N().tr("Es existieren Umsaetze zu mindestens einer der ausgewaehlten Kategorien. Wirklich loeschen?");
    else
      text = this.getI18N().tr("Es existieren Umsaetze zu {0}. Wirklich loeschen?",this.getUmsatzTypName((UmsatzTyp)list[0]));

    return this.showConfirmationDialog(title,text);
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

    // Fast-path: direkt markierte Kategorie hat Umsaetze.
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

    UmsatzTyp typ = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,id);
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
}
