-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "umsatz" um eine Spalte "addkey"
-- ----------------------------------------------------------------------

alter table umsatz add addkey VARCHAR(3) NULL;

-- ----------------------------------------------------------------------
-- $Log: update0035.sql,v $
-- Revision 1.1  2011/07/25 17:17:19  willuhn
-- @N BUGZILLA 1065 - zusaetzlich noch addkey
--
-- Revision 1.1  2011-07-25 14:42:40  willuhn
-- @N BUGZILLA 1065
--
-- ----------------------------------------------------------------------
