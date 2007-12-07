-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "konto" um eine Spalte "unterkonto"
-- ----------------------------------------------------------------------

alter table konto add unterkonto varchar(10) null;

-- ----------------------------------------------------------------------
-- $Log: update0001.sql,v $
-- Revision 1.1  2007/12/07 00:48:05  willuhn
-- @N weiterer Code fuer den neuen Update-Mechanismus
--
-- ----------------------------------------------------------------------

