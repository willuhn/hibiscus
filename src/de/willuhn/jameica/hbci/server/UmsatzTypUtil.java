/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse fuer die Umsatzkategorien.
 */
public class UmsatzTypUtil
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Liefert einen sprechenden Namen fuer den Kategorie-Typ.
   * @param type Typ
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @see UmsatzTyp#TYP_EGAL
   * @return sprechender Name.
   */
  public static String getNameForType(int type)
  {
    
    switch (type)
    {
      case UmsatzTyp.TYP_AUSGABE:
        return i18n.tr("Ausgabe");
      case UmsatzTyp.TYP_EINNAHME:
        return i18n.tr("Einnahme");
    }
    return i18n.tr("egal");
  }
  
  /**
   * Liefert eine Liste aller Umsatz-Kategorien, sortiert nach Nummer und Name.
   * @return Liste aller Umsatz-Kategorien.
   * @throws RemoteException
   */
  public static DBIterator<UmsatzTyp> getAll() throws RemoteException
  {
    DBIterator<UmsatzTyp> list = Settings.getDBService().createList(UmsatzTyp.class);
    list.setOrder("ORDER BY nummer,name");
    return list;
  }

  /**
   * Liefert eine Liste der Umsatz-Kategorien oberster Ebene.
   * @return Liste der Umsatz-Kategorien oberster Ebene.
   * @throws RemoteException
   */
  public static DBIterator<UmsatzTyp> getRootElements() throws RemoteException
  {
    DBIterator<UmsatzTyp> list = getAll();
    // die mit ungueltiger Parent-ID sind quasi Leichen - steht nur sicherheitshalber
    // mit hier drin. Eigentlich sollte die DB sowas via Constraint verhindern
    list.addFilter("parent_id is null or parent_id not in (select id from umsatztyp)");
    return list;
  }
  
  /**
   * Liefert einen Tree mit den gesuchten Umsatz-Kategorien.
   * @param skip einzelner Umsatz-Typ, der nicht enthalten sein soll.
   * Damit ist es zum Beispiel moeglich, eine Endlos-Rekursion zu erzeugen,
   * wenn ein Parent ausgewaehlt werden soll, der User aber die Kategorie
   * sich selbst als Parent zuordnet. Das kann hiermit ausgefiltert werden.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @return der Tree mit den Umsatz-Kategorien.
   * @throws RemoteException
   */
  public static GenericIterator<UmsatzTypBean> getTree(UmsatzTyp skip, int typ) throws RemoteException
  {
    // Wir laden erstmal alle Kategorien in einem einzelnen Query
    DBIterator<UmsatzTyp> it = getAll();

    final List<UmsatzTypBean> all = new LinkedList<UmsatzTypBean>();
    while (it.hasNext())
    {
      UmsatzTyp t = it.next();
      if (filtered(t,skip,typ))
        continue;
      all.add(new UmsatzTypBean(t));
    }

    // Wir ermitteln erstmal nur die Root-Elemente und verarbeiten die dann alle einzeln
    // Im Prinzip koennte man das alles auch bequemer ueber die passenden Methoden von UmsatzTypUtil
    // und GenericObjectNode machen. Das wuerde aber rekursiv eine ganze Reihe von SQL-Queries
    // ausloesen. Bei 50 verschachtelten Kategorien koennen da schnell 200 SQL-Abfragen zusammenkommen,
    // die jedesmal aufgerufen werden, wenn die Selectbox eingeblendet wird. Daher laden wir mit einem
    // einzelnen Query alle Kategorien und erzeugen den Baum dann komplett im Speicher. Das ist erheblich
    // schneller.
    final List<UmsatzTypBean> root = new LinkedList<UmsatzTypBean>();
    for (UmsatzTypBean t:all)
    {
      if (t.getAttribute("parent_id") == null)
      {
        t.collectChildren(all);
        root.add(t);
      }
    }
    return PseudoIterator.fromArray(root.toArray(new UmsatzTypBean[root.size()]));
  }

  /**
   * Liefert eine Liste mit den gesuchten Umsatz-Kategorien.
   * Die Reihenfolge entspricht der von <code>UmsatzTypUtil{@link #getTree(UmsatzTyp, int)}</code>.
   * Die Kategorien koennen also 1:1 in einer Liste angezeigt werden, wenn zur Anzeige
   * <code>UmsatzTypBean#getIndented()</code> verwendet wird.
   * @param skip einzelner Umsatz-Typ, der nicht enthalten sein soll.
   * Damit ist es zum Beispiel moeglich, eine Endlos-Rekursion zu erzeugen,
   * wenn ein Parent ausgewaehlt werden soll, der User aber die Kategorie
   * sich selbst als Parent zuordnet. Das kann hiermit ausgefiltert werden.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @return der Tree mit den Umsatz-Kategorien.
   * @throws RemoteException
   */
  public static List<UmsatzTypBean> getList(UmsatzTyp skip, int typ) throws RemoteException
  {
    GenericIterator<UmsatzTypBean> tree = getTree(skip,typ);
    
    // Jetzt rekursiv in Liste uebertragen
    List<UmsatzTypBean> result = new LinkedList<UmsatzTypBean>();
    while (tree.hasNext())
    {
      collect(tree.next(),result);
    }
    
    return result;
  }
  
  /**
   * Traegt die Kategorie und alle Kinder rekursiv in die Liste ein.
   * @param bean die Kategorie.
   * @param target die Ziel-Liste.
   * @throws RemoteException
   */
  private static void collect(UmsatzTypBean bean, List<UmsatzTypBean> target) throws RemoteException
  {
    target.add(bean);
    GenericIterator<UmsatzTypBean> children = bean.getChildren();
    while (children.hasNext())
    {
      collect(children.next(),target);
    }
  }

  /**
   * Prueft, ob die Umsatz-Kategorie gefiltert werden sollte, wenn sie entweder identisch
   * zu <code>skip</code> ist oder weil der angegebene Typ nicht passt.
   * @param ut die zu testende Umsatz-Kategorie.
   * @param skip die optionale zu ueberspringende Umsatz-Kategorie.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @return true, wenn der Umsatz gefiltert werden soll.
   * @throws RemoteException
   */
  private static boolean filtered(UmsatzTyp ut, UmsatzTyp skip, int typ) throws RemoteException
  {
    if (ut == null)
      return true;
    
    if (skip != null && BeanUtil.equals(skip,ut))
      return true;

    int ti = ut.getTyp();
    if (typ == UmsatzTyp.TYP_EGAL || ti == UmsatzTyp.TYP_EGAL)
      return false;
        
    return ti != typ;
  }
}
