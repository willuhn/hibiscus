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

CREATE TABLE umsatz (
  id NUMERIC default UNIQUEKEY('umsatz'),
  konto_id int(4) NOT NULL,
  empfaenger_id int(4),
  betrag double NOT NULL,
  zweck varchar(35) NOT NULL,
  zweck2 varchar(35),
  datum date NOT NULL,
  valuta date NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE passport (
  id NUMERIC default UNIQUEKEY('passport'),
  name varchar(255) NOT NULL,
  passport_type_id int(4) NOT NULL,
  UNIQUE (name),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE passport_type (
  id NUMERIC default UNIQUEKEY('passport_type'),
  name varchar(255) NOT NULL,
  implementor varchar(1000) NOT NULL,
  UNIQUE (name),
  UNIQUE (id),
  PRIMARY KEY (id)
);

CREATE TABLE passport_param (
  id NUMERIC default UNIQUEKEY('passport_param'),
  passport_id int(4) NOT NULL,
  name varchar(255) NOT NULL,
  value varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER TABLE konto ADD CONSTRAINT fk_passport FOREIGN KEY (passport_id) REFERENCES passport (id) DEFERRABLE;
ALTER TABLE passport ADD CONSTRAINT fk_passport_type FOREIGN KEY (passport_type_id) REFERENCES passport_type (id) DEFERRABLE;
ALTER TABLE passport_param ADD CONSTRAINT fk_passport_param FOREIGN KEY (passport_id) REFERENCES passport (id) DEFERRABLE;
ALTER TABLE ueberweisung ADD CONSTRAINT fk_konto FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE ueberweisung ADD CONSTRAINT fk_empfaenger FOREIGN KEY (empfaenger_id) REFERENCES empfaenger (id) DEFERRABLE;
ALTER TABLE umsatz ADD CONSTRAINT fk_konto2 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE umsatz ADD CONSTRAINT fk_empfaenger2 FOREIGN KEY (empfaenger_id) REFERENCES empfaenger (id) DEFERRABLE;
