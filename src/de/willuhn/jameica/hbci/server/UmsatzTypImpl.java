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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.db.AbstractDBObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBObjectNode;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Umsatz-Typs.
 */
public class UmsatzTypImpl extends AbstractDBObjectNode implements UmsatzTyp, Duplicatable
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static transient Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private final static transient boolean ignorewhitespace = settings.getBoolean("search.ignore.whitespace",true);
  
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

      String comment = this.getKommentar();
      if (comment != null && comment.length() > 1000)
        throw new ApplicationException(i18n.tr("Die Notiz ist zu lang. Bitte geben Sie maximal 1000 Zeichen ein."));

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

    ////////////////////////////////////////////////////////////////////////
    // Zuordnung einer Kategorie zu einem Konto oder einer Kontogruppe
    final String kat = this.getKontoKategorie();
    final Konto k = this.getKonto();
    if (k != null || (kat != null && kat.length() > 0))
    {
      final Konto test = umsatz.getKonto();
      if (test != null) // sicher ist sicher
      {
        // Zuordnung zum Konto
        if (k != null && !k.equals(test))
          return false;
        
        // Zuordnung zur Gruppe
        if (kat != null && !kat.equals(test.getKategorie()))
          return false;
      }
    }
    //
    ////////////////////////////////////////////////////////////////////////

    String s = this.getPattern();
    if (s == null || s.trim().length() == 0)
      return false;

    String zweck = VerwendungszweckUtil.toString(umsatz,"");
    String name  = StringUtils.trimToEmpty(umsatz.getGegenkontoName());
    String name2 = StringUtils.trimToEmpty(umsatz.getGegenkontoName2());
    String kto   = StringUtils.trimToEmpty(umsatz.getGegenkontoNummer());
    String kom   = StringUtils.trimToEmpty(umsatz.getKommentar());
    String art   = StringUtils.trimToEmpty(umsatz.getArt());
    String purp  = StringUtils.trimToEmpty(umsatz.getPurposeCode());
    String ref   = StringUtils.trimToEmpty(umsatz.getCustomerRef());
    String e2eid = StringUtils.trimToEmpty(umsatz.getEndToEndId());
    String mid   = StringUtils.trimToEmpty(umsatz.getMandateId());
    String id    = StringUtils.trimToEmpty(umsatz.getID());
    
    if (!isRegex())
    {
      zweck = zweck.toLowerCase();
      name  = name.toLowerCase();
      name2 = name2.toLowerCase();
      kto   = kto.toLowerCase();
      kom   = kom.toLowerCase();
      art   = art.toLowerCase();
      purp  = purp.toLowerCase();
      ref   = ref.toLowerCase();
      e2eid = e2eid.toLowerCase();
      mid   = mid.toLowerCase();

      if (ignorewhitespace)
      {
        zweck = StringUtils.deleteWhitespace(zweck);
        name = StringUtils.deleteWhitespace(name); // BUGZILLA 1705 - auch im Namen koennen Leerzeichen sein
        name2 = StringUtils.deleteWhitespace(name2);
        s = StringUtils.deleteWhitespace(s);
      }

      String[] list = s.toLowerCase().split(","); // Wir beachten Gross-Kleinschreibung grundsaetzlich nicht
      for (String value : list)
      {
        String test = value.trim();
        if (zweck.indexOf(test) != -1 ||
            name.indexOf(test) != -1 ||
            name2.indexOf(test) != -1 ||
            kto.indexOf(test)  != -1 ||
            kom.indexOf(test) != -1 ||
            art.indexOf(test) != -1 ||
            purp.indexOf(test) != -1 ||
            ref.indexOf(test) != -1 ||
            e2eid.indexOf(test) != -1 ||
            mid.indexOf(test) != -1 ||
            id.equals(test))
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
      Matcher mName2 = pattern.matcher(name2);
      Matcher mKto = pattern.matcher(kto);
      Matcher mKom = pattern.matcher(kom);
      Matcher mArt = pattern.matcher(art);
      Matcher mPurp = pattern.matcher(purp);
      Matcher mRef = pattern.matcher(ref);
      Matcher mE2eid = pattern.matcher(e2eid);
      Matcher mMid = pattern.matcher(mid);
      Matcher mAll = pattern.matcher(name + " " + name2 + " " + kto + " " + zweck + " " + kom + " " + art + " " + purp + " " + e2eid + " " + mid + " " + ref);

      return (mAll.matches()    ||
              mZweck.matches()  ||
              mName.matches()   ||
              mName2.matches()  ||
              mKto.matches()    ||
              mKom.matches()    ||
              mArt.matches()    ||
              mPurp.matches()   ||
              mRef.matches()    ||
              mE2eid.matches()  ||
              mMid.matches()
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

  @Override
  public void setKommentar(String kommentar) throws RemoteException
  {
    setAttribute("kommentar", kommentar);
    
  }

  @Override
  public String getKommentar() throws RemoteException
  {
    return (String) getAttribute("kommentar");
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
    if ("konto_id".equals(arg0))
      return getKonto();

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
   * @see de.willuhn.datasource.db.AbstractDBObject#overwrite(de.willuhn.datasource.rmi.DBObject)
   */
  public void overwrite(DBObject object) throws RemoteException
  {
    // Muessen wir ueberschreiben, weil wir fuer das Konto hier eine
    // Sonderbehandlung machen. Wuerden wir das hier nicht machen,
    // haetten wir nach dem Overwrite ploetzlich ein Konto-Objekt
    // statt der Konto-ID in den Properties. Das liegt eigentlich
    // nur daran, weil wir "konto_id" hier nicht als Foreign-Key
    // (wegen dem Cache) deklariert haben - bei "getAttribute("konto_id")"
    // aber trotzdem das Objekt (wegen KontoColumn) zurueckliefern.
    super.overwrite(object);
    
    // Jetzt ersetzen wir wieder das Konto-Objekt gegen die ID
    this.setKonto(((AbstractHibiscusTransferImpl)object).getKonto());
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
    list.setOrder("order by COALESCE(nummer,''), name");
    return list;
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObjectNode#getTopLevelList()
   */
  public GenericIterator getTopLevelList() throws RemoteException
  {
    DBIterator list = (DBIterator) super.getTopLevelList();
    list.setOrder("order by COALESCE(nummer,''), name");
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
    di.setOrder("order by COALESCE(nummer,''),name");
    return di;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  @Override
  public Object duplicate() throws RemoteException
  {
    UmsatzTyp t = (UmsatzTyp) this.getService().createObject(UmsatzTyp.class,null);
    t.setColor(this.getColor());
    t.setCustomColor(this.isCustomColor());
    t.setName(this.getName());
    t.setNummer(this.getNummer());
    t.setParent((DBObjectNode) this.getParent());
    t.setPattern(this.getPattern());
    t.setRegex(this.isRegex());
    t.setKommentar(this.getKommentar());
    t.setTyp(this.getTyp());
    t.setKonto(this.getKonto());
    t.setKontoKategorie(this.getKontoKategorie());
    t.setFlags(this.getFlags());
    return t;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getKonto()
   */
  @Override
  public Konto getKonto() throws RemoteException
  {
    Integer i = (Integer) super.getAttribute("konto_id");
    if (i == null)
      return null; // Kein Konto zugeordnet
   
    Cache cache = Cache.get(Konto.class,true);
    return (Konto) cache.get(i);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  @Override
  public void setKonto(Konto konto) throws RemoteException
  {
    final Integer id = (konto == null || konto.getID() == null) ? null : new Integer(konto.getID());
    setAttribute("konto_id",id);
    
    // Eine Zuordnung kann nur zu Konto ODER Kategorie moeglich sein - nicht beides gleichzeitig
    if (id != null)
      this.setKontoKategorie(null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getKontoKategorie()
   */
  @Override
  public String getKontoKategorie() throws RemoteException
  {
    return (String) this.getAttribute("konto_kategorie");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setKontoKategorie(java.lang.String)
   */
  @Override
  public void setKontoKategorie(String kategorie) throws RemoteException
  {
    this.setAttribute("konto_kategorie",kategorie);
    
    // Eine Zuordnung kann nur zu Konto ODER Kategorie moeglich sein - nicht beides gleichzeitig
    if (kategorie != null && kategorie.length() > 0)
      this.setAttribute("konto_id",null);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Flaggable#hasFlag(int)
   */
  @Override
  public boolean hasFlag(int flag) throws RemoteException
  {
    return (this.getFlags() & flag) == flag;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Flaggable#getFlags()
   */
  @Override
  public int getFlags() throws RemoteException
  {
    Integer i = (Integer) this.getAttribute("flags");
    return i == null ? Konto.FLAG_NONE : i.intValue();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Flaggable#setFlags(int)
   */
  @Override
  public void setFlags(int flags) throws RemoteException
  {
    if (flags < 0)
      return; // ungueltig
    
    this.setAttribute("flags",new Integer(flags));
  }
}
