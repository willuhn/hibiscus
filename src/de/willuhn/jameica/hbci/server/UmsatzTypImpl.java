/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzTypImpl.java,v $
 * $Revision: 1.16 $
 * $Date: 2005/12/13 00:06:31 $
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.rmi.UmsatzZuordnung;
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
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException {

		try {
			this.transactionBegin();

		
			// wir entfernen uns aus allen Umsaetzen
			DBIterator list = getUmsatzZuordnungen();
			UmsatzZuordnung u = null;
			while (list.hasNext())
			{
				u = (UmsatzZuordnung) list.next();
        u.delete();
			}

			// Jetzt koennen wir uns selbst loeschen
			super.delete();
			this.transactionCommit();
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
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsatzZuordnungen()
   */
  public DBIterator getUmsatzZuordnungen() throws RemoteException
  {
    DBIterator list = getService().createList(UmsatzZuordnung.class);
    list.addFilter("umsatztyp_id = " + getID());
    return list;
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
   * @see de.willuhn.jameica.hbci.rmi.filter.UmsatzFilter#filter(de.willuhn.jameica.hbci.rmi.Umsatz, org.kapott.hbci.GV_Result.GVRKUms.UmsLine)
   */
  public void filter(Umsatz umsatz, UmsLine rawData) throws RemoteException
  {
    if (umsatz == null)
      return;

    DBIterator types = Settings.getDBService().createList(UmsatzTyp.class);
    while (types.hasNext())
    {
      UmsatzTyp typ = (UmsatzTyp) types.next();
      if (typ.isZugeordnet(umsatz))
        return; // den haben wir bereits
      
      try
      {
        if (typ.matches(umsatz))
        {
          Logger.info("assigned umsatz to umsatz type " + typ.getName());
          UmsatzZuordnung z = (UmsatzZuordnung) Settings.getDBService().createObject(UmsatzZuordnung.class,null);
          z.setUmsatz(umsatz);
          z.setUmsatzTyp(typ);
          try
          {
            z.store();
          }
          catch (ApplicationException ae)
          {
            Logger.error("error while assigning umsatz",ae);
            throw new RemoteException(i18n.tr("Fehler beim Zuordnen des Umsatzes"),ae);
          }
        }
      }
      catch (PatternSyntaxException pe)
      {
        // Fehler im regulaeren Ausdruck loggen wir
        Logger.warn("invalid regex: " + pe.getMessage());
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#isZugeordnet(de.willuhn.jameica.hbci.rmi.Umsatz)
   */
  public boolean isZugeordnet(Umsatz u) throws RemoteException
  {
    DBIterator list = getService().createList(UmsatzZuordnung.class);
    list.addFilter("umsatztyp_id = " + getID());
    list.addFilter("umsatz_id = " + u.getID());
    return list.hasNext();
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

    if (vwz1 == null) vwz1 = "";
    if (vwz2 == null) vwz2 = "";
    if (name == null) name = "";
    if (kto == null)   kto = "";

    vwz1 = vwz1.toLowerCase();
    vwz2 = vwz2.toLowerCase();
    name = name.toLowerCase();
    kto  = kto.toLowerCase();
    
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
      
      return (mVwz1.matches() || mVwz2.matches() || mName.matches() || mKto.matches());
    }

    if (s == null)
      s = "";
    return (vwz1.indexOf(s) != -1 || vwz2.indexOf(s) != -1 || name.indexOf(s) != -1 || kto.indexOf(s) != -1);
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

}


/**********************************************************************
 * $Log: UmsatzTypImpl.java,v $
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