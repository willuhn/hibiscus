/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/AbstractPatternImpl.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/11/18 00:43:29 $
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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.filter.Pattern;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung fuer einzelne Filter-Kriterien.
 */
public abstract class AbstractPatternImpl extends AbstractDBObject implements Pattern
{

  protected I18N i18n;

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public AbstractPatternImpl() throws RemoteException
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException
  {
    // kann geloescht werden
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getField() == null || getField().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Attributes an, in dem gesucht werden soll"));
      if (getPattern() == null || getPattern().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Suchbegriff ein"));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking filter criteria",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Filter-Kriteriums"));
    }
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#getField()
   */
  public String getField() throws RemoteException
  {
    return (String) getAttribute("field");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#setField(java.lang.String)
   */
  public void setField(String field) throws RemoteException
  {
    setAttribute("field",field);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#getPattern()
   */
  public String getPattern() throws RemoteException
  {
    return (String) getAttribute("pattern");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#setPattern(java.lang.String)
   */
  public void setPattern(String pattern) throws RemoteException
  {
    setAttribute("pattern",pattern);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#getType()
   */
  public int getType() throws RemoteException
  {
    Integer i = (Integer) getAttribute("patterntype");
    return i == null ? Pattern.TYPE_EQUALS : i.intValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#setType(int)
   */
  public void setType(int type) throws RemoteException
  {
    setAttribute("patterntype",new Integer(type));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#ignoreCase()
   */
  public boolean ignoreCase() throws RemoteException
  {
    Integer i = (Integer) getAttribute("ignorecase");
    return i == null ? true : i.intValue() != 0;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#setIgnoreCase(boolean)
   */
  public void setIgnoreCase(boolean b) throws RemoteException
  {
    setAttribute("ignorecase",new Integer(b ? 1 : 0));
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if (arg0.equals("this"))
      return this;
    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#getNameForType(int)
   */
  public String getNameForType(int type) throws RemoteException
  {
    switch (type)
    {
      case Pattern.TYPE_CONTAINS:
        return i18n.tr("enthält");
      case Pattern.TYPE_ENDSWITH:
        return i18n.tr("endet mit");
      case Pattern.TYPE_EQUALS:
        return i18n.tr("identisch mit");
      case Pattern.TYPE_STARTSWITH:
        return i18n.tr("beginnt mit");
      default:
        throw new RemoteException("pattern type " + type + " unknown");
    }
  }

  /**
   * Ueberschrieben, um einige Werte vorzubelegen, falls sie fehlen.
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  public void insert() throws RemoteException, ApplicationException
  {
    if (getAttribute("ignorecase") == null)
      setIgnoreCase(true);
    if (getAttribute("patterntype") == null)
      setType(Pattern.TYPE_CONTAINS);
    super.insert();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#getName()
   */
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#setName(java.lang.String)
   */
  public void setName(String name) throws RemoteException
  {
    setAttribute("name",name);
  }
}


/**********************************************************************
 * $Log: AbstractPatternImpl.java,v $
 * Revision 1.6  2005/11/18 00:43:29  willuhn
 * @B bug 21
 *
 * Revision 1.5  2005/07/20 17:00:37  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/07/11 18:12:47  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/05/30 14:25:48  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/05/24 23:30:03  web0
 * @N Erster Code fuer OP-Verwaltung
 *
 **********************************************************************/