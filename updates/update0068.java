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
 * Neue Spalten fuer die Umsatz-Kategorien.
 */
public class update0068 extends AbstractUpdate
{
  private Map<Class<? extends DBSupport>,List<String>> statements = new HashMap()
  {{
    // Update fuer H2
    put(DBSupportH2Impl.class,Arrays.asList(
        "ALTER TABLE umsatztyp ADD (konto_id int(4), konto_kategorie varchar(255), flags int(1));",
        "ALTER TABLE umsatztyp ADD CONSTRAINT fk_konto14 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;"
    ));

    // Update fuer MySQL
    put(DBSupportMySqlImpl.class,Arrays.asList(
        "ALTER TABLE umsatztyp ADD konto_id int(10), ADD konto_kategorie varchar(255), ADD flags int(1);",
        "ALTER TABLE umsatztyp ADD CONSTRAINT fk_umsatztyp_konto FOREIGN KEY (konto_id) REFERENCES konto (id);"
    ));

    // Update fuer Postgres
    put(DBSupportPostgreSQLImpl.class,Arrays.asList(
        "ALTER TABLE umsatztyp ADD konto_id integer, ADD konto_kategorie varchar(255), ADD flags integer;",
        "ALTER TABLE umsatztyp ADD CONSTRAINT fk_konto14 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;"
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
