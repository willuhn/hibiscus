/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
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
 * Tabellen fuer den elektronischen Kontoauszug.
 */
public class update0062 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0062()
  {
    statements.put(DBSupportH2Impl.class.getName(),         "CREATE TABLE kontoauszug (\n" +
                                                            "  id IDENTITY(1),\n" +
                                                            "  konto_id int(4) NOT NULL,\n" +
                                                            "  ausgefuehrt_am datetime,\n" +
                                                            "  kommentar varchar(1000),\n" +
                                                            "  pfad varchar(1000),\n" +
                                                            "  dateiname varchar(256),\n" +
                                                            "  uuid varchar(255),\n" +
                                                            "  format varchar(5),\n" +
                                                            "  erstellungsdatum date,\n" +
                                                            "  von date,\n" +
                                                            "  bis date,\n" +
                                                            "  jahr int(4),\n" +
                                                            "  nummer int(5),\n" +
                                                            "  name1 varchar(255),\n" +
                                                            "  name2 varchar(255),\n" +
                                                            "  name3 varchar(255),\n" +
                                                            "  quittungscode varchar(1000),\n" +
                                                            "  quittiert_am datetime,\n" +
                                                            "  gelesen_am datetime,\n" +
                                                            "  UNIQUE (id),\n" +
                                                            "  PRIMARY KEY (id)\n" +
                                                            ");\n" +
                                                            "ALTER TABLE kontoauszug ADD CONSTRAINT fk_konto13 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;");
    statements.put(DBSupportMySqlImpl.class.getName(),      "CREATE TABLE kontoauszug (\n" +
                                                            "  id int(10) AUTO_INCREMENT,\n" +
                                                            "  konto_id int(10) NOT NULL,\n" +
                                                            "  ausgefuehrt_am datetime,\n" +
                                                            "  kommentar TEXT,\n" +
                                                            "  pfad TEXT,\n" +
                                                            "  dateiname TEXT,\n" +
                                                            "  uuid varchar(255),\n" +
                                                            "  format varchar(5),\n" +
                                                            "  erstellungsdatum date,\n" +
                                                            "  von date,\n" +
                                                            "  bis date,\n" +
                                                            "  jahr int(4),\n" +
                                                            "  nummer int(5),\n" +
                                                            "  name1 varchar(255),\n" +
                                                            "  name2 varchar(255),\n" +
                                                            "  name3 varchar(255),\n" +
                                                            "  quittungscode TEXT,\n" +
                                                            "  quittiert_am datetime,\n" +
                                                            "  gelesen_am datetime,\n" +
                                                            "  UNIQUE (id),\n" +
                                                            "  PRIMARY KEY (id)\n" +
                                                            ") ENGINE=InnoDB;\n" +
                                                            "CREATE INDEX idx_kontoauszug_konto ON kontoauszug(konto_id);\n" +
                                                            "CREATE INDEX idx_kontoauszug_gelesen ON kontoauszug(gelesen_am);\n" +
                                                            "ALTER TABLE kontoauszug ADD CONSTRAINT fk_kontoauszug_konto FOREIGN KEY (konto_id) REFERENCES konto (id);");
    statements.put(DBSupportPostgreSQLImpl.class.getName(), "CREATE TABLE kontoauszug (\n" +
                                                            "  id serial primary key,\n" +
                                                            "  konto_id integer NOT NULL,\n" +
                                                            "  ausgefuehrt_am timestamp,\n" +
                                                            "  kommentar varchar(1000),\n" +
                                                            "  pfad varchar(1000),\n" +
                                                            "  dateiname varchar(256),\n" +
                                                            "  uuid varchar(255),\n" +
                                                            "  format varchar(5),\n" +
                                                            "  erstellungsdatum date,\n" +
                                                            "  von date,\n" +
                                                            "  bis date,\n" +
                                                            "  jahr integer,\n" +
                                                            "  nummer integer,\n" +
                                                            "  name1 varchar(255),\n" +
                                                            "  name2 varchar(255),\n" +
                                                            "  name3 varchar(255),\n" +
                                                            "  quittungscode varchar(1000),\n" +
                                                            "  quittiert_am timestamp,\n" +
                                                            "  gelesen_am timestamp\n" +
                                                            ");\n" +
                                                            "ALTER TABLE kontoauszug ADD CONSTRAINT fk_konto13 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;");
  }

  /**
   * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
   */
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

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
    return "Datenbank-Update für elektronischen Kontoauszug.";
  }

}
