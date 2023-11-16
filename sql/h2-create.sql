CREATE TABLE konto (
  id IDENTITY(1),
  kontonummer varchar(16) NOT NULL,
  unterkonto varchar(30) null,
  blz varchar(15) NOT NULL,
  name varchar(255) NOT NULL,
  bezeichnung varchar(255),
  kundennummer varchar(255) NOT NULL,
  waehrung varchar(6) NOT NULL,
  passport_class varchar(1000),
  saldo double,
  saldo_datum datetime,
  kommentar varchar(1000) NULL,
  flags int(1) NULL,
  iban varchar(40) NULL,
  bic varchar(15) NULL,
  saldo_available double,
  kategorie varchar(255) NULL,
  backend_class varchar(1000),
  acctype int(2) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE empfaenger (
  id IDENTITY(1),
  kontonummer varchar(15) NULL,
  blz varchar(15) NULL,
  name varchar(255) NOT NULL,
  iban varchar(40) NULL,
  bic varchar(15) NULL,
  bank varchar(140) NULL,
  kommentar varchar(1000) NULL,
  kategorie varchar(255) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE ueberweisung (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  termin date NOT NULL,
  banktermin int(1) NULL,
  umbuchung int(1) NULL,
  ausgefuehrt int(1) NOT NULL,
  typ varchar(2) NULL,
  ausgefuehrt_am datetime NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE aueberweisung (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15) NULL,
  betrag double NOT NULL,
  zweck varchar(140),
  termin date NOT NULL,
  banktermin int(1) NULL,
  umbuchung int(1) NULL,
  instantpayment int(1) NULL,
  ausgefuehrt int(1) NOT NULL,
  ausgefuehrt_am datetime NULL,
  endtoendid varchar(35),
  pmtinfid varchar(35),
  purposecode varchar(10),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sepalastschrift (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15) NULL,
  betrag double NOT NULL,
  zweck varchar(140),
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  ausgefuehrt_am datetime NULL,
  endtoendid varchar(35),
  creditorid varchar(35) NOT NULL,
  mandateid varchar(35) NOT NULL,
  sigdate date NOT NULL,
  sequencetype varchar(8) NOT NULL,
  sepatype varchar(8) NULL,
  targetdate date NULL,
  orderid varchar(255) NULL,
  pmtinfid varchar(35),
  purposecode varchar(10),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sepaslast (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  sequencetype varchar(8) NOT NULL,
  sepatype varchar(8) NULL,
  targetdate date NULL,
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  ausgefuehrt_am datetime NULL,
  orderid varchar(255) NULL,
  pmtinfid varchar(35),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sepaslastbuchung (
  id IDENTITY(1),
  sepaslast_id int(4) NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15) NULL,
  betrag double NOT NULL,
  zweck varchar(140),
  endtoendid varchar(35),
  creditorid varchar(35) NOT NULL,
  mandateid varchar(35) NOT NULL,
  sigdate date NOT NULL,
  purposecode varchar(10),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sepasueb (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  banktermin int(1) NULL,
  ausgefuehrt int(1) NOT NULL,
  ausgefuehrt_am datetime NULL,
  pmtinfid varchar(35),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sepasuebbuchung (
  id IDENTITY(1),
  sepasueb_id int(4) NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15) NULL,
  betrag double NOT NULL,
  zweck varchar(140),
  endtoendid varchar(35),
  purposecode varchar(10),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE protokoll (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  kommentar varchar(1000) NOT NULL,
  datum datetime NOT NULL,
  typ int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE umsatz (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(40),
  empfaenger_blz varchar(15),
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(255),
  zweck2 varchar(35),
  zweck3 varchar(1000),
  datum date NOT NULL,
  valuta date NOT NULL,
  saldo double,
  primanota varchar(100),
  art varchar(500),
  customerref varchar(100),
  kommentar varchar(1000) NULL,
  checksum numeric NULL,
  umsatztyp_id int(5) NULL,
  flags int(1) NULL,
  gvcode varchar(3) NULL,
  addkey varchar(3) NULL,
  txid varchar(100),
  purposecode varchar(10),
  endtoendid varchar(100),
  mandateid varchar(100),
  empfaenger_name2 varchar(255),
  creditorid varchar(35),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE umsatztyp (
  id IDENTITY(1),
  name varchar(255) NOT NULL,
  nummer varchar(5) NULL,
  pattern varchar(1000) NULL,
  isregex int(1) NULL,
  umsatztyp int(1) NULL,
  parent_id int(5) NULL,
  color varchar(11) NULL,
  customcolor int(1) NULL,
  kommentar varchar(1000) NULL,
  konto_id int(4) NULL,
  konto_kategorie varchar(255) NULL,
  flags int(1) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE dauerauftrag (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  erste_zahlung date NOT NULL,
  letzte_zahlung date,
  orderid varchar(100),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  typ varchar(2) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sepadauerauftrag (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15) NULL,
  betrag double NOT NULL,
  zweck varchar(140),
  erste_zahlung date NOT NULL,
  letzte_zahlung date,
  orderid varchar(100),
  endtoendid varchar(35),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  canchange int(1) NULL,
  candelete int(1) NULL,
  pmtinfid varchar(35),
  purposecode varchar(10),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE turnus (
  id IDENTITY(1),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  initial int(1),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE lastschrift (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  typ varchar(2) NULL,
  ausgefuehrt_am datetime NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE slastschrift (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  ausgefuehrt_am datetime NULL,
  warnungen int(1) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE slastbuchung (
  id IDENTITY(1),
  slastschrift_id int(4) NOT NULL,
  gegenkonto_nr varchar(15) NOT NULL,
  gegenkonto_blz varchar(15) NOT NULL,
  gegenkonto_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  typ varchar(2) NULL,
  warnung varchar(255),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sueberweisung (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  ausgefuehrt_am datetime NULL,
  warnungen int(1) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sueberweisungbuchung (
  id IDENTITY(1),
  sueberweisung_id int(4) NOT NULL,
  gegenkonto_nr varchar(15) NOT NULL,
  gegenkonto_blz varchar(15) NOT NULL,
  gegenkonto_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  typ varchar(2) NULL,
  warnung varchar(255),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE systemnachricht (
  id IDENTITY(1),
  blz varchar(15) NOT NULL,
  nachricht varchar(4000) NOT NULL,
  datum date NOT NULL,
  gelesen int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE version (
  id IDENTITY(1),
  name varchar(255) NOT NULL,
  version int(5) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE property (
  id IDENTITY(1),
  name varchar(1000) NOT NULL,
  content varchar(20000) NULL,
  UNIQUE (id),
  UNIQUE (name),
  PRIMARY KEY (id)
);

CREATE TABLE reminder (
  id IDENTITY(1),
  uuid varchar(255) NOT NULL,
  content varchar(60000) NOT NULL,
  UNIQUE (id),
  UNIQUE (uuid),
  PRIMARY KEY (id)
);

CREATE TABLE kontoauszug (
  id IDENTITY(1),
  konto_id int(4) NOT NULL,
  ausgefuehrt_am datetime,
  kommentar varchar(1000),
  pfad varchar(1000),
  dateiname varchar(256),
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
  quittungscode varchar(1000),
  quittiert_am datetime,
  gelesen_am datetime,
  UNIQUE (id),
  PRIMARY KEY (id)
);


ALTER TABLE ueberweisung ADD CONSTRAINT fk_konto FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE umsatz ADD CONSTRAINT fk_konto2 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE protokoll ADD CONSTRAINT fk_konto3 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE dauerauftrag ADD CONSTRAINT fk_konto4 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE lastschrift ADD CONSTRAINT fk_konto5 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE slastschrift ADD CONSTRAINT fk_konto6 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE slastbuchung ADD CONSTRAINT fk_slastschrift1 FOREIGN KEY (slastschrift_id) REFERENCES slastschrift (id) DEFERRABLE;
ALTER TABLE sueberweisung ADD CONSTRAINT fk_konto7 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE sueberweisungbuchung ADD CONSTRAINT fk_sueberweisung1 FOREIGN KEY (sueberweisung_id) REFERENCES sueberweisung (id) DEFERRABLE;
ALTER TABLE aueberweisung ADD CONSTRAINT fk_konto8 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE sepalastschrift ADD CONSTRAINT fk_konto9 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE sepaslast ADD CONSTRAINT fk_konto10 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE sepaslastbuchung ADD CONSTRAINT fk_sepaslast1 FOREIGN KEY (sepaslast_id) REFERENCES sepaslast (id) DEFERRABLE;
ALTER TABLE sepasueb ADD CONSTRAINT fk_konto11 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE sepasuebbuchung ADD CONSTRAINT fk_sepasueb1 FOREIGN KEY (sepasueb_id) REFERENCES sepasueb (id) DEFERRABLE;
ALTER TABLE sepadauerauftrag ADD CONSTRAINT fk_konto12 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE kontoauszug ADD CONSTRAINT fk_konto13 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE umsatztyp ADD CONSTRAINT fk_konto14 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;

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
  
CREATE INDEX idx_umsatz_datum ON umsatz(datum);
CREATE INDEX idx_umsatz_valuta ON umsatz(valuta);
CREATE INDEX idx_umsatz_flags ON umsatz(flags);
  
INSERT INTO version (name,version) values ('db',71);
  
COMMIT;
