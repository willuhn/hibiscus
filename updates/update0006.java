/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0006.java,v $
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
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Datenbank-Update fuer neue Tabelle "property"
 */
public class update0006 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0006()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "CREATE TABLE property (" +
        "    id IDENTITY," +
        "    name varchar(255) NOT NULL," +
        "    content varchar(255) NULL," +
        "    UNIQUE (id)," +
        "    UNIQUE (name)," +
        "    PRIMARY KEY (id)" +
        ");\n");

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "CREATE TABLE property (" +
        "    id int(10) AUTO_INCREMENT," +
        "    name varchar(255) NOT NULL," +
        "    content varchar(255) NULL," +
        "    UNIQUE (id)," +
        "    UNIQUE (name)," +
        "    PRIMARY KEY (id)" +
        ") ENGINE=InnoDB;\n");
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
      throw new ApplicationException(i18n.tr("Datenbank {0} wird nicht unterstützt",driver));
    
    try
    {
      Logger.info("create sql table for update0006");
      ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'property' erstellt"));
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
    return "Datenbank-Update für neue Tabelle \"property\"";
  }

}


/*********************************************************************
 * $Log: update0006.java,v $
 * Revision 1.5  2011/10/24 14:24:22  willuhn
 * @B Parameter "database.driver" darf inzwischen NULL sein - in dem Fall H2 als Default verwenden
 *
 * Revision 1.4  2010-11-02 12:02:20  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.3  2008/10/12 22:10:20  willuhn
 * @B Typo in den Updates
 * @B Spalten-Sortierung und -breite fuer in den Positionen von Sammelauftraegen nicht gespeichert
 *
 * Revision 1.2  2008/05/30 12:37:50  willuhn
 * @C Feld "name" unique
 *
 * Revision 1.1  2008/05/30 12:34:04  willuhn
 * @N Neue Tabelle "property" in der allgemein alle moeglichen Parameter datenbank-gestuetzt gespeichert werden koennen.
 *
 * Revision 1.1  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 **********************************************************************/