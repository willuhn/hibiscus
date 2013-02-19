/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0033.java,v $
 * $Revision: 1.5 $
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
import de.willuhn.jameica.hbci.server.DBSupportPostgreSQLImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Verlaengert die Spalte "unterkonto" in der Tabelle "konto" von 10 auf 30 Zeichen.
 * BUGZILLA 1056
 */
public class update0033 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0033()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),"ALTER TABLE konto ALTER COLUMN unterkonto varchar(30) NULL;\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),"ALTER TABLE konto CHANGE unterkonto unterkonto varchar(30) NULL;\n");

    // Update fuer PostGreSQL
    statements.put(DBSupportPostgreSQLImpl.class.getName(),"ALTER TABLE konto ALTER COLUMN unterkonto TYPE varchar(30);\n");
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
    return "Datenbank-Update für Tabelle \"konto\"";
  }

}


/*********************************************************************
 * $Log: update0033.java,v $
 * Revision 1.5  2011/10/24 14:24:22  willuhn
 * @B Parameter "database.driver" darf inzwischen NULL sein - in dem Fall H2 als Default verwenden
 *
 * Revision 1.4  2011-06-22 16:26:38  willuhn
 * @B Postgres-Fixes
 *
 * Revision 1.3  2011-06-22 13:07:50  willuhn
 * @R UNDO, die Tabelle existierte tatsaechlich nicht
 *
 * Revision 1.1  2011-05-27 11:33:23  willuhn
 * @N BUGZILLA 1056
 *
 **********************************************************************/