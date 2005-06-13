ALTER CREATE TABLE systemnachricht (
  id NUMERIC default UNIQUEKEY('systemnachricht'),
  blz varchar(15) NOT NULL,
  nachricht text NOT NULL,
  datum date NOT NULL,
  gelesen int(1) NOT NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

-- Kommentar-Feld hinzugefuegt
ALTER CREATE TABLE umsatz (
  id NUMERIC default UNIQUEKEY('umsatz'),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15),
  empfaenger_blz varchar(15),
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(35) NOT NULL,
  zweck2 varchar(35),
  datum date NOT NULL,
  valuta date NOT NULL,
  saldo double,
  primanota varchar(100),
  art varchar(100),
  customerref varchar(100),
  umsatztyp_id int(4),
  kommentar text NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);
