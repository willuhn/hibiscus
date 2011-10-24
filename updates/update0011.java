/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0011.java,v $
 * $Revision: 1.3 $
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

import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.DBSupportMySqlImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Korrigiertes Datenbank-Update fuer Tabelle "version".
 * Die Spalte "name" bei MySQL zu kurz.
 */
public class update0011 implements Update
{
  /**
   * ct
   */
  public update0011()
  {
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
    if (driver == null || !driver.equals(DBSupportMySqlImpl.class.getName()))
    {
      Logger.info("skip update, not needed");
      return; // Update nur fuer MySQL noetig
    }
    try
    {
      Logger.info("update sql table for update0011");
      String sql = "ALTER TABLE version CHANGE name name VARCHAR(255) NOT NULL;\n";
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'version' aktualisiert"));
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
    return "Datenbank-Update für Tabelle \"version\"";
  }

}


/*********************************************************************
 * $Log: update0011.java,v $
 * Revision 1.3  2011/10/24 14:24:22  willuhn
 * @B Parameter "database.driver" darf inzwischen NULL sein - in dem Fall H2 als Default verwenden
 *
 * Revision 1.2  2008/11/25 00:52:53  willuhn
 * @N Loggen, wenn Update 11 nicht noetig ist
 *
 * Revision 1.1  2008/10/01 13:44:18  willuhn
 * @B DB-Update 11: Spalte "name" in Tabelle "version" war (nur bei MySQL) zu kurz
 *
 **********************************************************************/