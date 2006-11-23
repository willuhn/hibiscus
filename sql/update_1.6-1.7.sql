------------------------------------------------------------------------
-- $Source: /cvsroot/hibiscus/hibiscus/sql/Attic/update_1.6-1.7.sql,v $
-- $Revision: 1.1 $
-- $Date: 2006/11/23 23:24:17 $
-- $Author: willuhn $
-- $Locker:  $
-- $State: Exp $
--
-- Copyright (c) by willuhn.webdesign
-- All rights reserved
--
------------------------------------------------------------------------

-- Neue Spalte "umsatztyp_id"
ALTER CREATE TABLE umsatz (
  id NUMERIC default UNIQUEKEY('umsatz'),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15),
  empfaenger_blz varchar(15),
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(35),
  zweck2 varchar(35),
  datum date NOT NULL,
  valuta date NOT NULL,
  saldo double,
  primanota varchar(100),
  art varchar(100),
  customerref varchar(100),
  kommentar text NULL,
  checksum numeric NULL,
  umsatztyp_id int(5) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

-- Neue Spalte "parent_id"
ALTER CREATE TABLE umsatztyp (
  id NUMERIC default UNIQUEKEY('umsatztyp'),
  name varchar(255) NOT NULL,
  pattern varchar(255) NOT NULL,
  isregex int(1) NULL,
  iseinnahme int(1) NULL,
  parent_id int(5) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER TABLE umsatz ADD CONSTRAINT fk_umsatztyp1 FOREIGN KEY (umsatztyp_id) REFERENCES umsatztyp (id) DEFERRABLE;
ALTER TABLE umsatztyp ADD CONSTRAINT fk_umsatztyp2 FOREIGN KEY (parent_id) REFERENCES umsatztyp (id) DEFERRABLE;

-- Sinnvolle vordefinierte Werte fuer Suchfilter
insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('Gehalt','(Lohn.*?)|(Gehalt.*?)',1,1);
insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('Miete','Miete',0,0);
insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('Kreditkarte','(Visa.*?)|(Mastercard.*?)|(American Express.*?)',1,0);
insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('GEZ','RUNDFUNKANST.',0,0);
insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('Telefon','(O2.*?)|(Telekom.*?)|(telecom.*?)|(Vodafone.*?)|(eplus.*?)|(t-mobile.*?)|(Arcor.*?)',1,0);
insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('EC-Kartenzahlung','EC.*?',1,0);

------------------------------------------------------------------------
-- $Log: update_1.6-1.7.sql,v $
-- Revision 1.1  2006/11/23 23:24:17  willuhn
-- @N Umsatz-Kategorien: DB-Update, Edit
--
------------------------------------------------------------------------