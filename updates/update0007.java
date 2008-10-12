/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0007.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/10/12 22:10:20 $
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
 * Die Spalte "content" war u.U. zu kurz.
 */
public class update0007 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0007()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "DROP TABLE property;\n" +
        "CREATE TABLE property (" +
        "    id IDENTITY," +
        "    name varchar(255) NOT NULL," +
        "    content varchar(1000) NULL," +
        "    UNIQUE (id)," +
        "    UNIQUE (name)," +
        "    PRIMARY KEY (id)" +
        ");\n");

    // Update fuer McKoi
    statements.put(DBSupportMcKoiImpl.class.getName(),
        "ALTER CREATE TABLE property (" +
        "    id NUMERIC default UNIQUEKEY('property')," +
        "    name varchar(255) NOT NULL," +
        "    content varchar(1000) NULL," +
        "    UNIQUE (id)," +
        "    UNIQUE (name)," +
        "    PRIMARY KEY (id)" +
        ");\n");
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE property CHANGE content content text null;");
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
      throw new ApplicationException(i18n.tr("Datenbank {0} wird nicht unterstützt",driver));
    
    try
    {
      Logger.info("create sql table for update0007");
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
    return "Korrigiertes Datenbank-Update für neue Tabelle \"property\"";
  }

}


/*********************************************************************
 * $Log: update0007.java,v $
 * Revision 1.2  2008/10/12 22:10:20  willuhn
 * @B Typo in den Updates
 * @B Spalten-Sortierung und -breite fuer in den Positionen von Sammelauftraegen nicht gespeichert
 *
 * Revision 1.1  2008/06/15 21:55:51  willuhn
 * @N update007 - Spalte "content" vergroessert
 * @B Fix in update002 - verursachte Fehler auf alten MySQL-Versionen
 *
 **********************************************************************/