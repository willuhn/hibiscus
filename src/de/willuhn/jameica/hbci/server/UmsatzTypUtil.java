/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBObjectNode;
import de.willuhn.datasource.rmi.Listener;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse fuer die Umsatzkategorien.
 */
public class UmsatzTypUtil
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Virtueller Umsatz-Typ "Nicht zugeordnet".
   */
  public final static UmsatzTyp UNASSIGNED = new UmsatzTypUnassigned();

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
    list.setOrder("ORDER BY COALESCE(nummer,''),name");
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
  
  /**
   * Vergleicht zwei Kategorien.
   * @param t1 Kategorie 1.
   * @param t2 Kategorie 2.
   * @return Sortierung.
   * @throws RemoteException
   */
  public static int compare(UmsatzTyp t1, UmsatzTyp t2) throws RemoteException
  {
    // Nicht zugeordnete Kategorien ganz am Anfang
    if (t1 == null)
      return -1;
    
    if (t2 == null)
      return 1;

    // Erst Ausgaben, dann Einnahmen, dann Rest
    int thisType  = t1.getTyp();
    int otherType = t2.getTyp();
    if (thisType != otherType && thisType != UmsatzTyp.TYP_EGAL && otherType != UmsatzTyp.TYP_EGAL)
      return thisType < otherType ? -1 : 1;
    
    String n1  = t1.getNummer();  if (n1  == null) n1  = "";
    String n2  = t2.getNummer(); if (n2  == null) n2  = "";
    String na1 = t1.getName();    if (na1 == null) na1 = "";
    String na2 = t2.getName();   if (na2 == null) na2 = "";

    // erst nach Nummer
    int numberCompare = n1.compareTo(n2);
    if (numberCompare != 0)
      return numberCompare;
    
    // Falls Nummer identisch/leer, dann nach Name
    return na1.compareTo(na2);
  }
  
  /**
   * Virtuelle Umsatz-Typ-Bean fuer "nicht zugeordnet".
   */
  public static class UmsatzTypUnassigned implements UmsatzTyp
  {
    @Override
    public Object getAttribute(String attribute) throws RemoteException
    {
      if("indented".equals(attribute) || "name".equals(attribute))
        return this.getName();
      
      return null;
    }
    
    @Override
    public GenericIterator getTopLevelList() throws RemoteException
    {
      return null;
    }

    @Override
    public void setParent(DBObjectNode arg0) throws RemoteException
    {
    }

    @Override
    public void addDeleteListener(Listener arg0) throws RemoteException
    {
    }

    @Override
    public void addStoreListener(Listener arg0) throws RemoteException
    {
    }

    @Override
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      return false;
    }

    @Override
    public String getAttributeType(String arg0) throws RemoteException
    {
      return null;
    }

    @Override
    public DBIterator getList() throws RemoteException
    {
      return null;
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    @Override
    public void load(String arg0) throws RemoteException
    {
    }

    @Override
    public void removeDeleteListener(Listener arg0) throws RemoteException
    {
    }

    @Override
    public void removeStoreListener(Listener arg0) throws RemoteException
    {
    }

    @Override
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name","indented","kommentar"};
    }

    @Override
    public String getID() throws RemoteException
    {
      // Damit koennten wir den Typ wenigstens anhand der ID wiedererkennen
      // Bei den Umsatz-Kategorien aus der Datenbank kann diese ID nicht vorkommen
      return "-1";
    }

    @Override
    public void transactionBegin() throws RemoteException
    {
    }

    @Override
    public void transactionCommit() throws RemoteException
    {
    }

    @Override
    public void transactionRollback() throws RemoteException
    {
    }

    @Override
    public void clear() throws RemoteException
    {
    }

    @Override
    public void delete() throws RemoteException, ApplicationException
    {
    }

    @Override
    public boolean isNewObject() throws RemoteException
    {
      return false;
    }

    @Override
    public void overwrite(DBObject arg0) throws RemoteException
    {
    }

    @Override
    public void store() throws RemoteException, ApplicationException
    {
    }

    @Override
    public GenericIterator getChildren() throws RemoteException
    {
      return null;
    }

    @Override
    public GenericObjectNode getParent() throws RemoteException
    {
      return null;
    }

    @Override
    public GenericIterator getPath() throws RemoteException
    {
      return null;
    }

    @Override
    public GenericIterator getPossibleParents() throws RemoteException
    {
      return null;
    }

    @Override
    public boolean hasChild(GenericObjectNode arg0) throws RemoteException
    {
      return false;
    }

    @Override
    public String getName() throws RemoteException
    {
      return "<" + i18n.tr("Nicht zugeordnet") + ">";
    }

    @Override
    public void setName(String name) throws RemoteException
    {
    }

    @Override
    public String getNummer() throws RemoteException
    {
      return null;
    }

    @Override
    public void setNummer(String nummer) throws RemoteException
    {
    }

    @Override
    public String getPattern() throws RemoteException
    {
      return null;
    }

    @Override
    public void setPattern(String pattern) throws RemoteException
    {
    }

    @Override
    public GenericIterator getUmsaetze() throws RemoteException
    {
      return getUmsaetze(-1);
    }

    @Override
    public GenericIterator getUmsaetze(Date von, Date bis) throws RemoteException
    {
      DBIterator list = UmsatzUtil.getUmsaetze();

      if (von != null)
        list.addFilter("datum >= ?", new Object[] {new java.sql.Date(von.getTime())});
      
      if (bis != null)
        list.addFilter("datum <= ?", new Object[] {new java.sql.Date(bis.getTime())});

      // Alle, die fest zugeordnet sind, sowieso nicht
      list.addFilter("umsatztyp_id is null");

      ArrayList result = new ArrayList();
      while (list.hasNext())
      {
        Umsatz u = (Umsatz) list.next();
        if (matches(u))
          result.add(u);
      }
      return PseudoIterator.fromArray((Umsatz[]) result.toArray(new Umsatz[result.size()]));
    }

    @Override
    public GenericIterator getUmsaetze(int days) throws RemoteException
    {
      Date start = null;
      if (days > 0)
      {
        long d = days * 24l * 60l * 60l * 1000l;
        start = DateUtil.startOfDay(new Date(System.currentTimeMillis() - d));
      }
      return getUmsaetze(start, null);
    }

    @Override
    public double getUmsatz() throws RemoteException
    {
      return getUmsatz(-1);
    }

    @Override
    public double getUmsatz(Date von, Date bis) throws RemoteException
    {
      double sum = 0.0d;
      GenericIterator i = getUmsaetze(von, bis);
      while (i.hasNext())
      {
        Umsatz u = (Umsatz) i.next();
        sum += u.getBetrag();
      }
      
      // Die Abfrage der Kinder koennen wir uns schenken, weil dieses Kategorie keine Kinder hat
      return sum;
    }

    @Override
    public double getUmsatz(int days) throws RemoteException
    {
      double sum = 0.0d;
      GenericIterator i = getUmsaetze(days);
      while (i.hasNext())
      {
        Umsatz u = (Umsatz) i.next();
        sum += u.getBetrag();
      }

      // Die Abfrage der Kinder koennen wir uns schenken, weil dieses Kategorie keine Kinder hat
      return sum;
    }

    @Override
    public boolean isRegex() throws RemoteException
    {
      return false;
    }

    @Override
    public int getTyp() throws RemoteException
    {
      return UmsatzTyp.TYP_EGAL;
    }

    @Override
    public void setTyp(int typ) throws RemoteException
    {
    }

    @Override
    public void setRegex(boolean regex) throws RemoteException
    {
    }

    @Override
    public boolean matches(Umsatz umsatz, boolean allowReassign) throws RemoteException, PatternSyntaxException
    {
      if (umsatz == null)
        return false;

      // Umsatz ist bereits fest zugeordnet. Dann koennen wir uns das rechenaufwaendige dynamische Zuordnen
      // komplett sparen
      boolean assigned = umsatz.isAssigned();
      if (assigned)
        return false;

      return umsatz.getUmsatzTyp() == null;
    }

    public boolean matches(de.willuhn.jameica.hbci.rmi.Umsatz umsatz) throws RemoteException
    {
      return matches(umsatz,false);
    }

    @Override
    public int[] getColor() throws RemoteException
    {
      return null;
    }

    @Override
    public void setColor(int[] rgb) throws RemoteException
    {
    }

    @Override
    public boolean isCustomColor() throws RemoteException
    {
      return false;
    }

    @Override
    public void setCustomColor(boolean b) throws RemoteException
    {
    }

    @Override
    public void setKommentar(String kommentar) throws RemoteException
    {
    }

    @Override
    public String getKommentar() throws RemoteException
    {
      return null;
    }

    @Override
    public Konto getKonto() throws RemoteException
    {
      return null;
    }

    @Override
    public void setKonto(Konto konto) throws RemoteException
    {
    }

    @Override
    public String getKontoKategorie() throws RemoteException
    {
      return null;
    }

    @Override
    public void setKontoKategorie(String kategorie) throws RemoteException
    {
    }

    @Override
    public int getFlags() throws RemoteException
    {
      return UmsatzTyp.FLAG_NONE;
    }

    @Override
    public void setFlags(int flags) throws RemoteException
    {
    }

    @Override
    public boolean hasFlag(int flag) throws RemoteException
    {
      return false;
    };
  }
}
