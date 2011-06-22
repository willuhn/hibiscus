/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0028.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/06/22 16:26:38 $
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
 * Macht "passport_class" optional. Fuer Offline-Konten.
 */
public class update0028 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0028()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),"ALTER TABLE konto ALTER COLUMN passport_class varchar(1000) NULL;\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),"ALTER TABLE konto CHANGE passport_class passport_class TEXT NULL;\n");

    // Update fuer PostGreSQL
    statements.put(DBSupportPostgreSQLImpl.class.getName(),"ALTER TABLE konto ALTER COLUMN passport_class TYPE varchar(1000) NULL;\n");
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
      Logger.info("create sql table for update0028");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'konto' aktualisiert"));
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
 * $Log: update0028.java,v $
 * Revision 1.3  2011/06/22 16:26:38  willuhn
 * @B Postgres-Fixes
 *
 * Revision 1.2  2010-11-02 12:02:20  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.1  2010/04/22 12:42:03  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 *
 **********************************************************************/