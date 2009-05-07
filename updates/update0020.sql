-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "aueberweisung" um eine Spalte "empfaenger_bic"
-- ----------------------------------------------------------------------

alter table aueberweisung add empfaenger_bic varchar(15) NULL;

-- ----------------------------------------------------------------------
-- $Log: update0020.sql,v $
-- Revision 1.1  2009/05/07 15:13:37  willuhn
-- @N BIC in Auslandsueberweisung
--
-- ----------------------------------------------------------------------

