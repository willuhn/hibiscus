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
 * Fuegt die Spalten "mandateid" und "empfaenger_name2" zur Umsatz-Tabelle hinzu
 */
public class update0066 extends AbstractUpdate
{
  private Map<Class<? extends DBSupport>,List<String>> statements = new HashMap()
  {{
    // Update fuer H2
    put(DBSupportH2Impl.class,Arrays.asList(
        "ALTER TABLE umsatz ADD (mandateid varchar(100), empfaenger_name2 varchar(255));"
    ));

    // Update fuer MySQL
    put(DBSupportMySqlImpl.class,Arrays.asList(
        "ALTER TABLE umsatz ADD mandateid varchar(100), ADD empfaenger_name2 varchar(255);"
    ));

    // Update fuer Postgres
    put(DBSupportPostgreSQLImpl.class,Arrays.asList(
        "ALTER TABLE umsatz ADD mandateid varchar(100), ADD empfaenger_name2 varchar(255);"
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
