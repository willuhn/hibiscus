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
  UNIQUE (id),
  PRIMARY KEY (id)
);

DROP TABLE passport_param;
DROP TABLE passport;
DROP TABLE passport_type;
ALTER TABLE passport_param DROP CONSTRAINT fk_passport_param;
ALTER TABLE passport DROP CONSTRAINT fk_passport_type;
