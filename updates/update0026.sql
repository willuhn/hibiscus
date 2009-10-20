-- ----------------------------------------------------------------------
-- Entfernt die Spalte "empfaenger_bank" - wird nicht mehr benoetigt
-- ----------------------------------------------------------------------

alter table aueberweisung drop empfaenger_bank;

-- ----------------------------------------------------------------------
-- $Log: update0026.sql,v $
-- Revision 1.1  2009/10/20 23:12:58  willuhn
-- @N Support fuer SEPA-Ueberweisungen
-- @N Konten um IBAN und BIC erweitert
--
-- ----------------------------------------------------------------------
