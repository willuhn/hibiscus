-- ----------------------------------------------------------------------
-- Erweitert die Tabelle "umsatztyp" um die Spalten "color" und "customcolor"
-- ----------------------------------------------------------------------

alter table umsatztyp add color varchar(11) NULL;
alter table umsatztyp add customcolor int(1) NULL;

-- ----------------------------------------------------------------------
-- $Log: update0021.sql,v $
-- Revision 1.1  2009/05/08 13:58:30  willuhn
-- @N Icons in allen Menus und auf allen Buttons
-- @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
--
-- ----------------------------------------------------------------------

