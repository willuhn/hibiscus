/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0037.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/20 16:20:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.DBSupportMySqlImpl;
import de.willuhn.jameica.hbci.server.DBSupportPostgreSQLImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Legt die Tabelle fuer die Reminder an.
 */
public class update0037 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0037()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "CREATE TABLE reminder (" +
        "  id IDENTITY," +
        "  uuid varchar(255) NOT NULL," +
        "  content varchar(60000) NOT NULL," +
        "  UNIQUE (id)," +
        "  UNIQUE (uuid)," +
        "  PRIMARY KEY (id)" +
        ");\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "CREATE TABLE reminder (" +
        "  id int(10) AUTO_INCREMENT," +
        "  uuid varchar(255) NOT NULL," +
        "  content text NOT NULL," +
        "  UNIQUE (id)," +
        "  UNIQUE (uuid)," +
        "  PRIMARY KEY (id) " +
        ") ENGINE=InnoDB;\n" +
        "CREATE INDEX idx_reminder_uuid ON reminder(uuid);\n");

    // Update fuer PostgreSQL
    statements.put(DBSupportPostgreSQLImpl.class.getName(),
        "CREATE TABLE reminder (" +
        "  id serial primary key," +
        "  uuid varchar(255) NOT NULL," +
        "  content varchar(60000) NOT NULL" +
        ");\n");
  }

  /**
   * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
   */
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

    // Wenn wir eine Tabelle erstellen wollen, muessen wir wissen, welche
    // SQL-Dialekt wir sprechen
    String driver = HBCIDBService.SETTINGS.getString("database.driver",null);
    String sql = (String) statements.get(driver);
    if (sql == null)
      throw new ApplicationException(i18n.tr("Datenbank {0} nicht wird unterstützt",driver));
    
    try
    {
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle aktualisiert"));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to execute update",e);
      throw new ApplicationException(i18n.tr("Fehler beim Ausführen des Updates"),e);
    }
  }

  /**
   * @see de.willuhn.sql.version.Update#getName()
   */
  public String getName()
  {
    return "Datenbank-Update für Tabelle \"reminder\"";
  }

}


/*********************************************************************
 * $Log: update0037.java,v $
 * Revision 1.1  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 **********************************************************************/