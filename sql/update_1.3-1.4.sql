ALTER CREATE TABLE konto (
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
  synchronize int(1) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE systemnachricht (
  id NUMERIC default UNIQUEKEY('systemnachricht'),
  blz varchar(15) NOT NULL,
  nachricht text NOT NULL,
  datum date NOT NULL,
  gelesen int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

-- Kommentar-Feld hinzugefuegt und "ZWECK" nullable
-- Feld "umsatztyp_id" entfernt
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
  UNIQUE (id),
  PRIMARY KEY (id)
);

-- Name auf 27 Zeichen gekuerzt
ALTER CREATE TABLE empfaenger (
  id NUMERIC default UNIQUEKEY('empfaenger'),
  kontonummer varchar(15) NOT NULL,
  blz varchar(15) NOT NULL,
  name varchar(27) NOT NULL,
  kommentar varchar(1000) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE sueberweisung (
  id NUMERIC default UNIQUEKEY('sueberweisung'),
  konto_id int(4) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE sueberweisungbuchung (
  id NUMERIC default UNIQUEKEY('sueberweisungbuchung'),
  sueberweisung_id int(4) NOT NULL,
  gegenkonto_nr varchar(15) NOT NULL,
  gegenkonto_blz varchar(15) NOT NULL,
  gegenkonto_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27),
  UNIQUE (id),
  PRIMARY KEY (id)
);

-- Feld "banktermin" fuer Terminueberweisungen hinzugefuegt.
ALTER CREATE TABLE ueberweisung (
  id NUMERIC default UNIQUEKEY('ueberweisung'),
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
  UNIQUE (id),
  PRIMARY KEY (id)
);

-- Feld "patterntype" hinzugefuegt
ALTER CREATE TABLE umsatztyp (
  id NUMERIC default UNIQUEKEY('umsatztyp'),
  name varchar(255) NOT NULL,
  field varchar(255) NOT NULL,
  pattern varchar(255) NOT NULL,
  patterntype int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE umsatzzuordnung (
  id NUMERIC default UNIQUEKEY('umsatzzuordnung'),
  umsatztyp_id int(4) NOT NULL,
  umsatz_id int(4) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);


ALTER TABLE sueberweisung ADD CONSTRAINT fk_konto7 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE sueberweisungbuchung ADD CONSTRAINT fk_sueberweisung1 FOREIGN KEY (sueberweisung_id) REFERENCES sueberweisung (id) DEFERRABLE;

ALTER TABLE umsatzzuordnung ADD CONSTRAINT fk_umsatz FOREIGN KEY (umsatz_id) REFERENCES umsatz (id) DEFERRABLE;
ALTER TABLE umsatzzuordnung ADD CONSTRAINT fk_umsatztyp FOREIGN KEY (umsatztyp_id) REFERENCES umsatztyp (id) DEFERRABLE;

