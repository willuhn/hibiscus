/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzTypImpl.java,v $
 * $Revision: 1.43 $
 * $Date: 2008/08/29 16:46:24 $
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.db.AbstractDBObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Umsatz-Typs.
 */
public class UmsatzTypImpl extends AbstractDBObjectNode implements UmsatzTyp
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

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
      if (isRegex() && pattern != null && pattern.length() > 0)
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

      // Wir pruefen, ob es bereits eine Kategorie mit diesem Namen gibt
      // willuhn: In der Datenbank ist das Feld zwar bereits als UNIQUE
      // definiert. Damit wir beim Speichern aber nicht den SQL-Code parsen
      // muessen (und der vermutlich bei einer anderen Datenbank anders lautet)
      // machen wir die Pruefung vor dem Speichern trotzdem noch manuell.
      // Beim Speichern ist das auch nicht zeitkritisch.
      DBIterator list = getService().createList(UmsatzTyp.class);
      while (list.hasNext())
      {
        UmsatzTyp other = (UmsatzTyp) list.next();
        if (other.equals(this))
          continue; // Das sind wir selbst
        if (name.equals(other.getName()))
          throw new ApplicationException(i18n.tr("Es existiert bereits eine Kategorie mit dieser Bezeichnung"));
      }

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
      start = HBCIProperties.startOfDay(new Date(System.currentTimeMillis() - d));
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
      list.addFilter("valuta >= ?", new Object[] {new java.sql.Date(von.getTime())});
    
    if (bis != null)
      list.addFilter("valuta <= ?", new Object[] {new java.sql.Date(bis.getTime())});

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
    if (s == null || s.length() == 0)
      return false;

    s = s.toLowerCase(); // Wir beachten Gross-Kleinschreibung grundsaetzlich nicht

    String vwz1 = umsatz.getZweck();
    String vwz2 = umsatz.getZweck2();
    String name = umsatz.getGegenkontoName();
    String kto = umsatz.getGegenkontoNummer();
    String kom = umsatz.getKommentar();

    if (vwz1 == null)
      vwz1 = "";
    if (vwz2 == null)
      vwz2 = "";
    if (name == null)
      name = "";
    if (kto == null)
      kto = "";
    if (kom == null)
      kom = "";

    if (isRegex())
    {
      Pattern pattern = null;

      pattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
      Matcher mVwz1 = pattern.matcher(vwz1);
      Matcher mVwz2 = pattern.matcher(vwz2);
      Matcher mName = pattern.matcher(name);
      Matcher mKto = pattern.matcher(kto);
      Matcher mKom = pattern.matcher(kom);

      return (mVwz1.matches() ||
              mVwz2.matches() ||
              mName.matches() ||
              mKto.matches()  ||
              mKom.matches()
             );
    }

    vwz1 = vwz1.toLowerCase();
    vwz2 = vwz2.toLowerCase();
    name = name.toLowerCase();
    kto = kto.toLowerCase();
    kom = kom.toLowerCase();

    return (vwz1.indexOf(s) != -1 || 
            vwz2.indexOf(s) != -1 ||
            name.indexOf(s) != -1 ||
            kto.indexOf(s)  != -1 ||
            kom.indexOf(s) != -1
           );
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

    if (arg0 != null && arg0.startsWith("umsatz:"))
    {
      // TODO: Beheben! Das ist haesslich!
      try
      {
        String[] s = arg0.split(":");
        return new Double(getUmsatz(Integer.parseInt(s[1])));
      }
      catch (Exception e)
      {
        Logger.error("unable to parse number of days: " + arg0);
      }
    }
    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    if (this.isNewObject())
      return;

    UmsatzImpl.UMSATZTYP_CACHE.remove(this.getID());

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
    UmsatzImpl.UMSATZTYP_CACHE.put(this.getID(), this);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getColor()
   */
  public int[] getColor() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setColor(int[])
   */
  public void setColor(int[] rgb) throws RemoteException
  {
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

}

/*******************************************************************************
 * $Log: UmsatzTypImpl.java,v $
 * Revision 1.43  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.42  2008/08/08 08:57:14  willuhn
 * @N BUGZILLA 614
 *
 * Revision 1.41  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.40  2008/02/27 10:31:20  willuhn
 * @B Bug 554
 *
 * Revision 1.39  2008/02/26 01:01:16  willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp. die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet werden muss
 *
 * Revision 1.38  2007/08/24 22:22:00  willuhn
 * @N Regulaere Ausdruecke vorm Speichern testen
 *
 * Revision 1.37  2007/08/07 23:54:15  willuhn
 * @B Bug 394 - Erster Versuch. An einigen Stellen (z.Bsp. konto.getAnfangsSaldo) war ich mir noch nicht sicher. Heiner?
 *
 * Revision 1.36  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.35  2007/03/22 14:23:56  willuhn
 * @N Redesign Kategorie-Tree - ist jetzt erheblich schneller und enthaelt eine Pseudo-Kategorie "Nicht zugeordnet"
 *
 * Revision 1.34  2007/03/16 12:55:26  jost
 * Bugfix: Kategorie wurde in der Kontenliste nicht korrekt angezeigt.
 *
 * Revision 1.33  2007/03/12 13:58:56  willuhn
 * @C Eindeutigkeit des Namens trotz UNIQUE-Key vorher in insertCheck pruefen - das spart das Parsen der SQLException
 *
 * Revision 1.32  2007/03/10 07:18:50  jost
 * Neu: Nummer fÃ¼r die Sortierung der Umsatz-Kategorien
 * Umsatzkategorien editierbar gemacht (Verlagerung vom Code -> DB)
 *
 * Revision 1.31  2007/03/08 18:56:39  willuhn
 * @N Mehrere Spalten in Kategorie-Baum
 *
 * Revision 1.30  2007/03/06 20:06:56  jost
 * Neu: Umsatz-Kategorien-Ãœbersicht
 * Revision 1.29 2006/12/29 14:28:47 willuhn
 * 
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 * 
 * Revision 1.28 2006/11/30 23:48:40 willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 * 
 * Revision 1.27 2006/11/29 00:40:37 willuhn
 * @N Keylistener in Umsatzlist nur dann ausfuehren, wenn sich wirklich etwas
 *    geaendert hat
 * @C UmsatzTyp.matches matcht jetzt bei leeren Pattern nicht mehr
 * 
 * Revision 1.26 2006/11/23 23:24:17 willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 * 
 * Revision 1.25 2006/11/23 17:25:38 willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 * 
 * Revision 1.24 2006/08/25 10:13:43 willuhn
 * @B Fremdschluessel NICHT mittels PreparedStatement, da die sonst gequotet und
 *    von McKoi nicht gefunden werden. BUGZILLA 278
 * 
 * Revision 1.23 2006/08/23 09:45:14 willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 * 
 * Revision 1.22 2006/05/22 12:54:52 willuhn
 * @N bug 235 (thanks to Markus)
 * 
 * Revision 1.21 2006/04/25 23:25:09 willuhn
 * @N bug 81
 * 
 * Revision 1.20 2006/04/03 21:39:07 willuhn
 * @N UmsatzChart
 * 
 * Revision 1.19 2005/12/30 00:14:45 willuhn
 * @N first working pie charts
 * 
 * Revision 1.18 2005/12/29 01:22:11 willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 * 
 * Revision 1.17 2005/12/20 00:03:26 willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 * 
 * Revision 1.16 2005/12/13 00:06:31 willuhn
 * @N UmsatzTyp erweitert
 * 
 * Revision 1.15 2005/12/05 20:16:15 willuhn
 * @N Umsatz-Filter Refactoring
 * 
 * Revision 1.14 2005/12/05 17:20:40 willuhn
 * @N Umsatz-Filter Refactoring
 * 
 * Revision 1.13 2005/11/18 00:43:29 willuhn
 * @B bug 21
 * 
 * Revision 1.12 2005/11/14 23:47:20 willuhn
 * @N added first code for umsatz categories
 * 
 * Revision 1.11 2005/05/30 22:55:27 web0 *** empty log message ***
 * 
 * Revision 1.10 2005/02/28 16:28:24 web0
 * @N first code for "Sammellastschrift"
 * 
 * Revision 1.9 2004/11/12 18:25:07 willuhn *** empty log message ***
 * 
 * Revision 1.8 2004/08/18 23:13:51 willuhn
 * @D Javadoc
 * 
 * Revision 1.7 2004/07/25 17:15:06 willuhn
 * @C PluginLoader is no longer static
 * 
 * Revision 1.6 2004/07/23 15:51:44 willuhn
 * @C Rest des Refactorings
 * 
 * Revision 1.5 2004/07/21 23:54:30 willuhn *** empty log message ***
 * 
 * Revision 1.4 2004/07/13 22:20:37 willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 * 
 * Revision 1.3 2004/06/30 20:58:29 willuhn *** empty log message ***
 * 
 * Revision 1.2 2004/06/17 00:14:10 willuhn
 * @N GenericObject, GenericIterator
 * 
 * Revision 1.1 2004/05/25 23:23:17 willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 * 
 ******************************************************************************/
