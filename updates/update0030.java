/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0030.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 12:16:52 $
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
import de.willuhn.jameica.hbci.server.DBSupportPostgreSQLImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Erweitert die Tabelle "konto" um die Spalte "saldo_available".
 */
public class update0030 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0030()
  {
    statements.put(DBSupportH2Impl.class.getName(),        "ALTER TABLE konto ADD saldo_available double;\n");
    statements.put(DBSupportMcKoiImpl.class.getName(),     "ALTER TABLE konto ADD saldo_available double;\n");
    statements.put(DBSupportMySqlImpl.class.getName(),     "ALTER TABLE konto ADD saldo_available double;\n");
    statements.put(DBSupportPostgreSQLImpl.class.getName(),"ALTER TABLE konto ADD saldo_available float;\n");
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
 * $Log: update0030.java,v $
 * Revision 1.1  2010/06/17 12:16:52  willuhn
 * @N BUGZILLA 530
 *
 **********************************************************************/