-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "aueberweisung" um eine Spalte "endtoendid"
-- ----------------------------------------------------------------------

alter table aueberweisung add endtoendid VARCHAR(35) NULL;
