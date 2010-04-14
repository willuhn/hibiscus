-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "Konto" um die Spalten "iban" und "bic"
-- ----------------------------------------------------------------------

alter table konto add iban VARCHAR(40) NULL;
alter table konto add bic VARCHAR(15) NULL;

-- ----------------------------------------------------------------------
-- $Log: update0025.sql,v $
-- Revision 1.2  2010/04/14 17:44:10  willuhn
-- @N BUGZILLA 83
--
-- Revision 1.1  2009/10/20 23:12:58  willuhn
-- @N Support fuer SEPA-Ueberweisungen
-- @N Konten um IBAN und BIC erweitert
--
-- ----------------------------------------------------------------------

