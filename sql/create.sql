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
  zweck2 varchar(27) NOT NULL,
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
  turnus_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27) NOT NULL,
  erste_zahlung date NOT NULL,
  letzte_zahlung date,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE turnus (
  id NUMERIC default UNIQUEKEY('turnus'),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);


ALTER TABLE ueberweisung ADD CONSTRAINT fk_konto FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE umsatz ADD CONSTRAINT fk_konto2 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE protokoll ADD CONSTRAINT fk_konto3 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE umsatz ADD CONSTRAINT fk_umsatztyp FOREIGN KEY (umsatztyp_id) REFERENCES umsatztyp (id) DEFERRABLE;
ALTER TABLE dauerauftrag ADD CONSTRAINT fk_konto4 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE dauerauftrag ADD CONSTRAINT fk_turnus FOREIGN KEY (turnus_id) REFERENCES turnus (id) DEFERRABLE;
