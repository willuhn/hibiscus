CREATE TABLE konto (
  id NUMERIC default UNIQUEKEY('konto'),
  kontonummer varchar(15) NOT NULL,
  blz varchar(15) NOT NULL,
  name varchar(255) NOT NULL,
  bezeichnung varchar(255),
  kundennummer varchar(255) NOT NULL,
  waehrung varchar(6) NOT NULL,
  passport_class varchar(1000) NOT NULL,
  saldo double,
  saldo_datum date,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE empfaenger (
  id NUMERIC default UNIQUEKEY('empfaenger'),
  kontonummer varchar(15) NOT NULL,
  blz varchar(15) NOT NULL,
  name varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE ueberweisung (
  id NUMERIC default UNIQUEKEY('ueberweisung'),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE protokoll (
  id NUMERIC default UNIQUEKEY('protokoll'),
  konto_id int(4) NOT NULL,
  kommentar varchar(1000) NOT NULL,
  datum date NOT NULL,
  typ int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE umsatz (
  id NUMERIC default UNIQUEKEY('umsatz'),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15),
  empfaenger_blz varchar(15),
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(35) NOT NULL,
  zweck2 varchar(35),
  datum date NOT NULL,
  valuta date NOT NULL,
  saldo double,
  primanota varchar(100),
  art varchar(100),
  customerref varchar(100),
  umsatztyp_id int(4),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE umsatztyp (
  id NUMERIC default UNIQUEKEY('umsatztyp'),
  name varchar(255) NOT NULL,
  field varchar(255) NOT NULL,
  pattern varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE dauerauftrag (
  id NUMERIC default UNIQUEKEY('dauerauftrag'),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  erste_zahlung date NOT NULL,
  letzte_zahlung date,
  orderid varchar(20),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE turnus (
  id NUMERIC default UNIQUEKEY('turnus'),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  initial int(1),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE lastschrift (
  id NUMERIC default UNIQUEKEY('lastschrift'),
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
  id NUMERIC default UNIQUEKEY('slastschrift'),
  konto_id int(4) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE slastbuchung (
  id NUMERIC default UNIQUEKEY('slastbuchung'),
  slastschrift_id int(4) NOT NULL,
  gegenkonto_nr varchar(15) NOT NULL,
  gegenkonto_blz varchar(15) NOT NULL,
  gegenkonto_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE systemnachricht (
  id NUMERIC default UNIQUEKEY('systemnachricht'),
  blz varchar(15) NOT NULL,
  nachricht text NOT NULL,
  datum date NOT NULL,
  gelesen int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE filterpattern (
  id NUMERIC default UNIQUEKEY('filterpattern'),
  field varchar(255) NOT NULL,
  typ int(1) NOT NULL,
  pattern varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE offeneposten (
  id NUMERIC default UNIQUEKEY('offeneposten'),
  bezeichnung varchar(255) NOT NULL,
  offen int(1) NOT NULL,
  datum date NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE op_pattern (
  id NUMERIC default UNIQUEKEY('op_pattern'),
  offeneposten_id int(4) NOT NULL,
  filterpattern_id int(4) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);


ALTER TABLE ueberweisung ADD CONSTRAINT fk_konto FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE umsatz ADD CONSTRAINT fk_konto2 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE protokoll ADD CONSTRAINT fk_konto3 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE umsatz ADD CONSTRAINT fk_umsatztyp FOREIGN KEY (umsatztyp_id) REFERENCES umsatztyp (id) DEFERRABLE;
ALTER TABLE dauerauftrag ADD CONSTRAINT fk_konto4 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE lastschrift ADD CONSTRAINT fk_konto5 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE slastschrift ADD CONSTRAINT fk_konto6 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE slastbuchung ADD CONSTRAINT fk_slastschrift1 FOREIGN KEY (slastschrift_id) REFERENCES slastschrift (id) DEFERRABLE;
ALTER TABLE op_pattern ADD CONSTRAINT fk_offeneposten_1 FOREIGN KEY (offeneposten_id) REFERENCES offeneposten (id) DEFERRABLE;
ALTER TABLE op_pattern ADD CONSTRAINT fk_filterpattern_1 FOREIGN KEY (filterpattern_id) REFERENCES filterpattern (id) DEFERRABLE;

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
  