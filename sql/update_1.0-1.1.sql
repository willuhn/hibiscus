ALTER CREATE TABLE dauerauftrag (
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

ALTER CREATE TABLE turnus (
  id NUMERIC default UNIQUEKEY('turnus'),
  zeiteinheit int(1) NOT NULL,
  intervall int(2) NOT NULL,
  tag int(2) NOT NULL,
  bezeichnung varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);


ALTER TABLE dauerauftrag ADD CONSTRAINT fk_konto4 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE dauerauftrag ADD CONSTRAINT fk_turnus FOREIGN KEY (turnus_id) REFERENCES turnus (id) DEFERRABLE;
