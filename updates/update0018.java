/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0018.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/25 09:18:23 $
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
 * Erweitert die Spalte "kontonummer" auf 40 Stellen, um auch IBANs speichern zu koennen.
 * Eigentlich reichen fuer die IBAN 34 Stellen - aber wir lassen mal etwas Sicherheitspuffer.
 * Vielleicht fuer Kreditkartennummern.
 */
public class update0018 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0018()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "ALTER TABLE empfaenger ALTER COLUMN kontonummer VARCHAR(40);\n");

    // Update fuer McKoi
    statements.put(DBSupportMcKoiImpl.class.getName(),
        "ALTER CREATE TABLE empfaenger (" +
        "    id NUMERIC default UNIQUEKEY('empfaenger')," +
        "    kontonummer varchar(40) NOT NULL," +
        "    blz varchar(15) NOT NULL," +
        "    name varchar(27) NOT NULL," +
        "    kommentar varchar(1000) NULL," +
        "    UNIQUE (id)," +
        "    PRIMARY KEY (id)" +
        "  );\n");
    
    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE empfaenger CHANGE kontonummer kontonummer VARCHAR(40);\n");
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
      Logger.info("create sql table for update0018");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'empfaenger' aktualisiert"));
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
    return "Datenbank-Update für Tabelle \"empfaenger\"";
  }

}


/*********************************************************************
 * $Log: update0018.java,v $
 * Revision 1.2  2009/08/25 09:18:23  willuhn
 * @B fehlerhaftes Update-Statement fuer McKoi
 *
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/