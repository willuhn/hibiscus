/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
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
 * Legt die Tabelle fuer die SEPA-Sammel-Lastschriften an.
 */
public class update0049 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0049()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "CREATE TABLE sepaslast (" +
        "  id IDENTITY(1)," +
        "  konto_id int(4) NOT NULL," +
        "  bezeichnung varchar(255) NOT NULL," +
        "  sequencetype varchar(8) NOT NULL," +
        "  sepatype varchar(8) NULL," +
        "  targetdate date NULL," +
        "  termin date NOT NULL," +
        "  ausgefuehrt int(1) NOT NULL," +
        "  ausgefuehrt_am datetime NULL," +
        "  orderid varchar(255) NULL," +
        "  UNIQUE (id)," +
        "  PRIMARY KEY (id)" +
        ");\n" +
        "CREATE TABLE sepaslastbuchung (" +
        "  id IDENTITY(1)," +
        "  sepaslast_id int(4) NOT NULL," +
        "  empfaenger_konto varchar(40) NOT NULL," +
        "  empfaenger_name varchar(140) NOT NULL," +
        "  empfaenger_bic varchar(15) NULL," +
        "  betrag double NOT NULL," +
        "  zweck varchar(140)," +
        "  endtoendid varchar(35)," +
        "  creditorid varchar(35) NOT NULL," +
        "  mandateid varchar(35) NOT NULL," +
        "  sigdate date NOT NULL," +
        "  UNIQUE (id)," +
        "  PRIMARY KEY (id)" +
        ");\n" +
        "ALTER TABLE sepaslast ADD CONSTRAINT fk_konto10 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;\n" +
        "ALTER TABLE sepaslastbuchung ADD CONSTRAINT fk_sepaslast1 FOREIGN KEY (sepaslast_id) REFERENCES sepaslast (id) DEFERRABLE;\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "CREATE TABLE sepaslast (" +
        "    id int(10) AUTO_INCREMENT" +
        "  , konto_id int(10) NOT NULL" +
        "  , bezeichnung VARCHAR(255) NOT NULL" +
        "  , sequencetype VARCHAR(8) NOT NULL" +
        "  , sepatype VARCHAR(8)" +
        "  , targetdate DATE" +
        "  , termin DATE NOT NULL" +
        "  , ausgefuehrt int(10) NOT NULL" +
        "  , ausgefuehrt_am DATETIME" +
        "  , orderid VARCHAR(255)" +
        "  , UNIQUE (id)" +
        "  , PRIMARY KEY (id)" +
        ") ENGINE=InnoDB;\n" +
        "CREATE TABLE sepaslastbuchung (" +
        "  id int(10) AUTO_INCREMENT" +
        "  , sepaslast_id int(10) NOT NULL" +
        "  , empfaenger_konto VARCHAR(40) NOT NULL" +
        "  , empfaenger_name VARCHAR(140) NOT NULL" +
        "  , empfaenger_bic VARCHAR(15) NULL" +
        "  , betrag DOUBLE NOT NULL" +
        "  , zweck VARCHAR(140)" +
        "  , endtoendid VARCHAR(35)" +
        "  , creditorid VARCHAR(35) NOT NULL" +
        "  , mandateid VARCHAR(35) NOT NULL" +
        "  , sigdate DATE NOT NULL" +
        "  , UNIQUE (id)" +
        "  , PRIMARY KEY (id)" +
        ") ENGINE=InnoDB;\n" +
        "CREATE INDEX idx_sepaslast_konto ON sepaslast(konto_id);\n" +
        "CREATE INDEX idx_sepaslastbuchung_sepaslast ON sepaslastbuchung(sepaslast_id);\n" +
        "ALTER TABLE sepaslast ADD CONSTRAINT fk_sepaslast_konto FOREIGN KEY (konto_id) REFERENCES konto (id);\n" +
        "ALTER TABLE sepaslastbuchung ADD CONSTRAINT fk_sepaslastbuchung_sepaslast FOREIGN KEY (sepaslast_id) REFERENCES sepaslast (id);\n");


    // Update fuer PostgreSQL
    statements.put(DBSupportPostgreSQLImpl.class.getName(),
        "CREATE TABLE sepaslast (" +
        "  id serial primary key," +
        "  konto_id integer NOT NULL," +
        "  bezeichnung varchar(255) NOT NULL," +
        "  sequencetype varchar(8) NOT NULL," +
        "  sepatype varchar(8)," +
        "  targetdate date," +
        "  termin date NOT NULL," +
        "  ausgefuehrt integer NOT NULL," +
        "  ausgefuehrt_am timestamp," +
        "  orderid varchar(255)" +
        ");\n" +
        "CREATE TABLE sepaslastbuchung (" +
        "  id serial primary key," +
        "  sepaslast_id integer NOT NULL," +
        "  empfaenger_konto varchar(40) NOT NULL," +
        "  empfaenger_name varchar(140) NOT NULL," +
        "  empfaenger_bic varchar(15)," +
        "  betrag float NOT NULL," +
        "  zweck varchar(140)," +
        "  endtoendid varchar(35)," +
        "  creditorid varchar(35) NOT NULL," +
        "  mandateid varchar(35) NOT NULL," +
        "  sigdate date NOT NULL" +
        ");\n" +
        "ALTER TABLE sepaslast ADD CONSTRAINT fk_konto10 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;\n" +
        "ALTER TABLE sepaslastbuchung ADD CONSTRAINT fk_sepaslast1 FOREIGN KEY (sepaslast_id) REFERENCES sepaslast (id) DEFERRABLE;\n");

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
    return "Datenbank-Update für SEPA Sammellastschriften";
  }

}
