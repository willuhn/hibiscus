/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierte Auswahlbox fuer die Umsatz-Kategorie.
 */
public class UmsatzTypInput extends SelectInput
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private List<UmsatzTyp> all  = null;
  private List<UmsatzTyp> root = new ArrayList<UmsatzTyp>();

  /**
   * ct.
   * @param preselected der vorselectierte Umsatz-Typ.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  public UmsatzTypInput(UmsatzTyp preselected, int typ) throws RemoteException
  {
    this(preselected,null,typ);
  }

  /**
   * ct.
   * @param preselected der vorselectierte Umsatz-Typ.
   * @param skip einzelner Umsatz-Typ, der nicht angeboten werden soll.
   * Damit ist es zum Beispiel moeglich, eine Endlos-Rekursion zu erzeugen,
   * wenn ein Parent ausgewaehlt werden soll, der User aber die Kategorie
   * sich selbst als Parent zuordnet.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  public UmsatzTypInput(UmsatzTyp preselected, UmsatzTyp skip, int typ) throws RemoteException
  {
    super((List) null, preselected);
    this.setList(init(skip,typ));
    this.setAttribute("name");
    this.setName(i18n.tr("Umsatz-Kategorie"));
    this.setPleaseChoose(i18n.tr("<Keine Kategorie>"));
    refreshComment();
    
    // Betrag aktualisieren
    this.addListener(new Listener() {
    
      public void handleEvent(Event event)
      {
        refreshComment();
      }
    
    });
  }
  
  /**
   * Initialisiert die Liste der anzuzeigenden Kategorien.
   * @param skip zu ueberspringende Kategorie.
   * @param typ der Kategorie-Typ.
   * @return korrigierte Liste.
   * @throws RemoteException
   */
  private List<UmsatzTyp> init(UmsatzTyp skip, int typ) throws RemoteException
  {
    this.all = PseudoIterator.asList(UmsatzTypUtil.getAll());
    
    // Wir ermitteln erstmal nur die Root-Elemente und verarbeiten die dann alle einzeln
    // Im Prinzip koennte man das alles auch bequemer ueber die passenden Methoden von UmsatzTypUtil
    // und GenericObjectNode machen. Das wuerde aber rekursiv eine ganze Reihe von SQL-Queries
    // ausloesen. Bei 50 verschachtelten Kategorien koennen da schnell 200 SQL-Abfragen zusammenkommen,
    // die jedesmal aufgerufen werden, wenn die Selectbox eingeblendet wird. Daher laden wir mit einem
    // einzelnen Query alle Kategorien und erzeugen den Baum dann komplett im Speicher. Das ist erheblich
    // schneller.
    for (UmsatzTyp t:this.all)
    {
      String pid = this.getParentId(t);
      if (pid == null || this.find(pid) == null)
        this.root.add(t);
    }


    List<UmsatzTyp> result = new ArrayList<UmsatzTyp>();

    // Jetzt ueber die Root-Elemente iterieren
    for (UmsatzTyp t:this.root)
    {
      add(t,skip,result,typ);
    }
    return result;
  }
  
  /**
   * Fuegt die Kategorie und dessen Kinder zur Ergebnisliste hinzu, insofern sie zum Typ passen.
   * @param t die Kategorie.
   * @param skip optionale Ausschluss-Kategorie. Wenn t=skip ist, wird nichts zur Ergebnisliste hinzugefuegt.
   * @param result die Ergebnisliste, zu der die Kategorien hinzugefuegt werden sollen.
   * @param typ der gesuchte Typ, on dem die Kategorien sein muessen.
   * @throws RemoteException
   */
  private void add(UmsatzTyp t, UmsatzTyp skip, List<UmsatzTyp> result, int typ) throws RemoteException
  {
    if (skip != null && skip.equals(t))
      return;
    
    // Wir filtern hier zwei Faelle:
    
    // a) typ == TYP_EGAL -> es wird nichts gefiltert
    // b) typ != TYP_EGAL -> es werden nur die angezeigt, bei denen TYP_EGAL oder Typ passt
    
    int ti = t.getTyp();
    if (typ == UmsatzTyp.TYP_EGAL || (ti == UmsatzTyp.TYP_EGAL || ti == typ))
    {
      result.add(t);
      
      List<UmsatzTyp> children = this.getChildren(t);
      for (UmsatzTyp c:children)
      {
        add(c,skip,result,typ);
      }
    }
  }
  
  /**
   * Liefert alle direkten Kinder der Kategorie.
   * @param t die Kategorie.
   * @return die Liste der Kinder. Niemals NULL sondern hoechstens eine leere Liste.
   * @throws RemoteException
   */
  private List<UmsatzTyp> getChildren(UmsatzTyp t) throws RemoteException
  {
    List<UmsatzTyp> result = new ArrayList<UmsatzTyp>();
    if (t == null || this.all == null)
      return result;
    
    String id = t.getID();
    if (id == null) // Neue ungespeicherte Kategorie?
      return result;
    
    for (UmsatzTyp c:this.all)
    {
      String pid = this.getParentId(c);
      if (pid != null && pid.equals(id))
        result.add(c);
    }

    return result;
  }
  
  /**
   * Liefert die Parent-ID.
   * @param t die Kategorie.
   * @return die Parent-ID oder NULL, wenn keine existiert.
   * @throws RemoteException
   */
  private String getParentId(UmsatzTyp t) throws RemoteException
  {
    Object o = t.getAttribute("parent_id");
    if (o == null)
      return null;
    
    if ((o instanceof GenericObject))
      return ((GenericObject)o).getID();
    
    return o.toString();
  }
  
  /**
   * Sucht die Kategorie mit der angegebenen ID in der Liste aller Kategorien.
   * @param id die ID.
   * @return die Kategorie oder NULL, wenn sie nicht existiert.
   * @throws RemoteException
   */
  private UmsatzTyp find(String id) throws RemoteException
  {
    if (this.all == null || id == null)
      return null;
    
    for (UmsatzTyp t:this.all)
    {
      if (t.getID().equals(id))
        return t;
    }
    
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#format(java.lang.Object)
   */
  protected String format(Object bean)
  {
    String name = super.format(bean);
    try
    {
      UmsatzTyp t = (UmsatzTyp) bean;
      
      int depth = 0;
      
      // Maximal 100 Level in der Tiefe
      for (int i=0;i<100;++i)
      {
        String pid = this.getParentId(t);
        if (pid == null)
          break; // Oben angekommen

        t = find(pid);
        if (t == null) // Parent gibts nicht mehr, oben angekommen
          break;
        
        // Ansonsten naechstes Level laden
        depth++;
      }
      
      for (int i=0;i<depth;++i)
      {
        name = "    " + name;
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to indent category name",e);
    }
    return name;
  }
  
  /**
   * Aktualisiert den Kommentar.
   */
  private void refreshComment()
  {
    try
    {
      UmsatzTyp ut = (UmsatzTyp) getValue();
      if (ut == null)
      {
        setComment("");
        return;
      }
      
      Calendar cal = Calendar.getInstance();
      setComment(i18n.tr("Umsatz im laufenden Monat: {0} {1}", new String[]{HBCI.DECIMALFORMAT.format(ut.getUmsatz(cal.get(Calendar.DAY_OF_MONTH))), HBCIProperties.CURRENCY_DEFAULT_DE}));
    }
    catch (Exception e)
    {
      Logger.error("unable to refresh umsatz",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren des Umsatzes"), StatusBarMessage.TYPE_ERROR));
    }
  }
}
