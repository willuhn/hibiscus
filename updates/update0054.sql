-- ----------------------------------------------------------------------
-- Aktualisiert die Spalte "pmtinfid" - ist bei Sammelauftraegen im Kopf, nicht in den Buchungen
-- ----------------------------------------------------------------------

alter table sepaslastbuchung drop pmtinfid;
alter table sepasuebbuchung drop pmtinfid;
alter table sepaslast add pmtinfid VARCHAR(35);
alter table sepasueb add pmtinfid VARCHAR(35);
