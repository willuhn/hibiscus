-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "ueberweisung" um die Spalte "umbuchung"
-- ----------------------------------------------------------------------

alter table ueberweisung add umbuchung int(1) NULL;

-- ----------------------------------------------------------------------
-- $Log: update0022.sql,v $
-- Revision 1.1  2009/05/12 22:53:33  willuhn
-- @N BUGZILLA 189 - Ueberweisung als Umbuchung
--
-- ----------------------------------------------------------------------

