/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/TurnusHelper.java,v $
 * $Revision: 1.15 $
 * $Date: 2009/08/26 21:23:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.kapott.hbci.GV_Result.GVRDauerList;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse fuer das Finden oder Erstellen von Zahlungs-Turnus(sen) ;).
 */
public class TurnusHelper
{

	private static String[] wochentage = null;

  // Hilfsmapping, um die Tages-Konstanten aus java.util.Calendar in
  // integer von 1 (montag) - 7 (sonntag) umrechnen zu koennen
  private final static int[] DAYMAP = new int[]
    {
      Calendar.MONDAY,
      Calendar.TUESDAY,
      Calendar.WEDNESDAY,
      Calendar.THURSDAY,
      Calendar.FRIDAY,
      Calendar.SATURDAY,
      Calendar.SUNDAY
    };
  
  
  /**
   * Liefert ein String-Array mit den Bezeichnungen der Wochentage.
   * Hinweis: Da es sich um ein Array handelt, zaehlt der Index
   * natuerlich nicht von 1-7 sondern von 0-6.
   * @return Bezeichnungen der Wochentage.
   */
  public static String[] getWochentage()
  {
    if (wochentage != null)
      return wochentage;

    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    wochentage = new String[]
    {
      i18n.tr("Montag"),
      i18n.tr("Dienstag"),
      i18n.tr("Mittwoch"),
      i18n.tr("Donnerstag"),
      i18n.tr("Freitag"),
      i18n.tr("Samstag"),
      i18n.tr("Sonntag")
    };
    
    return wochentage;
  }

  /**
   * Liefert die Bezeichnung des Werktages mit dem genannten Index.
   * Den zum Zahltag des Turnus gehoerenden kann man dann wie folgt ermitteln:<br>
   * <code>String tag = TurnusHelper.getWochentag(turnus.getTag())</code>.
   * @param index Index des Wochentages von 1 - 7.
   * @return Bezeichnung des Wochentages, oder <code>null</code> wenn der Index
   * ausserhalb des definierten Bereichs liegt.
   */
  public static String getWochentag(int index)
  {
    if (index < 1 || index > 7)
      return null;
    return getWochentage()[index - 1];
  }


  /**
   * Berechnet das naechste Ausfuehrungsdatum fuer einen Turnus.
   * @param ersteZahlung Datum der ersten Zahlung.
   * @param letzteZahlung Datum der letzten Zahlung.
   * @param turnus Turnus.
   * @param valuta Stichtag, zu dem die Berechnung erfolgen soll.
   * Ist kein Datum angegeben, wird das aktuelle verwendet.
   * @return das ermittelte Datum oder <code>null</code>, wenn keines mehr existiert.
   * @throws RemoteException
   */
  public static Date getNaechsteZahlung(Date ersteZahlung, Date letzteZahlung, Turnus turnus, Date valuta) throws RemoteException
  {
    // Keine erste Zahlung angegeben und kein Turnus. Nichts ermittelbar
    if (ersteZahlung == null || turnus == null)
      return null;
    
    if (valuta == null)
      valuta = new Date();
    
    // Das Datum der ersten Zahlung liegt in der Zukunft oder ist heute. Dann brauchen
    // wir gar nicht rechnen, sondern koennen gleich das nehmen.
    if (ersteZahlung.after(valuta) || ersteZahlung.equals(valuta))
      return ersteZahlung;

    // Auftrag bereits abgelaufen, da sich das Valuta-Datum hinter
    // der letzten Ausfuehrung befindet
    if (letzteZahlung != null && letzteZahlung.before(valuta))
      return null;

    // OK, wenn wir hier angekommen sind, muessen wir rechnen ;)
    Calendar cal = Calendar.getInstance();
    cal.setTime(ersteZahlung);
    cal.setFirstDayOfWeek(Calendar.MONDAY);
    
    int ze  = turnus.getZeiteinheit();
    int tag = turnus.getTag();
    int iv  = turnus.getIntervall();

    Date test = null;
    
    // eigentlich gehoert hier ein "while true" hin, ich will aber eine
    // Abbruchbedingung, damit das Teil keine 1000 Jahre in die Zukunft
    // rechnet ;)
    for (int i=0;i<1000;++i)
    {
      // Woechentlich
      if (ze == Turnus.ZEITEINHEIT_WOECHENTLICH)
      {
        // Wochentag festlegen
        int calTag = DAYMAP[tag-1]; // "-1" weil das Array bei 0 anfaengt
        cal.set(Calendar.DAY_OF_WEEK,calTag);

        test = cal.getTime();
        if (test != null && (test.after(valuta)) || test.equals(valuta))
          return test; // Datum gefunden

        // Ne, dann Anzahl der Wochen drauf rechnen
        cal.add(Calendar.WEEK_OF_YEAR,iv);
      }
      // Monatlich
      else
      {
        // Tag im Monat festlegen
        if (tag == HBCIProperties.HBCI_LAST_OF_MONTH)
          cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        else
          cal.set(Calendar.DAY_OF_MONTH,tag);

        test = cal.getTime();
        if (test != null && (test.after(valuta)) || test.equals(valuta))
          return test; // Datum gefunden

        // Ne, dann Anzahl der Monate drauf rechnen
        cal.add(Calendar.MONTH,iv);
      }
    }
    return null; // kein Datum ermittelbar
  }

	/**
	 * Prueft, ob es in der lokalen Datenbank einen Zahlungsturnus gibt,
	 * der den Eigenschaften des uebergebenen Dauerauftrags aus HBCI4Java
	 * entspricht.
   * @param d der zu pruefende Dauerauftrag.
   * @return das Turnus-Objekt, wenn eines gefunden wurde oder <code>null</code>.
   * @throws RemoteException
   */
  public static Turnus findByDauerAuftrag(GVRDauerList.Dauer d) throws RemoteException
	{
		int ze = Turnus.ZEITEINHEIT_MONATLICH;
		if ("W".equalsIgnoreCase(d.timeunit)) ze = Turnus.ZEITEINHEIT_WOECHENTLICH;

		DBIterator list = Settings.getDBService().createList(Turnus.class);
		list.addFilter("zeiteinheit = " + ze);
		list.addFilter("intervall = " + d.turnus);
		list.addFilter("tag = " + d.execday);
		if (list.hasNext())
			return (Turnus) list.next();

		return null;
	}

  /**
	 * Erstellt einen neuen Turnus mit den Eigenschaften des uebergebenen
	 * Dauerauftrags aus HBCI4Java und speichert ihn auch gleich in der Datenbank.
	 * <b>Wichtig:</b> Die Funktion checkt bereits intern mittels
	 * <code>findByDauerAuftrag</code> ob bereits einer existiert und
	 * liefert ggf diesen zurueck. Der Aufrufer muss also nicht selbst pruefen,
	 * ob einer existiert.
   * @param d der zu pruefende Dauerauftrag.
   * @return das Turnus-Objekt. Es wird in jedem Fall ein solches zurueckgegeben.
   * Das ist entweder ein neues oder ein existierendes.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static Turnus createByDauerAuftrag(GVRDauerList.Dauer d) throws RemoteException, ApplicationException
	{
		Turnus turnus = findByDauerAuftrag(d);
		if (turnus != null)
			return turnus; // wir haben schon einen, den nehmen wir.

		int ze = Turnus.ZEITEINHEIT_MONATLICH;
		if ("W".equalsIgnoreCase(d.timeunit)) ze = Turnus.ZEITEINHEIT_WOECHENTLICH;
		
		// Keiner da, dann erstellen wir ihn.
		turnus = (Turnus) Settings.getDBService().createObject(Turnus.class,null);
		turnus.setIntervall(d.turnus);
		turnus.setTag(d.execday);
		turnus.setZeiteinheit(ze);
		turnus.store();
		return turnus;
	}

	/**
	 * Kleine Hilfs-Funktion, die sich eine passende Bezeichnung fuer einen Turnus selbst ausdenkt ;).
   * @param turnus der Turnus, fuer den eine Bezeichnung erstellt werden soll.
   * @return die Bezeichnung.
   * @throws RemoteException
   */
  public static String createBezeichnung(Turnus turnus) throws RemoteException
	{
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		int iv = turnus.getIntervall();
		int ze = turnus.getZeiteinheit();
		int ta = turnus.getTag();

		String s = null;

		// Einfacher Zahlungsplan
		if (iv == 1 && ze == Turnus.ZEITEINHEIT_MONATLICH)
			s = i18n.tr("Monatlich");
		else if (iv == 1 && ze == Turnus.ZEITEINHEIT_WOECHENTLICH)
			s = i18n.tr("Wöchentlich");

		// komplexer Zahlungsplan
		if (iv > 1 && ze == Turnus.ZEITEINHEIT_MONATLICH)
			s = i18n.tr("Alle {0} Monate","" + iv);
		else if (iv > 1 && ze == Turnus.ZEITEINHEIT_WOECHENTLICH)
			s = i18n.tr("Alle {0} Wochen","" + iv);

		// Standardfaelle
		if (iv == 3 && ze == Turnus.ZEITEINHEIT_MONATLICH)
			s = i18n.tr("Vierteljährlich");
		if (iv == 6 && ze == Turnus.ZEITEINHEIT_MONATLICH)
			s = i18n.tr("Halbjährlich");
		if (iv == 12 && ze == Turnus.ZEITEINHEIT_MONATLICH)
			s = i18n.tr("Jährlich");


		// Zahltag anhaengen
		if (ze == Turnus.ZEITEINHEIT_WOECHENTLICH)
			s+= ", " + getWochentag(ta);
		else if (ze == Turnus.ZEITEINHEIT_MONATLICH)
    {
      // BUGZILLA #49 http://www.willuhn.de/bugzilla/show_bug.cgi?id=49
      if (ta == HBCIProperties.HBCI_LAST_OF_MONTH)
        s+= ", " + i18n.tr("zum Monatsletzten");
      else
        s+= ", " + i18n.tr("am {0}. des Monats","" + ta);
    }

		return s;
	}
}


/**********************************************************************
 * $Log: TurnusHelper.java,v $
 * Revision 1.15  2009/08/26 21:23:46  willuhn
 * @C "aller x Wochen/Monate" sagt man wohl nur im Saechsischen ;) Habs geaendert auf "alle x Wochen/Monate". Google liefert mit dieser Schreibweise auch erheblich mehr Treffer
 *
 * Revision 1.14  2008/09/04 23:42:33  willuhn
 * @N Searchprovider fuer Sammel- und Dauerauftraege
 * @N Sortierung von Ueberweisungen und Lastschriften in Suchergebnissen
 * @C "getNaechsteZahlung" von DauerauftragUtil nach TurnusHelper verschoben
 *
 * Revision 1.13  2006/08/25 10:13:43  willuhn
 * @B Fremdschluessel NICHT mittels PreparedStatement, da die sonst gequotet und von McKoi nicht gefunden werden. BUGZILLA 278
 *
 * Revision 1.12  2006/08/23 09:45:13  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.11  2005/06/07 22:19:57  web0
 * @B bug 49
 *
 * Revision 1.10  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.9  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.8  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.6  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.5  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.4  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.3  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/15 23:39:22  willuhn
 * @N TurnusImpl
 *
 * Revision 1.1  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 **********************************************************************/