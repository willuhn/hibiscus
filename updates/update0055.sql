-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "aueberweisung" um die Spalte "umbuchung"
-- ----------------------------------------------------------------------

alter table aueberweisung add umbuchung int(1) NULL;
