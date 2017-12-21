/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/UmsatzTyp.java,v $
 * $Revision: 1.19 $
 * $Date: 2012/01/02 22:32:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
}


/**********************************************************************
 * $Log: UmsatzTyp.java,v $
 * Revision 1.19  2012/01/02 22:32:20  willuhn
 * @N BUGZILLA 1170
 *
 * Revision 1.18  2011-07-20 15:41:36  willuhn
 * @N Neue Funktion UmsatzTyp#matches(Umsatz,boolean allowReassign) - normalerweise liefert die Funktion ohne das Boolean false, wenn der Umsatz bereits manuell einer anderen Kategorie zugeordnet ist. Andernfalls kaeme es hier ja - zumindest virtuell - zu einer Doppel-Zuordnung. Da "UmsatzList" jedoch fuer den Suchbegriff (den man oben eingeben kann) intern on-the-fly einen UmsatzTyp erstellt, mit dem die Suche erfolgt, wuerden hier bereits fest zugeordnete Umsaetze nicht mehr gefunden werden. Daher die neue Funktion.
 *
 * Revision 1.17  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.16  2009/02/23 23:44:50  willuhn
 * @N Etwas Code fuer Support fuer Unter-/Ober-Kategorien
 *
 * Revision 1.15  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.14  2008/02/26 01:01:16  willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp. die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet werden muss
 *
 * Revision 1.13  2007/03/10 07:18:36  jost
 * Neu: Nummer für die Sortierung der Umsatz-Kategorien
 *
 * Revision 1.12  2007/03/06 20:06:40  jost
 * Neu: Umsatz-Kategorien-Übersicht
 *
 * Revision 1.11  2006/11/29 00:40:37  willuhn
 * @N Keylistener in Umsatzlist nur dann ausfuehren, wenn sich wirklich etwas geaendert hat
 * @C UmsatzTyp.matches matcht jetzt bei leeren Pattern nicht mehr
 *
 * Revision 1.10  2006/11/23 23:24:17  willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 *
 * Revision 1.9  2006/04/03 21:39:07  willuhn
 * @N UmsatzChart
 *
 * Revision 1.8  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.7  2005/12/29 01:22:12  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
 * Revision 1.6  2005/12/20 00:03:27  willuhn
 * @N Test-Code fuer Tortendiagramm-Auswertungen
 *
 * Revision 1.5  2005/12/13 00:06:38  willuhn
 * @N UmsatzTyp erweitert
 *
 * Revision 1.4  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.3  2005/12/05 17:20:40  willuhn
 * @N Umsatz-Filter Refactoring
 *
 * Revision 1.2  2005/11/14 23:47:21  willuhn
 * @N added first code for umsatz categories
 *
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/