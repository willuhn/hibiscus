/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/TurnusHelper.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/14 23:48:31 $
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

import org.kapott.hbci.GV_Result.GVRDauerList;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse fuer das Finden oder Erstellen von Zahlungs-Turnus(sen) ;).
 */
public class TurnusHelper
{

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

		DBIterator list = Settings.getDatabase().createList(Turnus.class);
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
		turnus = (Turnus) Settings.getDatabase().createObject(Turnus.class,null);
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
		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		String[] wochentage = new String[]
		{
			i18n.tr("Montags"),
			i18n.tr("Dienstags"),
			i18n.tr("Mittwochs"),
			i18n.tr("Donnerstags"),
			i18n.tr("Freitags"),
			i18n.tr("Samstags"),
			i18n.tr("Sonntags")
		};

		String s = "";
		if (turnus.getIntervall() == 1 && turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH)
			s += i18n.tr("Monatlich");
		else if (turnus.getIntervall() == 1 && turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_WOECHENTLICH)
			s += i18n.tr("Wöchentlich");

		if (turnus.getIntervall() > 1 && turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH)
			s += i18n.tr("Aller {0} Monate","" + turnus.getIntervall());
		else if (turnus.getIntervall() > 1 && turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_WOECHENTLICH)
			s += i18n.tr("Aller {0} Wochen","" + turnus.getIntervall());

		if (turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_WOECHENTLICH)
			s+= ", " + wochentage[turnus.getTag()];
		else if (turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH)
			s+= ", " + i18n.tr("am {0}. des Monats","" + turnus.getTag());

		return s;
	}
}


/**********************************************************************
 * $Log: TurnusHelper.java,v $
 * Revision 1.1  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 **********************************************************************/