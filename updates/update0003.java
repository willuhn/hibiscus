/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0003.java,v $
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
 * Datenbank-Update fuer Bug 538.
 */
public class update0003 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0003()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "alter table dauerauftrag alter column orderid varchar(100);\n");
    
    // Update fuer McKoi
    // Mckoi kann wohl scheinbar keine einzelnen Spalten selektiv aendern
    statements.put(DBSupportMcKoiImpl.class.getName(),
      "ALTER CREATE TABLE dauerauftrag (" +
      "id NUMERIC default UNIQUEKEY('dauerauftrag')," +
      "konto_id int(4) NOT NULL," +
      "empfaenger_konto varchar(15) NOT NULL," +
      "empfaenger_blz varchar(15) NOT NULL," +
      "empfaenger_name varchar(255)," +
      "betrag double NOT NULL," +
      "zweck varchar(27) NOT NULL," +
      "zweck2 varchar(27)," +
      "erste_zahlung date NOT NULL," +
      "letzte_zahlung date," +
      "orderid varchar(100)," +
      "zeiteinheit int(1) NOT NULL," +
      "intervall int(2) NOT NULL," +
      "tag int(2) NOT NULL," +
      "UNIQUE (id)," +
      "PRIMARY KEY (id));\n");
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
      "alter table dauerauftrag change orderid orderid varchar(100);\n");
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
      Logger.info("create sql tables for update0003");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Erweitere Spalte Order-ID in Tabelle dauerauftrag"));
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
    return "Datenbank-Update für Bug 538";
  }

}


/*********************************************************************
 * $Log: update0003.java,v $
 * Revision 1.2  2008/10/12 22:10:20  willuhn
 * @B Typo in den Updates
 * @B Spalten-Sortierung und -breite fuer in den Positionen von Sammelauftraegen nicht gespeichert
 *
 * Revision 1.1  2008/01/14 23:18:55  willuhn
 * @B BUGZILLA 538
 *
 **********************************************************************/