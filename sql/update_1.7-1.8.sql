-- Versionstabelle.
ALTER CREATE TABLE version (
  id NUMERIC default UNIQUEKEY('version'),
  name varchar(255) NOT NULL,
  version int(5) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

------------------------------------------------------------------------
-- $Log: update_1.7-1.8.sql,v $
-- Revision 1.1  2007/12/06 17:57:20  willuhn
-- @N Erster Code fuer das neue Versionierungs-System
--
------------------------------------------------------------------------
