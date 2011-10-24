/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0002.java,v $
 * $Revision: 1.8 $
 * $Date: 2011/10/24 14:24:22 $
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
        "    pattern varchar(255) NULL," +
        "    isregex int(1) NULL," + 
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
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "CREATE TABLE op (" +
        "    id int(10) AUTO_INCREMENT," +
        "    name varchar(255) not NULL," +
        "    pattern varchar(255) NULL," +
        "    isregex int(1) NULL," + 
        "    betrag double NOT NULL," +
        "    termin date NULL," +
        "    kommentar text NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ")TYPE=InnoDB;\n" +
        "CREATE TABLE op_buchung (" +
        "    id int(10) AUTO_INCREMENT," +
        "    umsatz_id int(10) NOT NULL," +
        "    op_id int(10) NOT NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        ")TYPE=InnoDB;\n");
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
    String driver = HBCIDBService.SETTINGS.getString("database.driver",DBSupportH2Impl.class.getName());
    String sql = (String) statements.get(driver);
    if (sql == null)
      throw new ApplicationException(i18n.tr("Datenbank {0} wird nicht unterstützt",driver));
    
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
 * Revision 1.8  2011/10/24 14:24:22  willuhn
 * @B Parameter "database.driver" darf inzwischen NULL sein - in dem Fall H2 als Default verwenden
 *
 * Revision 1.7  2010-11-02 12:02:20  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.6  2008/10/12 22:10:20  willuhn
 * @B Typo in den Updates
 * @B Spalten-Sortierung und -breite fuer in den Positionen von Sammelauftraegen nicht gespeichert
 *
 * Revision 1.5  2008/06/15 21:55:51  willuhn
 * @N update007 - Spalte "content" vergroessert
 * @B Fix in update002 - verursachte Fehler auf alten MySQL-Versionen
 *
 * Revision 1.4  2007/12/12 10:02:44  willuhn
 * @N Datenbank-Updates auch in Create-Scripts nachziehen
 *
 * Revision 1.3  2007/12/11 16:10:11  willuhn
 * @N Erster Code fuer "Offene Posten-Verwaltung"
 *
 * Revision 1.2  2007/12/11 15:25:18  willuhn
 * @N Class-Update fuer neue Tabellen "op" und "op_buchung"
 *
 * Revision 1.1  2007/12/11 15:23:53  willuhn
 * @N Class-Update fuer neue Tabellen "op" und "op_buchung"
 *
 **********************************************************************/