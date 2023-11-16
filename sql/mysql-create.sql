CREATE TABLE konto (
       id int(10) AUTO_INCREMENT
     , kontonummer VARCHAR(16) NOT NULL
     , unterkonto varchar(30) null
     , blz VARCHAR(15) NOT NULL
     , name VARCHAR(255) NOT NULL
     , bezeichnung VARCHAR(255)
     , kundennummer VARCHAR(255) NOT NULL
     , waehrung VARCHAR(6) NOT NULL
     , passport_class TEXT
     , saldo DOUBLE
     , saldo_datum DATETIME
     , kommentar TEXT
     , flags int(1)
     , iban VARCHAR(40) NULL
     , bic VARCHAR(15) NULL
     , saldo_available DOUBLE
     , kategorie VARCHAR(255) NULL
     , backend_class TEXT
     , acctype int(2)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE lastschrift (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , empfaenger_konto VARCHAR(15) NOT NULL
     , empfaenger_blz VARCHAR(15) NOT NULL
     , empfaenger_name VARCHAR(255)
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(27) NOT NULL
     , zweck2 VARCHAR(27)
     , zweck3 TEXT
     , termin DATE NOT NULL
     , ausgefuehrt int(10) NOT NULL
     , typ VARCHAR(2)
     , ausgefuehrt_am DATETIME
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE systemnachricht (
       id int(10) AUTO_INCREMENT
     , blz VARCHAR(15) NOT NULL
     , nachricht TEXT NOT NULL
     , datum DATE NOT NULL
     , gelesen int(10) NOT NULL
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE turnus (
       id int(10) AUTO_INCREMENT
     , zeiteinheit int(10) NOT NULL
     , intervall int(10) NOT NULL
     , tag int(10) NOT NULL
     , initial int(10)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE empfaenger (
       id int(10) AUTO_INCREMENT
     , kontonummer VARCHAR(15) NULL
     , blz VARCHAR(15) NULL
     , name VARCHAR(255) NOT NULL
     , iban VARCHAR(40) NULL
     , bic VARCHAR(15) NULL
     , bank VARCHAR(140) NULL
     , kommentar TEXT
     , kategorie VARCHAR(255) NULL
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sueberweisung (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , bezeichnung VARCHAR(255) NOT NULL
     , termin DATE NOT NULL
     , ausgefuehrt int(10) NOT NULL
     , ausgefuehrt_am DATETIME
     , warnungen int(1)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE umsatztyp (
       id int(10) AUTO_INCREMENT
     , name VARCHAR(255) NOT NULL
     , nummer VARCHAR(5)
     , pattern TEXT
     , isregex int(10)
     , umsatztyp int(10)
     , parent_id int(10)
     , color VARCHAR(11)
     , customcolor int(1)
     , kommentar TEXT
     , konto_id int(10) NULL
     , konto_kategorie VARCHAR(255) NULL
     , flags int(1)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE slastschrift (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , bezeichnung VARCHAR(255) NOT NULL
     , termin DATE NOT NULL
     , ausgefuehrt int(10) NOT NULL
     , ausgefuehrt_am DATETIME
     , warnungen int(1)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE protokoll (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , kommentar TEXT NOT NULL
     , datum DATETIME NOT NULL
     , typ int(10) NOT NULL
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE slastbuchung (
       id int(10) AUTO_INCREMENT
     , slastschrift_id int(10) NOT NULL
     , gegenkonto_nr VARCHAR(15) NOT NULL
     , gegenkonto_blz VARCHAR(15) NOT NULL
     , gegenkonto_name VARCHAR(255)
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(27) NOT NULL
     , zweck2 VARCHAR(27)
     , zweck3 TEXT
     , typ VARCHAR(2)
     , warnung VARCHAR(255)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sueberweisungbuchung (
       id int(10) AUTO_INCREMENT
     , sueberweisung_id int(10) NOT NULL
     , gegenkonto_nr VARCHAR(15) NOT NULL
     , gegenkonto_blz VARCHAR(15) NOT NULL
     , gegenkonto_name VARCHAR(255)
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(27) NOT NULL
     , zweck2 VARCHAR(27)
     , zweck3 TEXT
     , typ VARCHAR(2)
     , warnung VARCHAR(255)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE ueberweisung (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , empfaenger_konto VARCHAR(15) NOT NULL
     , empfaenger_blz VARCHAR(15) NOT NULL
     , empfaenger_name VARCHAR(255)
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(27) NOT NULL
     , zweck2 VARCHAR(27)
     , zweck3 TEXT
     , termin DATE NOT NULL
     , banktermin int(10)
     , umbuchung int(1)
     , ausgefuehrt int(10) NOT NULL
     , typ VARCHAR(2)
     , ausgefuehrt_am DATETIME
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE aueberweisung (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , empfaenger_konto VARCHAR(40) NOT NULL
     , empfaenger_name VARCHAR(140) NOT NULL
     , empfaenger_bic VARCHAR(15) NULL
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(140)
     , termin DATE NOT NULL
     , banktermin int(10)
     , umbuchung int(1)
     , instantpayment int(1)
     , ausgefuehrt int(10) NOT NULL
     , ausgefuehrt_am DATETIME
     , endtoendid VARCHAR(35)
     , pmtinfid VARCHAR(35)
     , purposecode VARCHAR(10)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sepalastschrift (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , empfaenger_konto VARCHAR(40) NOT NULL
     , empfaenger_name VARCHAR(140) NOT NULL
     , empfaenger_bic VARCHAR(15) NULL
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(140)
     , termin DATE NOT NULL
     , ausgefuehrt int(10) NOT NULL
     , ausgefuehrt_am DATETIME
     , endtoendid VARCHAR(35)
     , creditorid VARCHAR(35) NOT NULL
     , mandateid VARCHAR(35) NOT NULL
     , sigdate DATE NOT NULL
     , sequencetype VARCHAR(8) NOT NULL
     , sepatype VARCHAR(8)
     , targetdate DATE
     , orderid VARCHAR(255)
     , pmtinfid VARCHAR(35)
     , purposecode VARCHAR(10)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sepaslast (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , bezeichnung VARCHAR(255) NOT NULL
     , sequencetype VARCHAR(8) NOT NULL
     , sepatype VARCHAR(8)
     , targetdate DATE
     , termin DATE NOT NULL
     , ausgefuehrt int(10) NOT NULL
     , ausgefuehrt_am DATETIME
     , orderid VARCHAR(255)
     , pmtinfid VARCHAR(35)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sepaslastbuchung (
       id int(10) AUTO_INCREMENT
     , sepaslast_id int(10) NOT NULL
     , empfaenger_konto VARCHAR(40) NOT NULL
     , empfaenger_name VARCHAR(140) NOT NULL
     , empfaenger_bic VARCHAR(15) NULL
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(140)
     , endtoendid VARCHAR(35)
     , creditorid VARCHAR(35) NOT NULL
     , mandateid VARCHAR(35) NOT NULL
     , sigdate DATE NOT NULL
     , purposecode VARCHAR(10)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sepasueb (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , bezeichnung VARCHAR(255) NOT NULL
     , termin DATE NOT NULL
     , banktermin int(10)
     , ausgefuehrt int(10) NOT NULL
     , ausgefuehrt_am DATETIME
     , pmtinfid VARCHAR(35)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sepasuebbuchung (
       id int(10) AUTO_INCREMENT
     , sepasueb_id int(10) NOT NULL
     , empfaenger_konto VARCHAR(40) NOT NULL
     , empfaenger_name VARCHAR(140) NOT NULL
     , empfaenger_bic VARCHAR(15) NULL
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(140)
     , endtoendid VARCHAR(35)
     , purposecode VARCHAR(10)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE umsatz (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , empfaenger_konto VARCHAR(40)
     , empfaenger_blz VARCHAR(15)
     , empfaenger_name VARCHAR(255)
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(255)
     , zweck2 VARCHAR(35)
     , zweck3 TEXT
     , datum DATE NOT NULL
     , valuta DATE NOT NULL
     , saldo DOUBLE
     , primanota VARCHAR(100)
     , art VARCHAR(500)
     , customerref VARCHAR(100)
     , kommentar TEXT
     , checksum bigint(16)
     , umsatztyp_id int(10)
     , flags int(1)
     , gvcode varchar(3)
     , addkey varchar(3)
     , txid varchar(100)
     , purposecode varchar(10)
     , endtoendid varchar(100)
     , mandateid varchar(100)
     , empfaenger_name2 varchar(255)
     , creditorid varchar(35)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE dauerauftrag (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , empfaenger_konto VARCHAR(15) NOT NULL
     , empfaenger_blz VARCHAR(15) NOT NULL
     , empfaenger_name VARCHAR(255)
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(27) NOT NULL
     , zweck2 VARCHAR(27)
     , zweck3 TEXT
     , erste_zahlung DATE NOT NULL
     , letzte_zahlung DATE
     , orderid VARCHAR(100)
     , zeiteinheit int(10) NOT NULL
     , intervall int(10) NOT NULL
     , tag int(10) NOT NULL
     , typ VARCHAR(2)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE sepadauerauftrag (
       id int(10) AUTO_INCREMENT
     , konto_id int(10) NOT NULL
     , empfaenger_konto VARCHAR(40) NOT NULL
     , empfaenger_name VARCHAR(140) NOT NULL
     , empfaenger_bic VARCHAR(15) NULL
     , betrag DOUBLE NOT NULL
     , zweck VARCHAR(140)
     , erste_zahlung DATE NOT NULL
     , letzte_zahlung DATE
     , orderid VARCHAR(100)
     , endtoendid VARCHAR(35)
     , zeiteinheit int(10) NOT NULL
     , intervall int(10) NOT NULL
     , tag int(10) NOT NULL
     , canchange int(1)
     , candelete int(1)
     , pmtinfid VARCHAR(35)
     , purposecode VARCHAR(10)
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE version (
       id int(10) AUTO_INCREMENT
     , name VARCHAR(255) NOT NULL
     , version int(10) NOT NULL
     , UNIQUE (id)
     , PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE property (
  id int(10) AUTO_INCREMENT,
  name text NOT NULL,
  content text NULL,
  UNIQUE (id),
  UNIQUE KEY name (name(255)),
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE reminder (
  id int(10) AUTO_INCREMENT,
  uuid varchar(255) NOT NULL,
  content text NOT NULL,
  UNIQUE (id),
  UNIQUE (uuid),
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE kontoauszug (
  id int(10) AUTO_INCREMENT,
  konto_id int(10) NOT NULL,
  ausgefuehrt_am datetime,
  kommentar TEXT,
  pfad TEXT,
  dateiname TEXT,
  uuid varchar(255),
  format varchar(5),
  erstellungsdatum date,
  von date,
  bis date,
  jahr int(4),
  nummer int(5),
  name1 varchar(255),
  name2 varchar(255),
  name3 varchar(255),
  quittungscode TEXT,
  quittiert_am datetime,
  gelesen_am datetime,
  UNIQUE (id),
  PRIMARY KEY (id)
) ENGINE=InnoDB;


CREATE INDEX idx_lastschrift_konto ON lastschrift(konto_id);
CREATE INDEX idx_sueberweisung_konto ON sueberweisung(konto_id);
CREATE INDEX idx_umsatztyp_umsatztyp ON umsatztyp(parent_id);
CREATE INDEX idx_slastschrift_konto ON slastschrift(konto_id);
CREATE INDEX idx_protokoll_konto ON protokoll(konto_id);
CREATE INDEX idx_slastbuchung_slastschrift ON slastbuchung(slastschrift_id);
CREATE INDEX idx_sueberweisungbuchung_sueberweisung ON sueberweisungbuchung(sueberweisung_id);
CREATE INDEX idx_ueberweisung_konto ON ueberweisung(konto_id);
CREATE INDEX idx_umsatz_konto ON umsatz(konto_id);
CREATE INDEX idx_umsatz_umsatztyp ON umsatz(umsatztyp_id);
CREATE INDEX idx_dauerauftrag_konto ON dauerauftrag(konto_id);
CREATE INDEX idx_aueberweisung_konto ON aueberweisung(konto_id);
CREATE INDEX idx_reminder_uuid ON reminder(uuid);
CREATE INDEX idx_sepalast_konto ON sepalastschrift(konto_id);
CREATE INDEX idx_sepaslast_konto ON sepaslast(konto_id);
CREATE INDEX idx_sepaslastbuchung_sepaslast ON sepaslastbuchung(sepaslast_id);
CREATE INDEX idx_sepasueb_konto ON sepasueb(konto_id);
CREATE INDEX idx_sepasuebbuchung_sepasueb ON sepasuebbuchung(sepasueb_id);
CREATE INDEX idx_sepadauerauftrag_konto ON sepadauerauftrag(konto_id);
CREATE INDEX idx_kontoauszug_konto ON kontoauszug(konto_id);
CREATE INDEX idx_kontoauszug_gelesen ON kontoauszug(gelesen_am);

SET FOREIGN_KEY_CHECKS=OFF;
ALTER TABLE lastschrift ADD CONSTRAINT fk_lastschrift_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE sueberweisung ADD CONSTRAINT fk_sueberweisung_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE umsatztyp ADD CONSTRAINT fk_umsatztyp_umsatztyp FOREIGN KEY (parent_id) REFERENCES umsatztyp (id);
ALTER TABLE slastschrift ADD CONSTRAINT fk_slastschrift_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE protokoll ADD CONSTRAINT fk_protokoll_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE slastbuchung ADD CONSTRAINT fk_slastbuchung_slastschrift FOREIGN KEY (slastschrift_id) REFERENCES slastschrift (id);
ALTER TABLE sueberweisungbuchung ADD CONSTRAINT fk_sueberweisungbuchung_sueberweisung FOREIGN KEY (sueberweisung_id) REFERENCES sueberweisung (id);
ALTER TABLE ueberweisung ADD CONSTRAINT fk_ueberweisung_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE umsatz ADD CONSTRAINT fk_umsatz_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE umsatz ADD CONSTRAINT fk_umsatz_umsatztyp FOREIGN KEY (umsatztyp_id) REFERENCES umsatztyp (id);
ALTER TABLE dauerauftrag ADD CONSTRAINT fk_dauerauftrag_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE aueberweisung ADD CONSTRAINT fk_aueberweisung_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE sepalastschrift ADD CONSTRAINT fk_sepalast_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE sepaslast ADD CONSTRAINT fk_sepaslast_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE sepaslastbuchung ADD CONSTRAINT fk_sepaslastbuchung_sepaslast FOREIGN KEY (sepaslast_id) REFERENCES sepaslast (id);
ALTER TABLE sepasueb ADD CONSTRAINT fk_sepasueb_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE sepasuebbuchung ADD CONSTRAINT fk_sepasuebbuchung_sepasueb FOREIGN KEY (sepasueb_id) REFERENCES sepasueb (id);
ALTER TABLE sepadauerauftrag ADD CONSTRAINT fk_sepadauerauftrag_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE kontoauszug ADD CONSTRAINT fk_kontoauszug_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
ALTER TABLE umsatztyp ADD CONSTRAINT fk_umsatztyp_konto FOREIGN KEY (konto_id) REFERENCES konto (id);
SET FOREIGN_KEY_CHECKS=ON;

-- Bevor wir Daten speichern koennen, muessen wir ein COMMIT machen
COMMIT;

INSERT INTO turnus (zeiteinheit,intervall,tag,initial)
  VALUES (2,1,1,1);

INSERT INTO turnus (zeiteinheit,intervall,tag,initial)
  VALUES (2,1,15,1);

INSERT INTO turnus (zeiteinheit,intervall,tag,initial)
  VALUES (2,3,1,1);

INSERT INTO turnus (zeiteinheit,intervall,tag,initial)
  VALUES (2,6,1,1);

INSERT INTO turnus (zeiteinheit,intervall,tag,initial)
  VALUES (2,12,1,1);

INSERT INTO turnus (zeiteinheit,intervall,tag,initial)
  VALUES (1,1,1,1);

ALTER TABLE umsatz ADD INDEX (datum);
ALTER TABLE umsatz ADD INDEX (valuta);
ALTER TABLE umsatz ADD INDEX (flags);
ALTER TABLE protokoll ADD INDEX (datum);
ALTER TABLE ueberweisung ADD INDEX (termin);
ALTER TABLE lastschrift ADD INDEX (termin);

INSERT INTO version (name,version) values ('db',71);
