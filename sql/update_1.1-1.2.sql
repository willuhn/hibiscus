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
  UNIQUE (id),
  PRIMARY KEY (id)
);

ALTER CREATE TABLE passport_type (
  id NUMERIC default UNIQUEKEY('passport_type'),
  name varchar(255) NOT NULL,
  implementor varchar(1000) NOT NULL,
  abstractview varchar(1000) NOT NULL,
  controller varchar(1000) NOT NULL,
  UNIQUE (name),
  UNIQUE (id),
  PRIMARY KEY (id)
);

UPDATE passport_type set implementor='de.willuhn.jameica.hbci.passports.ddv.rmi.Passport',abstractview='de.willuhn.jameica.hbci.passports.ddv.View',controller='de.willuhn.jameica.hbci.passports.ddv.Controller' WHERE id = 1;
