-- ----------------------------------------------------------------------
-- Entfernt die Spalte "synchronize" - wird nicht mehr benoetigt
-- ----------------------------------------------------------------------

alter table konto drop synchronize;

-- ----------------------------------------------------------------------
-- $Log: update0014.sql,v $
-- Revision 1.1  2009/01/26 23:17:46  willuhn
-- @R Feld "synchronize" aus Konto-Tabelle entfernt. Aufgrund der Synchronize-Optionen pro Konto ist die Information redundant und ergibt sich implizit, wenn fuer ein Konto irgendeine der Synchronisations-Optionen aktiviert ist
--
-- ----------------------------------------------------------------------
