-- Filter-Framework vereinfacht
ALTER CREATE TABLE umsatztyp (
  id NUMERIC default UNIQUEKEY('umsatztyp'),
  name varchar(255) NOT NULL,
  pattern varchar(255) NOT NULL,
  isregex int(1) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

-- Wird nicht mehr persistiert sondern on demand ermittelt
DROP TABLE if exists umsatzzuordnung;
