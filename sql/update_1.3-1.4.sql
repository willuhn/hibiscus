ALTER CREATE TABLE systemnachricht (
  id NUMERIC default UNIQUEKEY('systemnachricht'),
  blz varchar(15) NOT NULL,
  nachricht text NOT NULL,
  datum date NOT NULL,
  gelesen int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE offeneposten (
  id NUMERIC default UNIQUEKEY('offeneposten'),
  umsatz_id int(4) NULL,
  bezeichnung varchar(255) NOT NULL,
  datum date NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE op_pattern (
  id NUMERIC default UNIQUEKEY('op_pattern'),
  offeneposten_id int(4) NOT NULL,
  field varchar(255) NOT NULL,
  patterntype int(1) NOT NULL,
  ignorecase int(1) NOT NULL,
  pattern varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER TABLE op_pattern ADD CONSTRAINT fk_offeneposten_1 FOREIGN KEY (offeneposten_id) REFERENCES offeneposten (id) DEFERRABLE;
ALTER TABLE offeneposten ADD CONSTRAINT fk_umsatz_1 FOREIGN KEY (umsatz_id) REFERENCES umsatz (id) DEFERRABLE;
