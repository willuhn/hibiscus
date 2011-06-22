/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0032.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/06/22 13:07:50 $
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
 * Fuehrt die Spalte "ausgefuehrt_am" in Ueberweisungen und Lastchriften ein.
 */
public class update0032 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0032()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "ALTER TABLE ueberweisung  ADD ausgefuehrt_am datetime NULL;\n" +
        "ALTER TABLE aueberweisung ADD ausgefuehrt_am datetime NULL;\n" +
        "ALTER TABLE lastschrift   ADD ausgefuehrt_am datetime NULL;\n" +
        "ALTER TABLE slastschrift  ADD ausgefuehrt_am datetime NULL;\n" +
        "ALTER TABLE sueberweisung ADD ausgefuehrt_am datetime NULL;\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE ueberweisung  ADD ausgefuehrt_am DATETIME;\n" +
        "ALTER TABLE aueberweisung ADD ausgefuehrt_am DATETIME;\n" +
        "ALTER TABLE lastschrift   ADD ausgefuehrt_am DATETIME;\n" +
        "ALTER TABLE slastschrift  ADD ausgefuehrt_am DATETIME;\n" +
        "ALTER TABLE sueberweisung ADD ausgefuehrt_am DATETIME;\n");

    // Update fuer PostGres
    statements.put(DBSupportPostgreSQLImpl.class.getName(),
        "ALTER TABLE ueberweisung  ADD ausgefuehrt_am timestamp;\n" +
        "ALTER TABLE aueberweisung ADD ausgefuehrt_am timestamp;\n" +
        "ALTER TABLE lastschrift   ADD ausgefuehrt_am timestamp;\n" +
        "ALTER TABLE slastschrift  ADD ausgefuehrt_am timestamp;\n" +
        "ALTER TABLE sueberweisung ADD ausgefuehrt_am timestamp;\n");
  }

  /**
   * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
   */
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

    String driver = HBCIDBService.SETTINGS.getString("database.driver",null);
    String sql = (String) statements.get(driver);
    if (sql == null)
      throw new ApplicationException(i18n.tr("Datenbank {0} nicht wird unterstützt",driver));
    
    try
    {
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabellen aktualisiert"));
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
    return "Datenbank-Update für neue Spalte \"ausgefuehrt_am\"";
  }

}


/*********************************************************************
 * $Log: update0032.java,v $
 * Revision 1.3  2011/06/22 13:07:50  willuhn
 * @R UNDO, die Tabelle existierte tatsaechlich nicht
 *
 * Revision 1.1  2011-04-29 15:33:28  willuhn
 * @N Neue Spalte "ausgefuehrt_am", in der das tatsaechliche Ausfuehrungsdatum von Auftraegen vermerkt wird
 *
 **********************************************************************/