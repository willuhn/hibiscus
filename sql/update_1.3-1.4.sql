ALTER CREATE TABLE systemnachricht (
  id NUMERIC default UNIQUEKEY('systemnachricht'),
  blz varchar(15) NOT NULL,
  nachricht text NOT NULL,
  datum date NOT NULL,
  gelesen int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);
