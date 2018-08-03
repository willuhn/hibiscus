/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.hbci.rmi.DBSupport;
import de.willuhn.jameica.hbci.server.AbstractUpdate;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.DBSupportMySqlImpl;
import de.willuhn.jameica.hbci.server.DBSupportPostgreSQLImpl;


/**
 * Macht die Umsatz-Tabelle fit fuer CAMT.
 */
public class update0063 extends AbstractUpdate
{
  private Map<Class<? extends DBSupport>,List<String>> statements = new HashMap()
  {{
    // Update fuer H2
    put(DBSupportH2Impl.class,Arrays.asList(
        "ALTER TABLE umsatz ALTER COLUMN zweck varchar(255);",
        "ALTER TABLE umsatz ADD txid varchar(100);",
        "ALTER TABLE umsatz ADD purposecode varchar(10);"
    ));

    // Update fuer MySQL
    put(DBSupportMySqlImpl.class,Arrays.asList(
        "ALTER TABLE umsatz CHANGE zweck zweck varchar(255);",
        "ALTER TABLE umsatz ADD txid varchar(100);",
        "ALTER TABLE umsatz ADD purposecode varchar(10);"
    ));

    // Update fuer PostGreSQL
    put(DBSupportPostgreSQLImpl.class,Arrays.asList(
        "ALTER TABLE umsatz ALTER COLUMN zweck TYPE varchar(255);",
        "ALTER TABLE umsatz ADD txid varchar(100);",
        "ALTER TABLE umsatz ADD purposecode varchar(10);"
    ));
  }};

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractUpdate#getStatements(java.lang.Class)
   */
  @Override
  protected List<String> getStatements(Class<? extends DBSupport> driverClass)
  {
    return statements.get(driverClass);
  }
}
