/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/HBCIDBServiceImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/07/23 16:23:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.db.DBServiceImpl;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.system.Application;

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
          ":jdbc:mckoi:local://" + PluginLoader.getPlugin(HBCI.class).getResources().getWorkPath() + "/db/db.conf",
          "hibiscus","hibiscus");
    this.setClassFinder(Application.getClassLoader().getClassFinder());
  }

}


/*********************************************************************
 * $Log: HBCIDBServiceImpl.java,v $
 * Revision 1.2  2004/07/23 16:23:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 **********************************************************************/