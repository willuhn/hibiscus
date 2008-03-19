-- Neue Versionstabelle
CREATE TABLE IF NOT EXISTS version (
       id int(10) AUTO_INCREMENT
     , name VARCHAR(15) NOT NULL
     , version int(10) NOT NULL
     , UNIQUE (id)
     , PRIMARY KEY (id)
)TYPE=InnoDB;

-- Typ der Spalte von "varchar(1000)" auf "text" geaendert (Siehe CVS-Commit auf mysql-create.sql vom 31.08.2007)
alter table protokoll change kommentar kommentar text not null;

-- Typ der Spalte von "int(10)" auf "bigint(16)" geaendert (Siehe BUGZILLA 478, CVS-Commit auf mysql-create.sql vom 10.09.2007)
alter table umsatz change checksum checksum bigint(16);
------------------------------------------------------------------------
-- $Log: mysql-update_1.7-1.8.sql,v $
-- Revision 1.2  2008/03/19 10:43:44  willuhn
-- @N Fehlende Aenderungen noch nachgetragen
--
-- Revision 1.1  2007/12/06 17:57:20  willuhn
-- @N Erster Code fuer das neue Versionierungs-System
--
------------------------------------------------------------------------
