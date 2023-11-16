CREATE TABLE konto (
  id serial primary key,
  kontonummer varchar(16) NOT NULL,
  unterkonto varchar(30) null,
  blz varchar(15) NOT NULL,
  name varchar(255) NOT NULL,
  bezeichnung varchar(255),
  kundennummer varchar(255) NOT NULL,
  waehrung varchar(6) NOT NULL,
  passport_class varchar(1000),
  saldo float,
  saldo_datum timestamp,
  kommentar varchar(1000) NULL,
  iban varchar(40) NULL,
  bic varchar(15) NULL,
  flags integer NULL,
  saldo_available float,
  kategorie varchar(255) NULL,
  backend_class varchar(1000),
  acctype integer NULL
);

CREATE TABLE empfaenger (
  id serial primary key,
  kontonummer varchar(15) NULL,
  blz varchar(15) NULL,
  name varchar(255) NOT NULL,
  iban varchar(40) NULL,
  bic varchar(15) NULL,
  bank varchar(140) NULL,
  kommentar varchar(1000) NULL,
  kategorie varchar(255) NULL
);

CREATE TABLE ueberweisung (
  id serial primary key,
  konto_id integer NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag float NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  termin date NOT NULL,
  banktermin integer NULL,
  ausgefuehrt integer NOT NULL,
  typ varchar(2) NULL,
  umbuchung integer NULL,
  ausgefuehrt_am timestamp
);

CREATE TABLE aueberweisung (
  id serial primary key,
  konto_id integer NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15) NULL,
  betrag float NOT NULL,
  zweck varchar(140),
  termin date NOT NULL,
  banktermin integer NULL,
  umbuchung integer NULL,
  instantpayment integer NULL,
  ausgefuehrt integer NOT NULL,
  ausgefuehrt_am timestamp,
  endtoendid varchar(35) NULL,
  pmtinfid varchar(35) NULL,
  purposecode varchar(10) NULL
);

CREATE TABLE sepalastschrift (
  id serial primary key,
  konto_id integer NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15),
  betrag float NOT NULL,
  zweck varchar(140),
  termin date NOT NULL,
  ausgefuehrt integer NOT NULL,
  ausgefuehrt_am timestamp,
  endtoendid varchar(35),
  creditorid varchar(35) NOT NULL,
  mandateid varchar(35) NOT NULL,
  sigdate date NOT NULL,
  sequencetype varchar(8) NOT NULL,
  sepatype varchar(8),
  targetdate date,
  orderid varchar(255),
  pmtinfid varchar(35),
  purposecode varchar(10)
);

CREATE TABLE sepaslast (
  id serial primary key,
  konto_id integer NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  sequencetype varchar(8) NOT NULL,
  sepatype varchar(8),
  targetdate date,
  termin date NOT NULL,
  ausgefuehrt integer NOT NULL,
  ausgefuehrt_am timestamp,
  orderid varchar(255),
  pmtinfid varchar(35)
);

CREATE TABLE sepaslastbuchung (
  id serial primary key,
  sepaslast_id integer NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15),
  betrag float NOT NULL,
  zweck varchar(140),
  endtoendid varchar(35),
  creditorid varchar(35) NOT NULL,
  mandateid varchar(35) NOT NULL,
  sigdate date NOT NULL,
  purposecode varchar(10)
);

CREATE TABLE sepasueb (
  id serial primary key,
  konto_id integer NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  banktermin integer NULL,
  ausgefuehrt integer NOT NULL,
  ausgefuehrt_am timestamp,
  pmtinfid varchar(35)
);

CREATE TABLE sepasuebbuchung (
  id serial primary key,
  sepasueb_id integer NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15),
  betrag float NOT NULL,
  zweck varchar(140),
  endtoendid varchar(35),
  purposecode varchar(10)
);

CREATE TABLE protokoll (
  id serial primary key,
  konto_id integer NOT NULL,
  kommentar varchar(1000) NOT NULL,
  datum timestamp NOT NULL,
  typ integer NOT NULL
);

CREATE TABLE umsatz (
  id serial primary key,
  konto_id integer NOT NULL,
  empfaenger_konto varchar(40),
  empfaenger_blz varchar(15),
  empfaenger_name varchar(255),
  betrag float NOT NULL,
  zweck varchar(255),
  zweck2 varchar(35),
  zweck3 varchar(1000),
  datum date NOT NULL,
  valuta date NOT NULL,
  saldo float,
  primanota varchar(100),
  art varchar(500),
  customerref varchar(100),
  kommentar varchar(1000) NULL,
  checksum numeric NULL,
  umsatztyp_id integer NULL,
  flags integer NULL,
  gvcode varchar(3) NULL,
  addkey varchar(3) NULL,
  txid varchar(100),
  purposecode varchar(10),
  endtoendid varchar(100),
  mandateid varchar(100),
  empfaenger_name2 varchar(255),
  creditorid varchar(35)
);

CREATE TABLE umsatztyp (
  id serial primary key,
  name varchar(255) NOT NULL,
  nummer varchar(5) NULL,
  pattern varchar(1000) NULL,
  isregex integer NULL,
  umsatztyp integer NULL,
  parent_id integer NULL,
  color varchar(11) NULL,
  customcolor integer NULL,
  kommentar varchar(1000) NULL,
  konto_id integer NULL,
  konto_kategorie varchar(255) NULL,
  flags integer NULL
);

CREATE TABLE dauerauftrag (
  id serial primary key,
  konto_id integer NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag float NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  erste_zahlung date NOT NULL,
  letzte_zahlung date,
  orderid varchar(100),
  zeiteinheit integer NOT NULL,
  intervall integer NOT NULL,
  tag integer NOT NULL,
  typ varchar(2) NULL
);

CREATE TABLE sepadauerauftrag (
  id serial primary key,
  konto_id integer NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15),
  betrag float NOT NULL,
  zweck varchar(140),
  erste_zahlung date NOT NULL,
  letzte_zahlung date,
  orderid varchar(100),
  endtoendid varchar(35),
  zeiteinheit integer NOT NULL,
  intervall integer NOT NULL,
  tag integer NOT NULL,
  canchange integer NULL,
  candelete integer NULL,
  pmtinfid varchar(35),
  purposecode varchar(10) NULL
);

CREATE TABLE turnus (
  id serial primary key,
  zeiteinheit integer NOT NULL,
  intervall integer NOT NULL,
  tag integer NOT NULL,
  initial integer
);

CREATE TABLE lastschrift (
  id serial primary key,
  konto_id integer NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag float NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  termin date NOT NULL,
  ausgefuehrt integer NOT NULL,
  typ varchar(2) NULL,
  ausgefuehrt_am timestamp
);

CREATE TABLE slastschrift (
  id serial primary key,
  konto_id integer NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt integer NOT NULL,
  ausgefuehrt_am timestamp,
  warnungen integer NULL
);

CREATE TABLE slastbuchung (
  id serial primary key,
  slastschrift_id integer NOT NULL,
  gegenkonto_nr varchar(15) NOT NULL,
  gegenkonto_blz varchar(15) NOT NULL,
  gegenkonto_name varchar(255),
  betrag float NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  typ varchar(2) NULL,
  warnung varchar(255)
);

CREATE TABLE sueberweisung (
  id serial primary key,
  konto_id integer NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt integer NOT NULL,
  ausgefuehrt_am timestamp,
  warnungen integer NULL
);

CREATE TABLE sueberweisungbuchung (
  id serial primary key,
  sueberweisung_id integer NOT NULL,
  gegenkonto_nr varchar(15) NOT NULL,
  gegenkonto_blz varchar(15) NOT NULL,
  gegenkonto_name varchar(255),
  betrag float NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  zweck3 varchar(1000),
  typ varchar(2) NULL,
  warnung varchar(255)
);



CREATE TABLE systemnachricht (
  id serial primary key,
  blz varchar(15) NOT NULL,
  nachricht varchar(4000) NOT NULL,
  datum date NOT NULL,
  gelesen integer NOT NULL
);

CREATE TABLE version (
  id serial primary key,
  name varchar(255) NOT NULL,
  version integer NOT NULL
);

CREATE TABLE property (
  id serial primary key,
  name varchar(1000) NOT NULL,
  content varchar(20000) NULL
);

CREATE TABLE reminder (
  id serial primary key,
  uuid varchar(255) NOT NULL,
  content varchar(60000) NOT NULL
);

CREATE TABLE kontoauszug (
  id serial primary key,
  konto_id integer NOT NULL,
  ausgefuehrt_am timestamp,
  kommentar varchar(1000),
  pfad varchar(1000),
  dateiname varchar(256),
  uuid varchar(255),
  format varchar(5),
  erstellungsdatum date,
  von date,
  bis date,
  jahr integer,
  nummer integer,
  name1 varchar(255),
  name2 varchar(255),
  name3 varchar(255),
  quittungscode varchar(1000),
  quittiert_am timestamp,
  gelesen_am timestamp
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
