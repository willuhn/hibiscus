/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/OffenerPostenImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/05/25 00:42:04 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.OPPattern;
import de.willuhn.jameica.hbci.rmi.OffenerPosten;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.filter.Pattern;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines einzelnen Offenen Postens.
 */
public class OffenerPostenImpl extends AbstractDBObject implements OffenerPosten
{

  private I18N i18n;

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public OffenerPostenImpl() throws RemoteException
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "offeneposten";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "bezeichnung";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException
  {
    // Kann getrost geloescht werden.
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getBezeichnung() == null || getBezeichnung().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bezeichnung ein"));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking OP entry",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen des offenen Postens"),e);
    }
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    this.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if (arg0.equals("umsatz_id"))
      return Umsatz.class;

    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.OffenerPosten#getBezeichnung()
   */
  public String getBezeichnung() throws RemoteException
  {
    return (String) getAttribute("bezeichnung");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.OffenerPosten#setBezeichnung(java.lang.String)
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    this.setAttribute("bezeichnung", bezeichnung);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.OffenerPosten#isOffen()
   */
  public boolean isOffen() throws RemoteException
  {
    return getUmsatz() == null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.OffenerPosten#getDatum()
   */
  public Date getDatum() throws RemoteException
  {
    return (Date) getAttribute("datum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Filter#getPattern()
   */
  public Pattern[] getPattern() throws RemoteException
  {
    DBIterator list = this.getService().createList(OPPattern.class);
    list.addFilter("offeneposten_id = '" + this.getID() + "'");
    Pattern[] p = new Pattern[list.size()];
    int i = 0;
    while (list.hasNext())
    {
      p[i++] = (Pattern) list.next();
    }
    return p;
  }

  /**
   * @see de.willuhn.datasource.rmi.Changeable#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    try
    {
      this.transactionBegin();

      // Beim Loeschen muessen wir auch alle Pattern mit loeschen
      Pattern[] pattern = getPattern();
      for (int i=0;i<pattern.length;++i)
      {
        pattern[i].delete();
      }
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
   * @see de.willuhn.jameica.hbci.rmi.OffenerPosten#getUmsatz()
   */
  public Umsatz getUmsatz() throws RemoteException
  {
    return (Umsatz) getAttribute("umsatz_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.OffenerPosten#setUmsatz(de.willuhn.jameica.hbci.rmi.Umsatz)
   */
  public void setUmsatz(Umsatz umsatz) throws RemoteException
  {
    if (umsatz == null)
      return;

    if (getUmsatz() != null)
    {
      Logger.warn("umsatz allready assigned to OP entry [" + this.getAttribute(this.getPrimaryAttribute())+ "], skipping");
      return;
    }
    setAttribute("umsatz_id",umsatz);
    Date date = new Date();
    Logger.info("marked OP entry as done: " + date.toString());
    setAttribute("datum",date);
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    // Ueberschrieben, um ein synthetisches Attribut "filter" zu erzeugen
    if ("filter".equals(arg0))
    {
      Pattern[] p = getPattern();
      if (p == null || p.length == 0)
        return null;

      StringBuffer sb = new StringBuffer();
      for (int i=0;i<p.length;++i)
      {
        sb.append(p[i].getAttribute(p[i].getPrimaryAttribute()));
        if (i<p.length-1)
          sb.append("\n"); // ausser beim letzten Element einen Zeilenumbruch anhaengen
      }
      return sb.toString();
    }
    return super.getAttribute(arg0);
  }
}


/**********************************************************************
 * $Log: OffenerPostenImpl.java,v $
 * Revision 1.2  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 * Revision 1.1  2005/05/24 23:30:03  web0
 * @N Erster Code fuer OP-Verwaltung
 *
 **********************************************************************/