/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/util/Attic/TurnusHelper.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/18 23:38:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.util;

import java.rmi.RemoteException;

import org.kapott.hbci.GV_Result.GVRDauerList;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
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
		turnus.setBezeichnung(createBezeichnung(turnus));
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
			s = i18n.tr("Aller {0} Monate","" + iv);
		else if (iv > 1 && ze == Turnus.ZEITEINHEIT_WOECHENTLICH)
			s = i18n.tr("Aller {0} Wochen","" + iv);

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
			s+= ", " + i18n.tr("am {0}. des Monats","" + ta);

		return s;
	}
	
  /**
	 * Liefert die Bezeichnung des Werktages mit dem genannten Index.
	 * Den zum Zahltag des Turnus gehoerenden kann man dann wie folgt ermitteln:<br>
	 * <code>String tag = TurnusHelper.getWochentag(turnus.getTag())</code>.
   * @param index Index des Wochentages von 1 - 7.
   * @return Bezeichnung des Wochentages, oder <code>null</code> wenn der Index
   * ausserhalb des definierten Bereichs liegt.
   * @throws RemoteException
   */
  public static String getWochentag(int index) throws RemoteException
	{
		if (index < 1 || index > 7)
			return null;
		return getWochentage()[index - 1];
	}

	/**
	 * Liefert ein String-Array mit den Bezeichnungen der Wochentage.
	 * Hinweis: Da es sich um ein Array handelt, zaehlt der Index
	 * natuerlich nicht von 1-7 sondern von 0-6.
	 * @return Bezeichnungen der Wochentage.
	 * @throws RemoteException
	 */
  public static String[] getWochentage() throws RemoteException
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
}


/**********************************************************************
 * $Log: TurnusHelper.java,v $
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