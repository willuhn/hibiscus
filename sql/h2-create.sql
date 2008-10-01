CREATE TABLE konto (
  id IDENTITY,
  kontonummer varchar(15) NOT NULL,
  unterkonto varchar(10) null,
  blz varchar(15) NOT NULL,
  name varchar(255) NOT NULL,
  bezeichnung varchar(255),
  kundennummer varchar(255) NOT NULL,
  waehrung varchar(6) NOT NULL,
  passport_class varchar(1000) NOT NULL,
  saldo double,
  saldo_datum datetime,
  synchronize int(1) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE empfaenger (
  id IDENTITY,
  kontonummer varchar(15) NOT NULL,
  blz varchar(15) NOT NULL,
  name varchar(27) NOT NULL,
  kommentar varchar(1000) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE ueberweisung (
  id IDENTITY,
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  termin date NOT NULL,
  banktermin int(1) NULL,
  ausgefuehrt int(1) NOT NULL,
  typ varchar(2) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE protokoll (
  id IDENTITY,
  konto_id int(4) NOT NULL,
  kommentar varchar(1000) NOT NULL,
  datum datetime NOT NULL,
  typ int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE umsatz (
  id IDENTITY,
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
  kommentar varchar(1000) NULL,
  checksum numeric NULL,
  umsatztyp_id int(5) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE umsatztyp (
  id IDENTITY,
  name varchar(255) NOT NULL,
  nummer varchar(5) NULL,
  pattern varchar(255) NULL,
  isregex int(1) NULL,
  umsatztyp int(1) NULL,
  parent_id int(5) NULL,
  UNIQUE (id),
  UNIQUE (name),
  PRIMARY KEY (id)
);

CREATE TABLE dauerauftrag (
  id IDENTITY,
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  erste_zahlung date NOT NULL,
  letzte_zahlung date,
  orderid varchar(100),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE turnus (
  id IDENTITY,
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  initial int(1),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE lastschrift (
  id IDENTITY,
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  typ varchar(2) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE slastschrift (
  id IDENTITY,
  konto_id int(4) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE slastbuchung (
  id IDENTITY,
  slastschrift_id int(4) NOT NULL,
  gegenkonto_nr varchar(15) NOT NULL,
  gegenkonto_blz varchar(15) NOT NULL,
  gegenkonto_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  typ varchar(2) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sueberweisung (
  id IDENTITY,
  konto_id int(4) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE sueberweisungbuchung (
  id IDENTITY,
  sueberweisung_id int(4) NOT NULL,
  gegenkonto_nr varchar(15) NOT NULL,
  gegenkonto_blz varchar(15) NOT NULL,
  gegenkonto_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  typ varchar(2) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE systemnachricht (
  id IDENTITY,
  blz varchar(15) NOT NULL,
  nachricht varchar(1000) NOT NULL,
  datum date NOT NULL,
  gelesen int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE version (
  id IDENTITY,
  name varchar(255) NOT NULL,
  version int(5) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE op (
  id IDENTITY,
  name varchar(255) not NULL,
  pattern varchar(255) NULL,
  isregex int(1) NULL,
  betrag double NOT NULL,
  termin date NULL,
  kommentar varchar(1000) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE op_buchung (
  id IDENTITY,
  umsatz_id int(10) NOT NULL,
  op_id int(10) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE verwendungszweck (
  id IDENTITY,
  typ int(1) NOT NULL,
  auftrag_id int(10) NOT NULL,
  zweck varchar(27) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE property (
  id IDENTITY,
  name varchar(1000) NOT NULL,
  content varchar(1000) NULL,
  UNIQUE (id),
  UNIQUE (name),
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
  
INSERT INTO version (name,version) values ('db',11);
  
COMMIT;
