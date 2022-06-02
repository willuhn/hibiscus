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
 * Verlaengert die Spalte "kontonummer" im Konto auf 16 Zeichen.
 */
public class update0070 extends AbstractUpdate
{
  private Map<Class<? extends DBSupport>,List<String>> statements = new HashMap()
  {{
    // Update fuer H2
    put(DBSupportH2Impl.class,Arrays.asList(
        "ALTER TABLE konto ALTER COLUMN kontonummer varchar(16) NOT NULL;"
    ));

    // Update fuer MySQL
    put(DBSupportMySqlImpl.class,Arrays.asList(
        "ALTER TABLE konto CHANGE kontonummer kontonummer varchar(16) NOT NULL;"
    ));

    // Update fuer PostGres
    put(DBSupportPostgreSQLImpl.class,Arrays.asList(
        "ALTER TABLE konto ALTER COLUMN kontonummer TYPE varchar(16) NOT NULL;"
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
