-- ----------------------------------------------------------------------
-- Fuehrt die neue Spalte "purposecode" ein.
-- ----------------------------------------------------------------------

alter table aueberweisung add purposecode VARCHAR(10);
alter table sepalastschrift add purposecode VARCHAR(10);
alter table sepaslastbuchung add purposecode VARCHAR(10);
alter table sepasuebbuchung add purposecode VARCHAR(10);
alter table sepadauerauftrag add purposecode VARCHAR(10);
