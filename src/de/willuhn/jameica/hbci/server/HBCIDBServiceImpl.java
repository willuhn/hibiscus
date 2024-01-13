/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.DBSupport;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.sql.version.Updater;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;
import de.willuhn.util.ProgressMonitor;

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
    Logger.info("determine current database version");
    Version version = null;
    try
    {
      version = VersionUtil.getVersion(this,"db");
      Logger.info("current database version: " + version.getVersion());
    }
    catch (RemoteException re)
    {
      Throwable cause = re.getCause();

      // Checken, ob es eine SQL-Exception war
      if (!(cause instanceof SQLException))
        throw re;
      
      Logger.warn("unable to determine database version - database probably empty, recreating");
      Logger.write(Level.DEBUG,"stacktrace for debugging purpose",re);
      try
      {
        this.install();
        
        // Jetzt sollte sich die Version laden lassen
        version = VersionUtil.getVersion(this,"db");
        Logger.info("current database version: " + version.getVersion());
      }
      catch (RemoteException re2)
      {
        Logger.error("unable to recreate database",re2);
        // Wir werfen die originale Exception
        throw re;
      }
    }
    
    try
    {
      Logger.info("init update provider");
      UpdateProvider provider = new HBCIUpdateProvider(getConnection(),version);
      Updater updater = new Updater(provider,"iso-8859-1");
      updater.execute();
      Logger.info("updates finished");
    }
    catch (Exception e)
    {
      // Wir versuchen herauszufinden, ob es dieses Problem hier ist:
      // https://homebanking-hilfe.de/forum/topic.php?p=139423#real139423
      // Siehe auch https://www.h2database.com/javadoc/org/h2/api/ErrorCode.html#c90131
      Throwable t = e;
      
      for (int i=0;i<10;++i)
      {
        if (t == null)
          break;
        
        if (t instanceof SQLException)
        {
          SQLException se = (SQLException) t;
          int code = se.getErrorCode();
          if (code == 90131)
          {
            Logger.error("found buggy h2 driver, update jameica first",e);
            
            I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
            throw new ApplicationException(i18n.tr("Bitte aktualisiere erst Jameica auf Version 2.8.2 oder höher"));
          }
        }
        t = t.getCause();
      }

      // Keine passende Exception gefunden. Dann Original weiterwerfen.
      throw new ApplicationException(e);
    }
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
      Application.getMessagingFactory().getMessagingQueue("jameica.error").sendMessage(new QueryMessage(e));
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
   * @see de.willuhn.datasource.db.DBServiceImpl#createList(java.lang.Class)
   */
  @Override
  public <T extends DBObject> DBIterator<T> createList(Class<? extends DBObject> arg0) throws RemoteException
  {
    try
    {
      return super.createList(arg0);
    }
    catch (RemoteException re)
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.error").sendMessage(new QueryMessage(re));
      throw re;
    }
  }
  
  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#createObject(java.lang.Class, java.lang.String)
   */
  @Override
  public <T extends DBObject> T createObject(Class<? extends DBObject> arg0, String arg1) throws RemoteException
  {
    try
    {
      return super.createObject(arg0, arg1);
    }
    catch (ObjectNotFoundException ofe)
    {
      // Das kann durchaus mal passieren. Das sollte kein Backup verhindern
      throw ofe;
    }
    catch (RemoteException re)
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.error").sendMessage(new QueryMessage(re));
      throw re;
    }
  }
  
  /**
   * @see de.willuhn.datasource.db.DBServiceImpl#execute(java.lang.String, java.lang.Object[], de.willuhn.datasource.rmi.ResultSetExtractor)
   */
  @Override
  public Object execute(String arg0, Object[] arg1, ResultSetExtractor arg2) throws RemoteException
  {
    try
    {
      return super.execute(arg0, arg1, arg2);
    }
    catch (RemoteException re)
    {
      Application.getMessagingFactory().getMessagingQueue("jameica.error").sendMessage(new QueryMessage(re));
      throw re;
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
