CREATE TABLE konto (
  id NUMERIC default UNIQUEKEY('konto'),
  kontonummer varchar(15) NOT NULL,
  blz varchar(15) NOT NULL,
  name varchar(255) NOT NULL,
  kundennummer varchar(255) NOT NULL,
  waehrung varchar(6) NOT NULL,
  passport_id int(4) NOT NULL,
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
  empfaenger_id int(4) NOT NULL,
  betrag double NOT NULL,
  zweck varchar(35) NOT NULL,
  zweck2 varchar(35) NOT NULL,
  termin date NOT NULL,
  ausgefuehrt int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE passport (
  id NUMERIC default UNIQUEKEY('passport'),
  name varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE passport_param (
  id NUMERIC default UNIQUEKEY('passport_param'),
  konto_id int(4) NOT NULL,
  name varchar(255) NOT NULL,
  value varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER TABLE konto ADD CONSTRAINT fk_passport FOREIGN KEY (passport_id) REFERENCES passport (id) DEFERRABLE;
ALTER TABLE passport_param ADD CONSTRAINT fk_passport_param FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE ueberweisung ADD CONSTRAINT fk_konto FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE ueberweisung ADD CONSTRAINT fk_empfaenger FOREIGN KEY (empfaenger_id) REFERENCES empfaenger (id) DEFERRABLE;
