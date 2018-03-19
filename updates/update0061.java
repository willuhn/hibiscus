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
 * Definiert ein paar Indizes.
 */
public class update0061 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0061()
  {
    statements.put(DBSupportH2Impl.class.getName(),         "CREATE INDEX idx_umsatz_datum ON umsatz(datum);\n" +
                                                            "CREATE INDEX idx_umsatz_valuta ON umsatz(valuta);\n" +
                                                            "CREATE INDEX idx_umsatz_flags ON umsatz(flags);");
    statements.put(DBSupportMySqlImpl.class.getName(),      "ALTER TABLE umsatz ADD INDEX (flags);"); // Die anderen Indizes gabs da schon
    statements.put(DBSupportPostgreSQLImpl.class.getName(), "CREATE INDEX idx_umsatz_datum ON umsatz(datum);\n" +
                                                            "CREATE INDEX idx_umsatz_valuta ON umsatz(valuta);\n" +
                                                            "CREATE INDEX idx_umsatz_flags ON umsatz(flags);");
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
    return "Datenbank-Update für Erweiterung des Umsatztyp um Kommentar.";
  }

}
