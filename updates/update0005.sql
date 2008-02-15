-- ----------------------------------------------------------------------
-- Erweitert die Tabellen "slastbuchung" und "sueberweisungbuchung" um eine Spalte "typ"
-- fuer den Textschluessel
-- ----------------------------------------------------------------------

alter table slastbuchung add typ varchar(2) NULL;
alter table sueberweisungbuchung add typ varchar(2) NULL;

-- ----------------------------------------------------------------------
-- $Log: update0005.sql,v $
-- Revision 1.1  2008/02/15 17:39:10  willuhn
-- @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
-- @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
--
-- ----------------------------------------------------------------------
