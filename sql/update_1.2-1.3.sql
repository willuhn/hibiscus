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

ALTER TABLE slastschrift ADD CONSTRAINT fk_konto6 FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE slastbuchung ADD CONSTRAINT fk_slastschrift1 FOREIGN KEY (slastschrift_id) REFERENCES slastschrift (id) DEFERRABLE;
