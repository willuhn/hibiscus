/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0023.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/16 12:44:02 $
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
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Erweitert die Spalte "nachricht" in der Tabelle "systemnachricht" auf 4000 Zeichen.
 * Banken schicken ggf. laengere Texte. MySQL und McKoi sind davon nicht betroffen,
 * da die Spalten dort im Format "TEXT" vorliegen und daher keine 1000-Zeichen-Begrenzung
 * vorliegt.
 */
public class update0023 implements Update
{
  
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
    if (driver == null || !driver.equals(DBSupportH2Impl.class.getName()))
    {
      Logger.info("skip update, not needed");
      return; // Update nur fuer H2 noetig
    }
    try
    {
      String sql = "ALTER TABLE systemnachricht ALTER COLUMN nachricht VARCHAR(4000) NOT NULL;";
      Logger.info("update sql table for update0023");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'systemnachricht' aktualisiert"));
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
    return "Datenbank-Update für Tabelle \"systemnachricht\"";
  }

}


/*********************************************************************
 * $Log: update0023.java,v $
 * Revision 1.1  2009/06/16 12:44:02  willuhn
 * @N DB-Update 23
 *
 **********************************************************************/