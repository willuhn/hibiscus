/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/OPPatternImpl.java,v $
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

import de.willuhn.jameica.hbci.rmi.OPPattern;
import de.willuhn.jameica.hbci.rmi.OffenerPosten;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung eines einzelnen Filter-Kriteriums.
 */
public class OPPatternImpl extends AbstractPatternImpl implements OPPattern
{

  /**
   * @throws java.rmi.RemoteException
   */
  public OPPatternImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "op_pattern";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("offeneposten_id".equals(arg0))
      return OffenerPosten.class;
    return super.getForeignObject(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.OPPattern#getOffenerPosten()
   */
  public OffenerPosten getOffenerPosten() throws RemoteException
  {
    return (OffenerPosten) getAttribute("offeneposten_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.OPPattern#setOffenerPosten(de.willuhn.jameica.hbci.rmi.OffenerPosten)
   */
  public void setOffenerPosten(OffenerPosten p) throws RemoteException
  {
    if (p == null)
      return;
    setAttribute("offeneposten_id",p);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getOffenerPosten() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Offenen Posten für dieses Kriterium aus"));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking op pattern",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Filter-Kriteriums"));
    }
    super.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    super.updateCheck();
  }
}


/**********************************************************************
 * $Log: OPPatternImpl.java,v $
 * Revision 1.2  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 * Revision 1.1  2005/05/24 23:30:03  web0
 * @N Erster Code fuer OP-Verwaltung
 *
 **********************************************************************/