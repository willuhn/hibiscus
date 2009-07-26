CREATE TABLE konto (
  id serial primary key,
  kontonummer varchar(15) NOT NULL,
  unterkonto varchar(10) null,
  blz varchar(15) NOT NULL,
  name varchar(255) NOT NULL,
  bezeichnung varchar(255),
  kundennummer varchar(255) NOT NULL,
  waehrung varchar(6) NOT NULL,
  passport_class varchar(1000) NOT NULL,
  saldo float,
  saldo_datum timestamp,
  kommentar varchar(1000) NULL
);

CREATE TABLE empfaenger (
  id serial primary key,
  kontonummer varchar(15) NULL,
  blz varchar(15) NULL,
  name varchar(27) NOT NULL,
  iban varchar(40) NULL,
  bic varchar(15) NULL,
  bank varchar(140) NULL,
  kommentar varchar(1000) NULL
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
  umbuchung int(1) NULL
);

CREATE TABLE aueberweisung (
  id serial primary key,
  konto_id integer NOT NULL,
  empfaenger_konto varchar(40) NOT NULL,
  empfaenger_bank varchar(140) NOT NULL,
  empfaenger_name varchar(140) NOT NULL,
  empfaenger_bic varchar(15) NULL,
  betrag float NOT NULL,
  zweck varchar(140),
  termin date NOT NULL,
  ausgefuehrt integer NOT NULL
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
  zweck varchar(35),
  zweck2 varchar(35),
  zweck3 varchar(1000),
  datum date NOT NULL,
  valuta date NOT NULL,
  saldo float,
  primanota varchar(100),
  art varchar(100),
  customerref varchar(100),
  kommentar varchar(1000) NULL,
  checksum numeric NULL,
  umsatztyp_id integer NULL,
  flags integer NULL
);

CREATE TABLE umsatztyp (
  id serial primary key,
  name varchar(255) NOT NULL,
  nummer varchar(5) NULL,
  pattern varchar(255) NULL,
  isregex integer NULL,
  umsatztyp integer NULL,
  parent_id integer NULL,
  color varchar(11) NULL,
  customcolor int(1) NULL
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
  tag integer NOT NULL
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
  typ varchar(2) NULL
);

CREATE TABLE slastschrift (
  id serial primary key,
  konto_id integer NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt integer NOT NULL
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
  typ varchar(2) NULL
);

CREATE TABLE sueberweisung (
  id serial primary key,
  konto_id integer NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt integer NOT NULL
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
  typ varchar(2) NULL
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

CREATE TABLE op (
  id serial primary key,
  name varchar(255) not NULL,
  pattern varchar(255) NULL,
  isregex integer NULL,
  betrag float NOT NULL,
  termin date NULL,
  kommentar varchar(1000) NULL
);

CREATE TABLE op_buchung (
  id serial primary key,
  umsatz_id integer NOT NULL,
  op_id integer NOT NULL
);

CREATE TABLE property (
  id serial primary key,
  name varchar(1000) NOT NULL,
  content varchar(1000) NULL
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
  
INSERT INTO version (name,version) values ('db',23);
