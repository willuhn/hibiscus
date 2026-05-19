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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
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
    if (list == null || list.length == 0)
      return super.confirmDelete(list);

    try
    {
      if (!this.hasAssignedUmsaetze(list))
        return super.confirmDelete(list);

      final String text = i18n.tr(list.length > 1 ? "Es existieren Umsätze zu mindestens einer der ausgewählten Kategorien. Wirklich löschen?" : "Es existieren Umsätze zu dieser Kategorie. Wirklich löschen?");
      return this.showConfirmationDialog(text);
    }
    catch (Exception e)
    {
      Logger.error("unable to detect assignment count for delete warning",e);
      return super.confirmDelete(list);
    }
  }

  /**
   * Liefert true, wenn für mindestens eine der Kategorien Umsätze existieren.
   * Das schliesst auch die Suche in untergeordneten Kategorien ein.
   * @param objects die Kategorien.
   * @return true, wenn Umsätze existieren.
   * @throws RemoteException
   */
  private boolean hasAssignedUmsaetze(DBObject[] objects) throws RemoteException
  {
    final List<UmsatzTyp> list = new ArrayList<>();
    
    // Fast-path: direkt markierte Kategorie hat Umsaetze.
    for (DBObject o:objects)
    {
      if (!(o instanceof UmsatzTyp))
        continue;
      
      final UmsatzTyp typ = (UmsatzTyp) o;
      if (this.hasAssignedUmsaetze(typ))
        return true;
      
      list.add(typ);
    }

    // Danach rekursiv auf Unterkategorien der Auswahl pruefen.
    final Set<UmsatzTyp> affected = this.getAffected(list);
    for (UmsatzTyp t:affected)
    {
      if (this.hasAssignedUmsaetze(t))
        return true;
    }

    return false;
  }

  /**
   * Liefert true, wenn der Umsatztyp Umsaetze enthaelt.
   * @param typ der Typ.
   * @return true, wenn er Umsaetze enthaelt.
   * @throws RemoteException
   */
  private boolean hasAssignedUmsaetze(UmsatzTyp typ) throws RemoteException
  {
    if (typ == null || typ.isNewObject())
      return false;

    return typ.getUmsaetze().hasNext();
  }

  /**
   * Liefert alle übergebenen sowie alle direkt und indirekten Kind-Kategorien, die von
   * der Löschaktion betroffen sind. 
   * @param list die Liste der zu löschenden Kategorien.
   * @return die Liste der zu löschenden sowie aller Kind-Kategorien.
   * @throws RemoteException
   */
  private Set<UmsatzTyp> getAffected(List<UmsatzTyp> list) throws RemoteException
  {
    final DBIterator<UmsatzTyp> all = Settings.getDBService().createList(UmsatzTyp.class);
    final Map<String,List<UmsatzTyp>> childrenByParent = new HashMap<>();
    while (all.hasNext())
    {
      final UmsatzTyp t = all.next();
      final Object parent = t.getAttribute("parent_id");
      if (parent == null)
        continue;

      final String parentId = String.valueOf(parent);
      final List<UmsatzTyp> children = childrenByParent.computeIfAbsent(parentId, k -> new LinkedList<>());
      children.add(t);
    }

    final Set<UmsatzTyp> result = new HashSet<>();
    final LinkedList<UmsatzTyp> queue = new LinkedList<>(list);
    while (!queue.isEmpty())
    {
      final UmsatzTyp current = queue.removeFirst();
      List<UmsatzTyp> children = childrenByParent.get(current.getID());
      if (children == null)
        continue;

      for (UmsatzTyp child:children)
      {
        if (result.add(child))
          queue.add(child);
      }
    }

    return result;
  }
}
