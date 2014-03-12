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
 * Legt die Tabelle fuer die SEPA-Sammel-Ueberweisungen an.
 */
public class update0051 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0051()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "CREATE TABLE sepasueb (" +
        "  id IDENTITY(1)," +
        "  konto_id int(4) NOT NULL," +
        "  bezeichnung varchar(255) NOT NULL," +
        "  termin date NOT NULL," +
        "  ausgefuehrt int(1) NOT NULL," +
        "  ausgefuehrt_am datetime NULL," +
        "  UNIQUE (id)," +
        "  PRIMARY KEY (id)" +
        ");\n" +
        "CREATE TABLE sepasuebbuchung (" +
        "  id IDENTITY(1)," +
        "  sepasueb_id int(4) NOT NULL," +
        "  empfaenger_konto varchar(40) NOT NULL," +
        "  empfaenger_name varchar(140) NOT NULL," +
        "  empfaenger_bic varchar(15) NULL," +
        "  betrag double NOT NULL," +
        "  zweck varchar(140)," +
        "  endtoendid varchar(35)," +
        "  UNIQUE (id)," +
        "  PRIMARY KEY (id)" +
        ");\n" +
        "ALTER TABLE sepasueb ADD CONSTRAINT fk_konto11 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;\n" +
        "ALTER TABLE sepasuebbuchung ADD CONSTRAINT fk_sepasueb1 FOREIGN KEY (sepasueb_id) REFERENCES sepasueb (id) DEFERRABLE;\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "CREATE TABLE sepasueb (" +
        "    id int(10) AUTO_INCREMENT" +
        "  , konto_id int(10) NOT NULL" +
        "  , bezeichnung VARCHAR(255) NOT NULL" +
        "  , termin DATE NOT NULL" +
        "  , ausgefuehrt int(10) NOT NULL" +
        "  , ausgefuehrt_am DATETIME" +
        "  , UNIQUE (id)" +
        "  , PRIMARY KEY (id)" +
        ") ENGINE=InnoDB;\n" +
        "CREATE TABLE sepasuebbuchung (" +
        "  id int(10) AUTO_INCREMENT" +
        "  , sepasueb_id int(10) NOT NULL" +
        "  , empfaenger_konto VARCHAR(40) NOT NULL" +
        "  , empfaenger_name VARCHAR(140) NOT NULL" +
        "  , empfaenger_bic VARCHAR(15) NULL" +
        "  , betrag DOUBLE NOT NULL" +
        "  , zweck VARCHAR(140)" +
        "  , endtoendid VARCHAR(35)" +
        "  , UNIQUE (id)" +
        "  , PRIMARY KEY (id)" +
        ") ENGINE=InnoDB;\n" +
        "CREATE INDEX idx_sepasueb_konto ON sepasueb(konto_id);\n" +
        "CREATE INDEX idx_sepasuebbuchung_sepasueb ON sepasuebbuchung(sepasueb_id);\n" +
        "ALTER TABLE sepasueb ADD CONSTRAINT fk_sepasueb_konto FOREIGN KEY (konto_id) REFERENCES konto (id);\n" +
        "ALTER TABLE sepasuebbuchung ADD CONSTRAINT fk_sepasuebbuchung_sepasueb FOREIGN KEY (sepasueb_id) REFERENCES sepasueb (id);\n");


    // Update fuer PostgreSQL
    statements.put(DBSupportPostgreSQLImpl.class.getName(),
        "CREATE TABLE sepasueb (" +
        "  id serial primary key," +
        "  konto_id integer NOT NULL," +
        "  bezeichnung varchar(255) NOT NULL," +
        "  termin date NOT NULL," +
        "  ausgefuehrt integer NOT NULL," +
        "  ausgefuehrt_am timestamp" +
        ");\n" +
        "CREATE TABLE sepasuebbuchung (" +
        "  id serial primary key," +
        "  sepasueb_id integer NOT NULL," +
        "  empfaenger_konto varchar(40) NOT NULL," +
        "  empfaenger_name varchar(140) NOT NULL," +
        "  empfaenger_bic varchar(15)," +
        "  betrag float NOT NULL," +
        "  zweck varchar(140)," +
        "  endtoendid varchar(35)" +
        ");\n" +
        "ALTER TABLE sepasueb ADD CONSTRAINT fk_konto11 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;\n" +
        "ALTER TABLE sepasuebbuchung ADD CONSTRAINT fk_sepasueb1 FOREIGN KEY (sepasueb_id) REFERENCES sepasueb (id) DEFERRABLE;\n");

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
    return "Datenbank-Update für SEPA Sammelüberweisungen";
  }

}
