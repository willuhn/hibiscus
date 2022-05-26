-- ------------------------------------------------------------------------
-- Vergroessert die Spalte kontonummer in der Tabelle konto auf 16 Zeichen
-- Update auf DB Version 70
-- ------------------------------------------------------------------------
ALTER TABLE `konto` CHANGE `kontonummer` `kontonummer` VARCHAR(16) NOT NULL; 
