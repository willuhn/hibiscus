-- ----------------------------------------------------------------------
-- Fuehrt die neue Spalte "pmtinfid" ein.
-- ----------------------------------------------------------------------

alter table aueberweisung add pmtinfid VARCHAR(35);
alter table sepalastschrift add pmtinfid VARCHAR(35);
alter table sepaslastbuchung add pmtinfid VARCHAR(35);
alter table sepasuebbuchung add pmtinfid VARCHAR(35);
alter table sepadauerauftrag add pmtinfid VARCHAR(35);
