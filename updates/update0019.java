/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0019.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/11/02 12:02:20 $
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
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Fuehrt IBAN, BIC und Bankname fuer Auslandskonten ein.
 * Das Update macht update0018 rueckgaengig und BLZ und Kontonummer optional.
 */
public class update0019 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0019()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "ALTER TABLE empfaenger ALTER COLUMN kontonummer VARCHAR(15) NULL;\n" +
        "ALTER TABLE empfaenger ALTER COLUMN blz VARCHAR(15) NULL;\n" +
        "ALTER TABLE empfaenger ADD COLUMN bank VARCHAR(140) NULL;\n" +
        "ALTER TABLE empfaenger ADD COLUMN bic VARCHAR(15) NULL;\n" +
        "ALTER TABLE empfaenger ADD COLUMN iban VARCHAR(40) NULL;\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE empfaenger CHANGE kontonummer kontonummer VARCHAR(15) NULL;\n" +
        "ALTER TABLE empfaenger CHANGE blz blz VARCHAR(15) NULL;\n" +
        "ALTER TABLE empfaenger ADD bank VARCHAR(140) NULL;\n" +
        "ALTER TABLE empfaenger ADD bic VARCHAR(15) NULL;\n" +
        "ALTER TABLE empfaenger ADD iban VARCHAR(40) NULL;\n");
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
      Logger.info("create sql table for update0019");
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
 * $Log: update0019.java,v $
 * Revision 1.3  2010/11/02 12:02:20  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.2  2009/08/25 09:18:23  willuhn
 * @B fehlerhaftes Update-Statement fuer McKoi
 *
 * Revision 1.1  2009/02/18 00:35:54  willuhn
 * @N Auslaendische Bankverbindungen im Adressbuch
 *
 **********************************************************************/