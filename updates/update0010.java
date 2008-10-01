/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0010.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/10/01 11:06:08 $
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
 * Korrigiertes Datenbank-Update fuer neue Tabelle "property".
 * Die Spalte "name" war u.U. zu kurz.
 */
public class update0010 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0010()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "ALTER TABLE property ALTER COLUMN name VARCHAR(1000) NOT NULL;\n");

    // Update fuer McKoi
    statements.put(DBSupportMcKoiImpl.class.getName(),
        "ALTER CREATE TABLE property (" +
        "    id NUMERIC default UNIQUEKEY('property')," +
        "    name varchar(1000) NOT NULL," +
        "    content varchar(1000) NULL," +
        "    UNIQUE (id)," +
        "    UNIQUE (name)," +
        "    PRIMARY KEY (id)" +
        ");\n");
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE property DROP KEY name;\n" +
        "ALTER TABLE property CHANGE name name TEXT NOT NULL;\n" +
        "ALTER TABLE property ADD UNIQUE KEY (name(512));\n");
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
      Logger.info("create sql table for update0010");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'property' aktualisiert"));
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
    return "Datenbank-Update für Tabelle \"property\"";
  }

}


/*********************************************************************
 * $Log: update0010.java,v $
 * Revision 1.2  2008/10/01 11:06:08  willuhn
 * @B DB-Update 10: Unique-Key korrigiert (bei Spalten vom Typ "TEXT" muss die Laenge des Index mit angegeben werden. Siehe http://www.mydigitallife.info/2007/07/09/mysql-error-1170-42000-blobtext-column-used-in-key-specification-without-a-key-length/
 *
 * Revision 1.1  2008/10/01 10:42:51  willuhn
 * @N Spalte "name" in Tabelle "property" vergroessert - es gibt Parameter, die laenger als 255 Zeichen sind. DB-Update 10
 *
 **********************************************************************/