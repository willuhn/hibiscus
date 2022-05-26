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
 * Erweitert die Tabelle "aueberweisung" um die Spalte "instantpayment"
 */
public class update0070 extends AbstractUpdate
{
  private Map<Class<? extends DBSupport>,List<String>> statements = new HashMap()
  {{
    put(DBSupportH2Impl.class,        Arrays.asList("ALTER TABLE konto ALTER COLUMN kontonummer VARCHAR(16) NOT NULL;\n"));
    put(DBSupportMySqlImpl.class,     Arrays.asList("ALTER TABLE konto CHANGE kontonummer kontonummer VARCHAR(16) NOT NULL;\n"));
    put(DBSupportPostgreSQLImpl.class,Arrays.asList("ALTER TABLE konto ALTER COLUMN kontonummer TYPE VARCHAR(16), ALTER COLUMN kontonummer SET NOT NULL;\n"));
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
