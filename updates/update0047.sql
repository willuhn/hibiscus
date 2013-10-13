-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "sepalastschrift" um die Spalten "sepatype" und "targetdate"
-- ----------------------------------------------------------------------

alter table sepalastschrift add sepatype varchar(8);
alter table sepalastschrift add targetdate date;
