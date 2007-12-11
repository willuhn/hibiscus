/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0002.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/12/11 15:25:18 $
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
import de.willuhn.jameica.hbci.server.DBSupportMcKoiImpl;
import de.willuhn.jameica.hbci.server.DBSupportMySqlImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Datenbank-Update fuer "Offene Posten".
 */
public class update0002 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0002()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "CREATE TABLE op (" +
        "    id IDENTITY," +
        "    name varchar(255) not NULL," +
        "    betrag double NOT NULL," +
        "    termin date NULL," +
        "    kommentar varchar(1000) NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ");\n" +
        "CREATE TABLE op_buchung (" +
        "    id IDENTITY," +
        "    umsatz_id int(10) NOT NULL," +
        "    op_id int(10) NOT NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ");\n");
    
    // Update fuer McKoi
    statements.put(DBSupportMcKoiImpl.class.getName(),
        "CREATE TABLE op (" +
        "    id NUMERIC default UNIQUEKEY('op')," +
        "    name varchar(255) not NULL," +
        "    betrag double NOT NULL," +
        "    termin date NULL," +
        "    kommentar varchar(1000) NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ");\n" +
        "CREATE TABLE op_buchung (" +
        "    id NUMERIC default UNIQUEKEY('op_buchung')," +
        "    umsatz_id int(10) NOT NULL," +
        "    op_id int(10) NOT NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ");\n");
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "CREATE TABLE op (" +
        "    id int(10) AUTO_INCREMENT," +
        "    name varchar(255) not NULL," +
        "    betrag double NOT NULL," +
        "    termin date NULL," +
        "    kommentar varchar(1000) NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ");\n" +
        "CREATE TABLE op_buchung (" +
        "    id int(10) AUTO_INCREMENT," +
        "    umsatz_id int(10) NOT NULL," +
        "    op_id int(10) NOT NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
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
      throw new ApplicationException(i18n.tr("Datenbank {0} wird unterstützt",driver));
    
    try
    {
      Logger.info("create sql tables for update0002");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabellen für Offene-Posten-Verwaltung erstellt"));
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
    return "Datenbank-Update für \"Offene Posten\"";
  }

}


/*********************************************************************
 * $Log: update0002.java,v $
 * Revision 1.2  2007/12/11 15:25:18  willuhn
 * @N Class-Update fuer neue Tabellen "op" und "op_buchung"
 *
 * Revision 1.1  2007/12/11 15:23:53  willuhn
 * @N Class-Update fuer neue Tabellen "op" und "op_buchung"
 *
 **********************************************************************/