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
  typ varchar(2) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);
