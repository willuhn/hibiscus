/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.regex.PatternSyntaxException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBObjectNode;

/**
 * Interface zur Einstufung von Umsaetzen in verschiedene Kategorien.
 */
public interface UmsatzTyp extends DBObjectNode
{
  /**
   * Umsatzkategorie vom Typ "Ausgabe".
   */
  public final static int TYP_AUSGABE  = 0;
  
  /**
   * Umsatzkategorie vom Typ "Einnahme".
   */
  public final static int TYP_EINNAHME = 1;

  /**
   * Umsatzkategorie vom Typ "Egal".
   */
  public final static int TYP_EGAL     = 2;
  
  /**
   * Maximale Laenge des Pattern.
   */
  public final static int MAXLENGTH_PATTERN = 1000;
  
  /**
   * Flag "kein Flag".
   */
  public final static int FLAG_NONE    = 0;

  /**
   * Flag "In Auswertungen ignorieren".
   */
  public final static int FLAG_SKIP_REPORTS = 1 << 0;
  
	/**
	 * Liefert den Namen des Umsatz-Typs.
   * @return Name des Umsatz-Typs.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;

	/**
	 * Speichert den Namen des Umsatz-Typs.
   * @param name Name des Umsatz-Typs.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
	
  /**
   * Liefert die Nummer des Umsatz-Typs. Die Nummer wird für die Sortierung bei der Auswertung
   * eingesetzt.
   * @return Nummer des Umsatz-Typs.
   * @throws RemoteException
   */
  public String getNummer() throws RemoteException;
  
  /**
   * Speichert die Nummer des Umsatz-Typs. 
   * @param nummer Nummer des Umsatz-Typs
   * @throws RemoteException
   */
  public void setNummer(String nummer) throws RemoteException;
  
  /**
   * Liefert das Suchmuster fuer den Umsatztyp.
   * @return Suchmuster.
   * @throws RemoteException
   */
  public String getPattern() throws RemoteException;

  /**
   * Speichert den Kommentar fuer den Umsatztyp.
   * @param kommentar der Kommentar.
   * @throws RemoteException
   */
  public void setKommentar(String kommentar) throws RemoteException;

  /**
   * Liefert den Kommentar fuer den Umsatztyp.
   * @return Kommentar.
   * @throws RemoteException
   */
  public String getKommentar() throws RemoteException;

  /**
   * Speichert das Suchmuster fuer den Umsatztyp.
   * @param pattern das Suchmuster.
   * @throws RemoteException
   */
  public void setPattern(String pattern) throws RemoteException;

	/**
	 * Liefert eine Liste von Umsaetzen, die diesem Umsatz-Typ entsprechen.
   * @return Umsatz-Liste.
   * @throws RemoteException
   */
  public GenericIterator getUmsaetze() throws RemoteException;
  
  /**
   * Liefert eine Liste von Umsaetzen aus dem angegebenen Zeitraum.
   * @param von Start-Datum. Wenn == null, dann bleibt es unberücksichtigt.
   * @param bis Ende-Datum. Wenn == null, dann bleibt es unberücksichtigt.
   * @return Umsatz-Liste.
   * @throws RemoteException
   */
  public GenericIterator getUmsaetze(Date von, Date bis) throws RemoteException;
  
  /**
   * Liefert eine Liste von Umsaetzen der letzten Tage, die diesem Umsatz-Typ entsprechen.
   * @param days Anzahl der Tage.
   * @return Umsatz-Liste.
   * @throws RemoteException
   */
  public GenericIterator getUmsaetze(int days) throws RemoteException;

  /**
   * Liefert die Hoehe des Umsatzes, der fuer diesen Umsatztyp auf allen Konten vorliegt.
   * @return Hoehe des Umsatzes.
   * @throws RemoteException
   */
  public double getUmsatz() throws RemoteException;
  
  /**
   * Liefert die Hoehe des Umsatzes aus dem angegebenen Zeitraum.
   * @param von Start-Datum. Wenn == null, dann bleibt es unberücksichtigt.
   * @param bis Ende-Datum. Wenn == null, dann bleibt es unberücksichtigt.
   * @return Hoehe des Umsatzes.
   * @throws RemoteException
   */
  public double getUmsatz(Date von, Date bis) throws RemoteException;
  
  /**
   * Liefert die Hoehe des Umsatzes der letzten Tage, der fuer diesen Umsatztyp auf allen Konten vorliegt.
   * @param days Anzahl der Tage.
   * @return Hoehe des Umsatzes.
   * @throws RemoteException
   */
  public double getUmsatz(int days) throws RemoteException;

  /**
   * Prueft, ob es sich bei dem Pattern um einen regulaeren Ausdruck handelt.
   * @return true, wenn es sich um einen regulaeren Ausdruck handelt.
   * @throws RemoteException
   */
  public boolean isRegex() throws RemoteException;
  
  /**
   * Liefert den Typ der Kategorie.
   * @return Typ der Kategorie.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @see UmsatzTyp#TYP_EGAL
   * @throws RemoteException
   */
  public int getTyp() throws RemoteException;
  
  /**
   * Speichert den Typ der Kategorie.
   * @param typ Typ der Kategorie.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @see UmsatzTyp#TYP_EGAL
   * @throws RemoteException
   */
  public void setTyp(int typ) throws RemoteException;
  
  /**
   * Speichert, ob es sich bei dem Pattern um einen regulaeren Ausdruck handelt.
   * @param regex true, wenn es sich um einen regulaeren Ausdruck handelt.
   * @throws RemoteException
   */
  public void setRegex(boolean regex) throws RemoteException;

  /**
   * Prueft, ob der Umsatz diesem Pattern entspricht.
   * Ist fuer den Umsatz-Typ kein Pattern definiert, liefert die Funktion
   * immer false.
   * @param umsatz zu pruefender Umsatz.
   * @return true, wenn er dem Pattern entspricht.
   * @throws RemoteException
   * @throws PatternSyntaxException wird geworden, wenn es ein regulaerer Ausdruck mit Fehlern ist.
   */
  public boolean matches(Umsatz umsatz) throws RemoteException, PatternSyntaxException;

  /**
   * Prueft, ob der Umsatz diesem Pattern entspricht.
   * Ist fuer den Umsatz-Typ kein Pattern definiert, liefert die Funktion
   * immer false.
   * @param umsatz zu pruefender Umsatz.
   * @param allowReassign true, wenn der Umsatz auch dann als passend gewertet werden
   * soll, wenn er bereits fest einer anderen Kategorie zugeordnet ist. Per Default (also
   * wenn die "matches(Umsatz)"-Funktion ohne diesen Boolean-Parameter aufgerufen wird)
   * ist dieser Parameter "false". Das heisst, ein Umsatz, der bereits manuell (nicht per Suchbegriff)
   * einer anderen Kategorie zugeordnet ist, liefert hier false, wenn "this" nicht
   * die zugeordnete Kategorie ist.
   * @return true, wenn er dem Pattern entspricht.
   * @throws RemoteException
   * @throws PatternSyntaxException wird geworden, wenn es ein regulaerer Ausdruck mit Fehlern ist.
   */
  public boolean matches(Umsatz umsatz, boolean allowReassign) throws RemoteException, PatternSyntaxException;
  
  /**
   * Liefert die fuer diese Kategorie zu verwendende Farbe.
   * @return Farbe oder null, wenn noch keine definiert wurde.
   * @throws RemoteException
   */
  public int[] getColor() throws RemoteException;
  
  /**
   * Speichert die Farbe fuer die Umsatz-Kategorie.
   * @param rgb Farbe.
   * @throws RemoteException
   */
  public void setColor(int[] rgb) throws RemoteException;
  
  /**
   * Prueft, ob eine benutzerdefinierte Farbe verwendet werden soll.
   * @return true, wenn eine benutzerdefinierte Farbe verwendet werden soll.
   * @throws RemoteException
   */
  public boolean isCustomColor() throws RemoteException;
  
  /**
   * Legt fest, ob eine benutzerdefinierte Farbe verwendet werden soll.
   * @param b true, wenn eine benutzerdefinierte Farbe verwendet werden soll.
   * @throws RemoteException
   */
  public void setCustomColor(boolean b) throws RemoteException;
  
  /**
   * Liefert das optional zugeordnete Konto.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;
  
  /**
   * Speichert das optional zugeordnete Konto.
   * @param konto Konto.
   * @throws RemoteException
   */
  public void setKonto(Konto konto) throws RemoteException;
  
  /**
   * Liefert eine optionale Konto-Kategorie.
   * @return eine optionale Konto-Kategorie.
   * @throws RemoteException
   */
  public String getKontoKategorie() throws RemoteException;
  
  /**
   * Speichert eine optionale Konto-Kategorie.
   * @param kategorie die optionale Konto-Kategorie.
   * @throws RemoteException
   */
  public void setKontoKategorie(String kategorie) throws RemoteException;
  
  /**
   * Liefert ein Bit-Feld mit Flags.
   * Ein Objekt kann mit verschiedenen Flags markiert
   * werden. Das kann zum Beispiel "deaktiviert" sein.
   * Damit fuer kuenftige weitere Flags nicht immer
   * ein neues Feld zur Datenbank hinzugefuegt werden
   * muss, verwenden wir hier ein Bitfeld. Damit koennen
   * mehrere Flags in einem Wert codiert werden.
   * @return Bit-Feld mit den Flags des Objektes.
   * @throws RemoteException
   */
  public int getFlags() throws RemoteException;
  
  /**
   * Speichert die Flags einen Objektes.
   * @param flags die Flags in Form eines Bit-Feldes.
   * @throws RemoteException
   */
  public void setFlags(int flags) throws RemoteException;
  
  /**
   * Prueft, ob das angegebene Flag vorhanden ist.
   * @param flag das zu pruefende Flag.
   * @return true, wenn es gesetzt ist.
   * @throws RemoteException
   */
  public boolean hasFlag(int flag) throws RemoteException;
  
  /**
   * Liefert einen Pfad mit den Namen der Kategorien bis zur obersten Ebene.
   * @param sep das Trennzeichen.
   * @return der Pfad.
   * @throws RemoteException
   */
  public String getPath(String sep) throws RemoteException;
}
