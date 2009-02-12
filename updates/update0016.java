/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0016.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/02/12 23:55:57 $
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
 * Erweitert die Spalte "empfaenger_konto" auf 34 Stellen, um auch IBANs speichern zu koennen
 */
public class update0016 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0016()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "ALTER TABLE umsatz ALTER COLUMN empfaenger_konto VARCHAR(40);\n");

    // Update fuer McKoi
    statements.put(DBSupportMcKoiImpl.class.getName(),
        "ALTER CREATE TABLE umsatz (" +
        "    id NUMERIC default UNIQUEKEY('umsatz')," +
        "    konto_id int(4) NOT NULL," +
        "    empfaenger_konto varchar(40)," +
        "    empfaenger_blz varchar(15)," +
        "    empfaenger_name varchar(255)," +
        "    betrag double NOT NULL," +
        "    zweck varchar(35)," +
        "    zweck2 varchar(35)," +
        "    zweck3 varchar(1000)," +
        "    datum date NOT NULL," +
        "    valuta date NOT NULL," +
        "    saldo double," +
        "    primanota varchar(100)," +
        "    art varchar(100)," +
        "    customerref varchar(100)," +
        "    kommentar text NULL," +
        "    checksum numeric NULL," +
        "    umsatztyp_id int(5) NULL," +
        "    flags int(1) NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        "  );\n");
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE umsatz CHANGE empfaenger_konto empfaenger_konto VARCHAR(40);\n");
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
      Logger.info("create sql table for update0016");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'umsatz' aktualisiert"));
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
    return "Datenbank-Update für Tabelle \"umsatz\"";
  }

}


/*********************************************************************
 * $Log: update0016.java,v $
 * Revision 1.1  2009/02/12 23:55:57  willuhn
 * @N Erster Code fuer Unterstuetzung von Auslandsueberweisungen: In Tabelle "umsatz" die Spalte "empfaenger_konto" auf 40 Stellen erweitert und Eingabefeld bis max. 34 Stellen, damit IBANs gespeichert werden koennen
 *
 **********************************************************************/