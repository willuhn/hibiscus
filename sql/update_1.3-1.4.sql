ALTER CREATE TABLE systemnachricht (
  id NUMERIC default UNIQUEKEY('systemnachricht'),
  blz varchar(15) NOT NULL,
  nachricht text NOT NULL,
  datum date NOT NULL,
  gelesen int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE filterpattern (
  id NUMERIC default UNIQUEKEY('filterpattern'),
  field varchar(255) NOT NULL,
  typ int(1) NOT NULL,
  ignorecase int(1) NOT NULL,
  pattern varchar(255) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE offeneposten (
  id NUMERIC default UNIQUEKEY('offeneposten'),
  bezeichnung varchar(255) NOT NULL,
  offen int(1) NOT NULL,
  datum date NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE op_pattern (
  id NUMERIC default UNIQUEKEY('op_pattern'),
  offeneposten_id int(4) NOT NULL,
  filterpattern_id int(4) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER TABLE op_pattern ADD CONSTRAINT fk_offeneposten_1 FOREIGN KEY (offeneposten_id) REFERENCES offeneposten (id) DEFERRABLE;
ALTER TABLE op_pattern ADD CONSTRAINT fk_filterpattern_1 FOREIGN KEY (filterpattern_id) REFERENCES filterpattern (id) DEFERRABLE;
