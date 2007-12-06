ALTER TABLE umsatz ALTER COLUMN kommentar varchar(1000) NULL;
ALTER TABLE systemnachricht ALTER COLUMN nachricht varchar(1000) NULL;

CREATE TABLE IF NOT EXISTS version (
  id IDENTITY,
  name varchar(255) NOT NULL,
  version int(5) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

------------------------------------------------------------------------
-- $Log: h2-update_1.7-1.8.sql,v $
-- Revision 1.2  2007/12/06 17:57:20  willuhn
-- @N Erster Code fuer das neue Versionierungs-System
--
-- Revision 1.1  2007/10/02 16:08:55  willuhn
-- @C Bugfix mit dem falschen Spaltentyp nochmal ueberarbeitet
--
------------------------------------------------------------------------
