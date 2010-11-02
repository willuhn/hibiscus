/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0029.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/11/02 12:02:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

import java.sql.ResultSet;
import java.sql.Statement;

import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Entfernt den Unique-Key "name" in der Tabelle "umsatztyp".
 */
public class update0029 implements Update
{
  /**
   * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
   */
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

    String driver = HBCIDBService.SETTINGS.getString("database.driver",null);

    if (driver == null)
      throw new ApplicationException(i18n.tr("Keine Datenbank konfiguriert"));

    
    if (!DBSupportH2Impl.class.getName().equals(driver))
    {
      Logger.info("update not needed for " + driver);
      return;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // H2

    // Leider haben wir dem Constraint keinen Namen gegeben, sodass wir ihn erst ermitteln muessen
    Statement st  = null;
    Statement st2 = null;
    ResultSet rs  = null;
    try
    {
      st = myProvider.getConnection().createStatement();
      rs = st.executeQuery("SELECT constraint_name FROM information_schema.constraints WHERE table_name = 'UMSATZTYP' AND constraint_type='UNIQUE' AND column_list='NAME'");
      if (!rs.next())
      {
        Logger.info("constraint 'name' not found in table 'umsatztyp' for " + driver + ", update not needed");
        return;
      }
      // Update durchfuehren
      st2 = myProvider.getConnection().createStatement();
      st2.execute("ALTER TABLE umsatztyp drop constraint if exists " + rs.getString(1));
      myProvider.getProgressMonitor().log(i18n.tr("Tabelle 'umsatztyp' aktualisiert"));
    }
    catch (Exception e)
    {
      // Das Loggen wir nur. Aber eigentlich koennen wir da nichts machen.
      // Dann bleibt der Constraint halt drin. Das fuehrt dann halt dazu,
      // dass der User keine zwei Umsatz-Kategorien mit dem gleichen Namen
      // anlegen kann.
      Logger.error("unable to remove unique key, skipping update",e);
    }
    finally
    {
      try {
        if (rs != null)
          rs.close();
      } catch (Exception e) {/* useless */}
      try {
        if (st != null)
          st.close();
      } catch (Exception e) {/* useless */}
      try {
        if (st2 != null)
          st2.close();
      } catch (Exception e) {/* useless */}
    }
    ////////////////////////////////////////////////////////////////////////////
  }

  /**
   * @see de.willuhn.sql.version.Update#getName()
   */
  public String getName()
  {
    return "Datenbank-Update für Tabelle \"umsatztyp\"";
  }

}


/*********************************************************************
 * $Log: update0029.java,v $
 * Revision 1.2  2010/11/02 12:02:20  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.1  2010/06/02 15:32:03  willuhn
 * @N Unique-Constraint auf Spalte "name" in Tabelle "umsatztyp" entfernt. Eine Kategorie kann jetzt mit gleichem Namen beliebig oft auftreten
 * @N Auswahlbox der Oberkategorie in Einstellungen->Umsatz-Kategorien zeigt auch die gleiche Baumstruktur wie bei der Zuordnung der Kategorie in der Umsatzliste
 *
 **********************************************************************/