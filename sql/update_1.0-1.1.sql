ALTER CREATE TABLE login (
  id NUMERIC default UNIQUEKEY('login'),
  username varchar(100) NOT NULL,
  password varchar(100) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE konto (
  id NUMERIC default UNIQUEKEY('konto'),
  login_id int(4) NULL,
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

ALTER CREATE TABLE dauerauftrag (
  id NUMERIC default UNIQUEKEY('dauerauftrag'),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15) NOT NULL,
  empfaenger_blz varchar(15) NOT NULL,
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(27) NOT NULL,
  zweck2 varchar(27) NULL,
  erste_zahlung date NOT NULL,
  letzte_zahlung date,
  orderid varchar(20),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE turnus (
  id NUMERIC default UNIQUEKEY('turnus'),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  initial int(1),
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE lastschrift (
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
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER TABLE dauerauftrag ADD CONSTRAINT fk_konto4 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE konto ADD CONSTRAINT fk_login FOREIGN KEY (login_id) REFERENCES login (id) DEFERRABLE;
ALTER TABLE lastschrift ADD CONSTRAINT fk_konto5 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;

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
