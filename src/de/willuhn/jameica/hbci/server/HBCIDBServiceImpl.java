/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/HBCIDBServiceImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/09/15 22:31:42 $
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

import de.willuhn.datasource.db.DBServiceImpl;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public class HBCIDBServiceImpl extends DBServiceImpl implements DBService
{

  /**
   * @throws RemoteException
   */
  public HBCIDBServiceImpl() throws RemoteException
  {
    super("com.mckoi.JDBCDriver",
          ":jdbc:mckoi:local://" + Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() + "/db/db.conf",
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

}


/*********************************************************************
 * $Log: HBCIDBServiceImpl.java,v $
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