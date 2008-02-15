/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0004.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/02/15 17:39:10 $
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
 * Datenbank-Update fuer BUGZILLA 188.
 */
public class update0004 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0004()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "CREATE TABLE verwendungszweck (" +
        "    id IDENTITY," +
        "    typ int(1) NOT NULL," +
        "    auftrag_id int(10) NOT NULL," +
        "    zweck varchar(27) NOT NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ");\n");

    // Update fuer McKoi
    statements.put(DBSupportMcKoiImpl.class.getName(),
        "CREATE TABLE verwendungszweck (" +
        "    id NUMERIC default UNIQUEKEY('verwendungszweck')," +
        "    typ int(1) NOT NULL," +
        "    auftrag_id int(10) NOT NULL," +
        "    zweck varchar(27) NOT NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ");\n");
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "CREATE TABLE verwendungszweck (" +
        "    id int(10) AUTO_INCREMENT," +
        "    typ int(1) NOT NULL," +
        "    auftrag_id int(10) NOT NULL," +
        "    zweck varchar(27) NOT NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ")TYPE=InnoDB;\n" +
        "CREATE INDEX idx_zweck ON verwendungszweck(typ,auftrag_id);\n");
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
      Logger.info("create sql tables for update0004");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle für zusätzliche Verwendungszwecke erstellt"));
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
    return "Datenbank-Update für \"Bug 188 (Mehr Zeilen für Verwendungszweck)\"";
  }

}


/*********************************************************************
 * $Log: update0004.java,v $
 * Revision 1.1  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 **********************************************************************/