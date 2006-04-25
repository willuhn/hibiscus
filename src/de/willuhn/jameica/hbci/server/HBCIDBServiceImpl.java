/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/HBCIDBServiceImpl.java,v $
 * $Revision: 1.11 $
 * $Date: 2006/04/25 23:25:12 $
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import de.willuhn.datasource.db.EmbeddedDBServiceImpl;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.ResultSetExtractor;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public class HBCIDBServiceImpl extends EmbeddedDBServiceImpl implements HBCIDBService
{

  /**
   * @throws RemoteException
   */
  public HBCIDBServiceImpl() throws RemoteException
  {
    super(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() + "/db/db.conf",
          "hibiscus","hibiscus");
    this.setClassFinder(Application.getClassLoader().getClassFinder());
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    return i18n.tr("Datenbank-Service für Hibiscus");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#execute(java.lang.String, java.lang.Object[], de.willuhn.jameica.hbci.rmi.ResultSetExtractor)
   */
  public Object execute(String sql, Object[] params, ResultSetExtractor extractor) throws RemoteException
  {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      ps = getConnection().prepareStatement(sql);
      if (params != null)
      {
        for (int i=0;i<params.length;++i)
        {
          Object o = params[i];
          if (o == null)
            ps.setNull((i+1), Types.NULL);
          else
            ps.setObject((i+1),params[i]);
        }
      }

      rs = ps.executeQuery();
      return extractor.extract(rs);
    }
    catch (SQLException e)
    {
      Logger.error("error while executing sql statement",e);
      throw new RemoteException("error while executing sql statement: " + e.getMessage(),e);
    }
    finally
    {
      if (rs != null)
      {
        try
        {
          rs.close();
        }
        catch (Throwable t)
        {
          Logger.error("error while closing resultset",t);
        }
      }
      if (ps != null)
      {
        try
        {
          ps.close();
        }
        catch (Throwable t2)
        {
          Logger.error("error while closing statement",t2);
        }
      }
    }
  }

}


/*********************************************************************
 * $Log: HBCIDBServiceImpl.java,v $
 * Revision 1.11  2006/04/25 23:25:12  willuhn
 * @N bug 81
 *
 * Revision 1.10  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2005/01/30 20:45:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2005/01/05 15:17:50  willuhn
 * @N Neues Service-System in Jameica
 *
 * Revision 1.7  2004/11/17 19:02:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/03 18:42:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/09/15 22:31:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/08/31 18:13:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/23 16:23:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 **********************************************************************/