CREATE TABLE dauerauftrag (
  id NUMERIC default UNIQUEKEY('dauerauftrag'),
  konto_id int(4) NOT NULL,
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


ALTER TABLE dauerauftrag ADD CONSTRAINT fk_konto FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
