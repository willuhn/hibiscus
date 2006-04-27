------------------------------------------------------------------------
-- $Source: /cvsroot/hibiscus/hibiscus/sql/Attic/update_1.5-1.6.sql,v $
-- $Revision: 1.1 $
-- $Date: 2006/04/27 22:26:16 $
-- $Author: willuhn $
-- $Locker:  $
-- $State: Exp $
--
-- Copyright (c) by willuhn.webdesign
-- All rights reserved
--
------------------------------------------------------------------------

-- Vergessene Constraints
ALTER TABLE ueberweisung ADD CONSTRAINT fk_konto_usb FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE slastschrift ADD CONSTRAINT fk_konto_slast FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE umsatz ADD CONSTRAINT fk_konto_ums FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
ALTER TABLE lastschrift ADD CONSTRAINT fk_konto_last FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;

------------------------------------------------------------------------
-- $Log: update_1.5-1.6.sql,v $
-- Revision 1.1  2006/04/27 22:26:16  willuhn
-- *** empty log message ***
--
------------------------------------------------------------------------
