/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import de.willuhn.datasource.db.DBServiceImpl;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.DBSupport;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.sql.version.Updater;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;
import de.willuhn.util.ProgressMonitor;

/**
 * @author willuhn
 */
public class HBCIDBServiceImpl extends DBServiceImpl implements HBCIDBService
{
  private DBSupport driver = null;
  
  /**
   * @throws RemoteException
   */
  public HBCIDBServiceImpl() throws RemoteException
  {
    this(SETTINGS.getString("database.driver",DBSupportH2Impl.class.getName()));
  }
  
  /**
   * Konstruktor mit expliziter Angabe des Treibers.
   * @param driverClass der zu verwendende Treiber.
   * @throws RemoteException
   */
  public HBCIDBServiceImpl(String driverClass) throws RemoteException
  {
    super();
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    MultipleClassLoader cl = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();
    this.setClassloader(cl);
    this.setClassFinder(cl.getClassFinder());
    if (driverClass == null)
      throw new RemoteException("no driver given");
    Logger.info("loading database driver: " + driverClass);
    try
    {
      Class c = cl.load(driverClass);
      this.driver = (DBSupport) service.get(c);
    }
    catch (Throwable t)
    {
      throw new RemoteException("unable to load database driver " + driverClass,t);
    }
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
   * @see de.willuhn.datasource.db.DBServiceImpl#getAutoCommit()
   */
  protected boolean getAutoCommit() throws RemoteException
  {
    return SETTINGS.getBoolean("autocommit",super.getAutoCommit());
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcDriver()
   */
  protected String getJdbcDriver() throws RemoteException
  {
    return this.driver.getJdbcDriver();
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcPassword()
   */
  protected String getJdbcPassword() throws RemoteException
  {
    return this.driver.getJdbcPassword();
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcUrl()
   */
  protected String getJdbcUrl() throws RemoteException
  {
    return this.driver.getJdbcUrl();
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcUsername()
   */
  protected String getJdbcUsername() throws RemoteException
  {
    return this.driver.getJdbcUsername();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#checkConsistency()
   */
  public void checkConsistency() throws RemoteException, ApplicationException
  {
    Logger.info("init update provider");
    UpdateProvider provider = new HBCIUpdateProvider(getConnection(),VersionUtil.getVersion(this,"db"));
    Updater updater = new Updater(provider,"iso-8859-1");
    updater.execute();
    Logger.info("updates finished");
  }

  
  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getConnection()
   */
  protected Connection getConnection() throws RemoteException
  {
    try
    {
      return super.getConnection();
    }
    catch (RemoteException re)
    {
      // Wir benachrichtigen Jameica ueber den Fehler, damit beim Shutdown kein Backup erstellt wird
      Application.getMessagingFactory().getMessagingQueue("jameica.error").sendMessage(new QueryMessage(re));
      throw re;
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#executeUpdate(java.lang.String, java.lang.String[])
   */
  @Override
  public int executeUpdate(String query, String... params) throws RemoteException
  {
    if (!isStarted())
      throw new RemoteException("db service not started");
    
    Connection conn = getConnection();
    PreparedStatement ps = null;
    try
    {
      ps = conn.prepareStatement(query);
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
      
      int count = ps.executeUpdate();
      conn.commit();
      return count;
    }
    catch (SQLException e)
    {
      Logger.error("error while executing sql update",e);
      throw new RemoteException("error while executing sql update: " + e.getMessage(),e);
    }
    finally
    {
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

  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#install()
   */
  public void install() throws RemoteException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    ProgressMonitor monitor = Application.getCallback().getStartupMonitor();
    monitor.setStatusText(i18n.tr("Installiere Hibiscus"));
    this.driver.install();
    
    Manifest mf = Application.getPluginLoader().getPlugin(HBCI.class).getManifest();
    File file = new File(mf.getPluginDir() + File.separator + "sql","create.sql");
    this.driver.execute(getConnection(),file);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#getSQLTimestamp(java.lang.String)
   */
  public String getSQLTimestamp(String content) throws RemoteException
  {
    return this.driver.getSQLTimestamp(content);
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getInsertWithID()
   */
  protected boolean getInsertWithID() throws RemoteException
  {
    return this.driver.getInsertWithID();
  }

  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#checkConnection(java.sql.Connection)
   */
  protected void checkConnection(Connection conn) throws SQLException
  {
    try
    {
      this.driver.checkConnection(conn);
    }
    catch (RemoteException re)
    {
      throw new SQLException(re.getMessage());
    }
    super.checkConnection(conn);
  }
  
  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#getTransactionIsolationLevel()
   */
  protected int getTransactionIsolationLevel() throws RemoteException
  {
    // BUGZILLA 447
    return this.driver.getTransactionIsolationLevel();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.HBCIDBService#getDriver()
   */
  public DBSupport getDriver() throws RemoteException
  {
    return this.driver;
  }
}
