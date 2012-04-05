/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzTypImpl.java,v $
 * $Revision: 1.66 $
 * $Date: 2012/04/05 21:44:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.db.AbstractDBObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Umsatz-Typs.
 */
public class UmsatzTypImpl extends AbstractDBObjectNode implements UmsatzTyp
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private final static transient Map<String,Pattern> patternCache = new HashMap<String,Pattern>();

  /**
   * ct.
   * 
   * @throws RemoteException
   */
  public UmsatzTypImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "umsatztyp";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      String name = getName();
      if (name == null || name.length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
      
      String pattern = getPattern();
      if (pattern != null && pattern.length() > 0)
      {
        if (pattern.length() > MAXLENGTH_PATTERN)
          throw new ApplicationException(i18n.tr("Bitte geben Sie maximal {0} Zeichen als Suchbegriff ein.",Integer.toString(MAXLENGTH_PATTERN)));
        
        if (isRegex())
        {
          try
          {
            Pattern.compile(pattern);
          }
          catch (PatternSyntaxException pse)
          {
            throw new ApplicationException(i18n.tr("Regulärer Ausdruck ungültig: {0}",pse.getDescription()));
          }
        }
      }

      if (isCustomColor() && (getColor() == null || getColor().length != 3))
        throw new ApplicationException("Wählen Sie bitte eine benutzerdefinierte Farbe aus");

    }
    catch (RemoteException e)
    {
      Logger.error("error while insert check", e);
      throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatz-Typs."));
    }
    super.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsaetze()
   */
  public GenericIterator getUmsaetze() throws RemoteException
  {
    return getUmsaetze(-1);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsaetze(int)
   */
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

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsaetze(Date, Date)
   */
  public GenericIterator getUmsaetze(Date von, Date bis) throws RemoteException
  {
    DBIterator list = UmsatzUtil.getUmsaetze();

    if (von != null)
      list.addFilter("datum >= ?", new Object[] {new java.sql.Date(von.getTime())});
    
    if (bis != null)
      list.addFilter("datum <= ?", new Object[] {new java.sql.Date(bis.getTime())});

    if (this.isNewObject()) // Neuer Umsatztyp. Der hat noch keine ID
      list.addFilter("umsatztyp_id is null");
    else
      // Gibts schon. Also koennen wir auch nach festzugeordneten suchen
      list.addFilter("(umsatztyp_id is null or umsatztyp_id=" + this.getID() + ")");

    ArrayList result = new ArrayList();
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      if (u.isAssigned() || matches(u)) // entweder fest zugeordnet oder passt via Suchfilter
        result.add(u);
    }
    return PseudoIterator.fromArray((Umsatz[]) result.toArray(new Umsatz[result.size()]));
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getName()
   */
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException
  {
    this.setAttribute("name", name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getNummer()
   */
  public String getNummer() throws RemoteException
  {
    return (String) getAttribute("nummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setNummer(java.lang.String)
   */
  public void setNummer(String nummer) throws RemoteException
  {
    this.setAttribute("nummer", nummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getPattern()
   */
  public String getPattern() throws RemoteException
  {
    return (String) getAttribute("pattern");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setPattern(java.lang.String)
   */
  public void setPattern(String pattern) throws RemoteException
  {
    setAttribute("pattern", pattern);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#matches(de.willuhn.jameica.hbci.rmi.Umsatz)
   */
  public boolean matches(Umsatz umsatz) throws RemoteException
  {
    return matches(umsatz,false);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#matches(de.willuhn.jameica.hbci.rmi.Umsatz, boolean)
   */
  public boolean matches(Umsatz umsatz, boolean allowReassign) throws RemoteException
  {
    // Wenn der Umsatz fest zugeordnet ist, duerfen wir nicht nach Begriffen suchen
    // Dann gilt er nur als zugeordnet, wenn es der gleiche Typ ist
    if (!allowReassign && umsatz.isAssigned())
    {
      String id = this.getID();
      if (id == null)
        return false;
      UmsatzTyp typ = umsatz.getUmsatzTyp();
      return typ.equals(this);
    }
    
    // BUGZILLA 614 - wenn die Kategorie gar nicht passt, koennen wir gleich abbrechen
    double betrag = umsatz.getBetrag();
    int typ       = this.getTyp();
    
    if (betrag != 0.0d)
    {
      // Betrag kleiner als null aber als Einnahme kategorisiert ODER
      // Betrag groesser als null aber als Ausgabe kategorisiert
      if ((betrag < 0.0d && (typ == UmsatzTyp.TYP_EINNAHME)) || (betrag > 0.0d && (typ == UmsatzTyp.TYP_AUSGABE)))
        return false;
    }

    String s = this.getPattern();
    if (s == null || s.trim().length() == 0)
      return false;

    String zweck = VerwendungszweckUtil.toString(umsatz);
    String name  = umsatz.getGegenkontoName();
    String kto   = umsatz.getGegenkontoNummer();
    String kom   = umsatz.getKommentar();
    
    if (name == null) name = "";
    if (kto  == null) kto = "";
    if (kom  == null) kom = "";

    if (!isRegex())
    {
      zweck = zweck.toLowerCase();
      name  = name.toLowerCase();
      kto   = kto.toLowerCase();
      kom   = kom.toLowerCase();

      String[] list = s.toLowerCase().split(","); // Wir beachten Gross-Kleinschreibung grundsaetzlich nicht

      for (int i=0;i<list.length;++i)
      {
        String test = list[i].trim();
        if (zweck.indexOf(test) != -1 ||
            name.indexOf(test) != -1 ||
            kto.indexOf(test)  != -1 ||
            kom.indexOf(test) != -1)
        {
          return true;
        }
      }
      return false;
    }
    
    
    Pattern pattern = patternCache.get(s);

    try
    {
      if (pattern == null)
      {
        pattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
        patternCache.put(s,pattern);
      }
      
      Matcher mZweck = pattern.matcher(zweck);
      Matcher mName = pattern.matcher(name);
      Matcher mKto = pattern.matcher(kto);
      Matcher mKom = pattern.matcher(kom);
      Matcher mAll = pattern.matcher(name + " " + kto + " " + zweck + " " + kom);

      return (mZweck.matches() ||
              mName.matches() ||
              mKto.matches()  ||
              mKom.matches()  ||
              mAll.matches()
             );
    }
    catch (Exception e)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Ungültiger regulärer Ausdruck \"{0}\": {1}", new String[]{s,e.getMessage()}),StatusBarMessage.TYPE_ERROR));
      Logger.error("invalid regex pattern: " + e.getMessage(),e);
      return false;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#isRegex()
   */
  public boolean isRegex() throws RemoteException
  {
    Integer i = (Integer) getAttribute("isregex");
    return i != null && i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setRegex(boolean)
   */
  public void setRegex(boolean regex) throws RemoteException
  {
    setAttribute("isregex", new Integer(regex ? 1 : 0));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsatz()
   */
  public double getUmsatz() throws RemoteException
  {
    return getUmsatz(-1);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsatz(Date, Date)
   */
  public double getUmsatz(Date von, Date bis) throws RemoteException
  {
    // Das kann man mal ueber einen SQL-Join schneller machen
    // Ne, kann man doch nicht, weil jeder Umsatz noch via matches()
    // auf Treffer mit regulaeren Ausdruecken geprueft wird.
    // In SQL ist das viel zu aufwaendig
    double sum = 0.0d;
    GenericIterator i = getUmsaetze(von, bis);
    while (i.hasNext())
    {
      Umsatz u = (Umsatz) i.next();
      sum += u.getBetrag();
    }
    
    GenericIterator children = this.getChildren();
    while (children.hasNext())
    {
      UmsatzTyp t = (UmsatzTyp) children.next();
      sum += t.getUmsatz(von,bis);
    }
    return sum;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsatz(int)
   */
  public double getUmsatz(int days) throws RemoteException
  {
    // Das kann man mal ueber einen SQL-Join schneller machen
    // Ne, kann man doch nicht, weil jeder Umsatz noch via matches()
    // auf Treffer mit regulaeren Ausdruecken geprueft wird.
    // In SQL ist das viel zu aufwaendig
    double sum = 0.0d;
    GenericIterator i = getUmsaetze(days);
    while (i.hasNext())
    {
      Umsatz u = (Umsatz) i.next();
      sum += u.getBetrag();
    }

    GenericIterator children = this.getChildren();
    while (children.hasNext())
    {
      UmsatzTyp t = (UmsatzTyp) children.next();
      sum += t.getUmsatz(days);
    }
    return sum;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    // BUGZILLA 554
    if ("nummer-int".equals(arg0))
    {
      String n = getNummer();
      if (n == null || n.length() == 0)
        return null;
      try
      {
        Integer i = new Integer(n);
        return i;
      }
      catch (Exception e)
      {
        Logger.warn("unable to parse " + n + " as number");
        return null;
      }
    }
    
    if ("umsatz".equals(arg0))
      return new Double(getUmsatz());

    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    if (this.isNewObject())
      return;

    patternCache.clear(); // Pattern-Cache leeren
    Cache.clear(UmsatzTyp.class); // Cache loeschen

    // Ueberschrieben, weil wir beim Loeschen pruefen muessen,
    // ob wir irgendwelchen Umsaetzen zugeordnet sind und
    // diese bei der Gelegenheit entfernen muessen.

    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("umsatztyp_id = " + this.getID());
    if (!list.hasNext())
    {
      // Ne, keine Umsaetze zugeordnet. Dann koennen wir getrost loeschen.
      super.delete();
      return;
    }

    try
    {
      // Wir haben zugeordnete Umsaetze. Dort muessen wir uns entfernen
      Logger.info("removing assignments to existing umsatz objects");
      transactionBegin();
      while (list.hasNext())
      {
        Umsatz u = (Umsatz) list.next();
        u.setUmsatzTyp(null);
        u.store();
      }
      super.delete();
      transactionCommit();
    }
    catch (RemoteException e)
    {
      this.transactionRollback();
      throw e;
    }
    catch (ApplicationException e2)
    {
      this.transactionRollback();
      throw e2;
    }
  }

  /**
   * Ueberschrieben, um den Umsatztyp-Cache zu aktualisieren.
   * 
   * @see de.willuhn.datasource.db.AbstractDBObject#store()
   */
  public void store() throws RemoteException, ApplicationException
  {
    super.store();
    patternCache.clear(); // Pattern-Cache leeren
    Cache.clear(UmsatzTyp.class); // Cache loeschen
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getColor()
   */
  public int[] getColor() throws RemoteException
  {
    String color = (String) this.getAttribute("color");
    if (color == null || color.length() == 0)
      return null;
    
    try
    {
      int[] rgb = new int[3];
      String[] values = color.split(",");
      for (int i=0;i<3;++i)
      {
        int v = Integer.parseInt(values[i]);
        if (v < 0 || v > 255)
          throw new IllegalArgumentException("invalid color value: " + v);
        rgb[i] = v;
      }
      return rgb;
    }
    catch (Exception e)
    {
      Logger.error("invalid color code: " + color + ", ignoring",e);
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setColor(int[])
   */
  public void setColor(int[] rgb) throws RemoteException
  {
    if (rgb == null || rgb.length != 3)
    {
      this.setAttribute("color",null);
      return;
    }
    for (int i=0;i<3;++i)
    {
      if (rgb[i] < 0 || rgb[i] > 255)
      {
        Logger.error("invalid color value: " + rgb[i]);
        return;
      }
    }
    this.setAttribute("color",rgb[0] + "," + rgb[1] + "," + rgb[2]);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getTyp()
   */
  public int getTyp() throws RemoteException
  {
    Integer i = (Integer) getAttribute("umsatztyp");
    return i == null ? UmsatzTyp.TYP_EGAL : i.intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setTyp(int)
   */
  public void setTyp(int typ) throws RemoteException
  {
    setAttribute("umsatztyp",new Integer(typ));
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObjectNode#getPossibleParents()
   */
  public GenericIterator getPossibleParents() throws RemoteException
  {
    DBIterator list = (DBIterator) super.getPossibleParents();
    list.setOrder("order by nummer, name");
    return list;
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObjectNode#getTopLevelList()
   */
  public GenericIterator getTopLevelList() throws RemoteException
  {
    DBIterator list = (DBIterator) super.getTopLevelList();
    list.setOrder("order by nummer, name");
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#isCustomColor()
   */
  public boolean isCustomColor() throws RemoteException
  {
    Integer i = (Integer) getAttribute("customcolor");
    return i != null && i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setCustomColor(boolean)
   */
  public void setCustomColor(boolean b) throws RemoteException
  {
    setAttribute("customcolor", new Integer(b ? 1 : 0));
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObjectNode#getChildren()
   */
  public GenericIterator getChildren() throws RemoteException
  {
    GenericIterator i = super.getChildren();
    if (this.isNewObject() || !(i instanceof DBIterator))
      return i;
    
    DBIterator di = (DBIterator) i;
    di.setOrder("order by nummer,name");
    return di;
  }
  
  
}

/*******************************************************************************
 * $Log: UmsatzTypImpl.java,v $
 * Revision 1.66  2012/04/05 21:44:18  willuhn
 * @B BUGZILLA 1219
 *
 * Revision 1.65  2012/01/02 22:32:20  willuhn
 * @N BUGZILLA 1170
 *
 * Revision 1.64  2011-07-20 15:41:36  willuhn
 * @N Neue Funktion UmsatzTyp#matches(Umsatz,boolean allowReassign) - normalerweise liefert die Funktion ohne das Boolean false, wenn der Umsatz bereits manuell einer anderen Kategorie zugeordnet ist. Andernfalls kaeme es hier ja - zumindest virtuell - zu einer Doppel-Zuordnung. Da "UmsatzList" jedoch fuer den Suchbegriff (den man oben eingeben kann) intern on-the-fly einen UmsatzTyp erstellt, mit dem die Suche erfolgt, wuerden hier bereits fest zugeordnete Umsaetze nicht mehr gefunden werden. Daher die neue Funktion.
 *
 * Revision 1.63  2011-04-27 11:07:02  willuhn
 * @B Umsaetze, die bereits fest zugeordnet sind, duerfen nicht gematcht werden
 *
 * Revision 1.62  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.61  2011-01-10 22:29:24  willuhn
 * @B BUGZILLA 977
 *
 * Revision 1.60  2010-12-14 12:47:59  willuhn
 * @B Cache wurde nicht immer korrekt aktualisiert, was dazu fuehren konnte, dass sich das Aendern/Loeschen/Anlegen von Kategorien erst nach 10 Sekunden auswirkte und bis dahin Umsaetze der Kategorie "nicht zugeordnet" zugewiesen wurden, obwohl sie in einer Kategorie waren
 *
 * Revision 1.59  2010-12-14 11:54:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.58  2010-12-07 11:10:33  willuhn
 * @C Verwendungszweck beim Matching mergen
 * @N Pattern-Cache
 *
 * Revision 1.57  2010-11-24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 * Revision 1.56  2010-08-26 12:53:08  willuhn
 * @N Cache nur befuellen, wenn das explizit gefordert wird. Andernfalls wuerde der Cache u.U. unnoetig gefuellt werden, obwohl nur ein Objekt daraus geloescht werden soll
 *
 * Revision 1.55  2010-08-26 11:31:23  willuhn
 * @N Neuer Cache. In dem werden jetzt die zugeordneten Konten von Auftraegen und Umsaetzen zwischengespeichert sowie die Umsatz-Kategorien. Das beschleunigt das Laden der Umsaetze und Auftraege teilweise erheblich
 *
 * Revision 1.54  2010/06/02 15:32:03  willuhn
 * @N Unique-Constraint auf Spalte "name" in Tabelle "umsatztyp" entfernt. Eine Kategorie kann jetzt mit gleichem Namen beliebig oft auftreten
 * @N Auswahlbox der Oberkategorie in Einstellungen->Umsatz-Kategorien zeigt auch die gleiche Baumstruktur wie bei der Zuordnung der Kategorie in der Umsatzliste
 *
 * Revision 1.53  2010/04/11 20:56:53  willuhn
 * @N BUGZILLA #846
 *
 * Revision 1.52  2010/03/05 20:16:44  willuhn
 * @B ClassCastException - siehe BUGZILLA 686
 *
 * Revision 1.51  2010/03/05 17:54:13  willuhn
 * @C Umsatz-Kategorien nach Nummer und anschliessend nach Name sortieren
 ******************************************************************************/