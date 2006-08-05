------------------------------------------------------------------------
-- $Source: /cvsroot/hibiscus/hibiscus/sql/Attic/update_1.5-1.6.sql,v $
-- $Revision: 1.4 $
-- $Date: 2006/08/05 21:10:45 $
-- $Author: willuhn $
-- $Locker:  $
-- $State: Exp $
--
-- Copyright (c) by willuhn.webdesign
-- All rights reserved
--
------------------------------------------------------------------------
--
--
-- Verlorengegangene Constraints
-- Vorher sicherheitshalber loeschen (falls einige schon existieren)
--ALTER TABLE ueberweisung DROP CONSTRAINT fk_konto_usb;
--ALTER TABLE slastschrift DROP CONSTRAINT fk_konto_slast;
--ALTER TABLE umsatz DROP CONSTRAINT fk_konto_ums;
--ALTER TABLE lastschrift DROP CONSTRAINT fk_konto_last;

--ALTER TABLE ueberweisung ADD CONSTRAINT fk_konto_usb FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
--ALTER TABLE slastschrift ADD CONSTRAINT fk_konto_slast FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
--ALTER TABLE umsatz ADD CONSTRAINT fk_konto_ums FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
--ALTER TABLE lastschrift ADD CONSTRAINT fk_konto_last FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;


insert into umsatztyp (id,name,pattern,isregex,iseinnahme) values (100,'Gehalt','(Lohn.*?)|(Gehalt.*?)',1,1);
insert into umsatztyp (id,name,pattern,isregex,iseinnahme) values (101,'Miete','Miete',0,0);
insert into umsatztyp (id,name,pattern,isregex,iseinnahme) values (102,'Kreditkarte','(Visa.*?)|(Mastercard.*?)|(American Express.*?)',1,0);
insert into umsatztyp (id,name,pattern,isregex,iseinnahme) values (103,'GEZ','RUNDFUNKANST.',0,0);
insert into umsatztyp (id,name,pattern,isregex,iseinnahme) values (104,'Telefon','(O2.*?)|(Telekom.*?)|(telecom.*?)|(Vodafone.*?)|(eplus.*?)|(t-mobile.*?)|(Arcor.*?)',1,0);
insert into umsatztyp (id,name,pattern,isregex,iseinnahme) values (105,'EC','EC.*?',1,0);

------------------------------------------------------------------------
-- $Log: update_1.5-1.6.sql,v $
-- Revision 1.4  2006/08/05 21:10:45  willuhn
-- @N Vordefinierte Filter
--
-- Revision 1.3  2006/06/08 22:42:02  willuhn
-- *** empty log message ***
--
-- Revision 1.2  2006/05/11 20:34:16  willuhn
-- @B fehleranfaellige SQL-Updates entfernt
--
-- Revision 1.1  2006/04/27 22:26:16  willuhn
-- *** empty log message ***
--
------------------------------------------------------------------------