/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzTypImpl.java,v $
 * $Revision: 1.22 $
 * $Date: 2006/05/22 12:54:52 $
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Umsatz-Typs.
 */
public class UmsatzTypImpl extends AbstractDBObject implements UmsatzTyp
{

  private I18N i18n = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public UmsatzTypImpl() throws RemoteException {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "umsatztyp";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {
			if (getName() == null || getName().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
		}
		catch (RemoteException e)
		{
			Logger.error("error while insert check",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatz-Typs."));
		}
    super.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
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
    DBIterator list = getService().createList(Umsatz.class);
    if (days > 0)
    {
      long d = days * 24l * 60l * 60l * 1000l;
      list.addFilter("TONUMBER(valuta) > " + (System.currentTimeMillis() - d));
    }
    ArrayList result = new ArrayList();
    while (list.hasNext())
    {
      Umsatz u = (Umsatz) list.next();
      if (matches(u))
        result.add(u);
    }
    return PseudoIterator.fromArray((Umsatz[])result.toArray(new Umsatz[result.size()]));
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
    this.setAttribute("name",name);
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
    setAttribute("pattern",pattern);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#matches(de.willuhn.jameica.hbci.rmi.Umsatz)
   */
  public boolean matches(Umsatz umsatz) throws RemoteException
  {
    String vwz1 = umsatz.getZweck();
    String vwz2 = umsatz.getZweck2();
    String name = umsatz.getEmpfaengerName();
    String kto  = umsatz.getEmpfaengerKonto();
    String kom  = umsatz.getKommentar();

    if (vwz1 == null) vwz1 = "";
    if (vwz2 == null) vwz2 = "";
    if (name == null) name = "";
    if (kto  == null) kto  = "";
    if (kom  == null) kom  = "";

    vwz1 = vwz1.toLowerCase();
    vwz2 = vwz2.toLowerCase();
    name = name.toLowerCase();
    kto  = kto.toLowerCase();
    kom  = kom.toLowerCase();
    
    String s = this.getPattern();
    if (s != null)
      s = s.toLowerCase();  // Wir beachten Gross-Kleinschreibung grundsaetzlich nicht

    if (isRegex())
    {
      if (s == null)
        s = ".*";
      Pattern pattern = null;
      
      pattern = Pattern.compile(s);
      Matcher mVwz1 = pattern.matcher(vwz1);
      Matcher mVwz2 = pattern.matcher(vwz2);
      Matcher mName = pattern.matcher(name);
      Matcher mKto  = pattern.matcher(kto);
      Matcher mKom  = pattern.matcher(kom);
      
      return (mVwz1.matches() || mVwz2.matches() || mName.matches() || mKto.matches() || mKom.matches());
    }

    if (s == null)
      s = "";
    return (vwz1.indexOf(s) != -1 || vwz2.indexOf(s) != -1 || name.indexOf(s) != -1 || kto.indexOf(s) != -1 || kom.indexOf(s) != -1);
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
    setAttribute("isregex",new Integer(regex ? 1 : 0));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsatz()
   */
  public double getUmsatz() throws RemoteException
  {
    return getUmsatz(-1);
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
    if ("umsatz".equals(arg0)) // Synthetisches Attribut "umsatz"
      return new Double(getUmsatz());
    if (arg0 != null && arg0.startsWith("umsatz"))
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
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#isEinnahme()
   */
  public boolean isEinnahme() throws RemoteException
  {
    Integer i = (Integer) getAttribute("iseinnahme");
    return i != null && i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#setEinnahme(boolean)
   */
  public void setEinnahme(boolean einnahme) throws RemoteException
  {
    setAttribute("iseinnahme",new Integer(einnahme ? 1 : 0));
  }
}


/**********************************************************************
 * $Log: UmsatzTypImpl.java,v $
 * Revision 1.22  2006/05/22 12:54:52  willuhn
 * @N bug 235 (thanks to Markus)
 *
 * Revision 1.21  2006/04/25 23:25:09  willuhn
 * @N bug 81
 *
 * Revision 1.20  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 * Revision 1.19  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.18  2005/12/29 01:22:11  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
 * Revision 1.17  2005/12/20 00:03:26  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 * Revision 1.16  2005/12/13 00:06:31  willuhn
 * @N UmsatzTyp erweitert
 *
 * Revision 1.15  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.14  2005/12/05 17:20:40  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.13  2005/11/18 00:43:29  willuhn
 * @B bug 21
 *
 * Revision 1.12  2005/11/14 23:47:20  willuhn
 * @N added first code for umsatz categories
 *
 * Revision 1.11  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.10  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.9  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.7  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.5  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.3  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/