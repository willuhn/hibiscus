/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.io.StringReader;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.DBSupport;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse fuer Datenbank-Updates.
 */
public abstract class AbstractUpdate implements Update
{
  @Override
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

    try
    {
      String driver = HBCIDBService.SETTINGS.getString("database.driver",DBSupportH2Impl.class.getName());
      Class<? extends DBSupport> driverClass = Application.getClassLoader().load(driver);
      
      List<String> sql = this.getStatements(driverClass);
      if (sql == null)
        throw new ApplicationException(i18n.tr("Datenbank {0} nicht wird unterstützt",driver));
      
      if (sql.size() == 0)
      {
        myProvider.getProgressMonitor().log(i18n.tr("Update übersprungen, nicht notwendig"));
        return;
      }

      // Wir packen alle Zeilen in einen String, damit es in einer gemeinsamen Transaktion ausgefuehrt wird.
      StringBuilder sb = new StringBuilder();
      for (String s:sql)
      {
        sb.append(s);
        sb.append("\n");
      }
      
      ScriptExecutor.execute(new StringReader(sb.toString()),myProvider.getConnection(),myProvider.getProgressMonitor());
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
   * Liefert die auszufuehrenden Datenbank-Updates fuer den jeweiligen Treiber.
   * @param driverClass die Klasse des Treibers.
   * @return die Statements.
   */
  protected abstract List<String> getStatements(Class<? extends DBSupport> driverClass);

  @Override
  public String getName()
  {
    return "database update " + this.getClass().getSimpleName();
  }
}
