/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Prüft eingehende Umsätze, ob diese als Gegenkonto ein eigenes Offlinekonto haben.
 * Wenn ja, wird der entsprechende Umsatz für das Offlinekonto angelegt.
 */
public class CheckOfflineUmsatzMessageConsumer implements MessageConsumer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public boolean autoRegister()
  {
    // Wird manuell per Manifest registriert, um die Reihenfolge festzulegen. Muss NACH OfflineSaldoMessageConsumer passieren,
    // damit sichergestellt ist, dass der Saldo des Kontos aktualisiert wurde, wenn wir dran sind.
    return false;
  }

  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{ImportMessage.class};
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    // Wenn es keine Import-Message ist ignorieren wir die folgenden
    if (message == null || !(message instanceof ImportMessage))
      return;
    
    GenericObject o = ((ImportMessage)message).getObject();
    
    if (!(o instanceof Umsatz))
      return; // interessiert uns nicht
    
    // wir haben einen Umsatz, den es zu bearbeiten gilt...
    Umsatz u = (Umsatz) o;

    Logger.debug("imported umsatz, checking if counter entry can be created [id: " + u.getID() + "]");
    // Vormerkbuchungen ignorieren wir. Zum einen, weil wir sie in dem
    // Offline-Konto nicht wieder automatisch loeschen, zum anderen, weil
    // der User sie auch nicht loeschen kann
    if (u.hasFlag(Umsatz.FLAG_NOTBOOKED))
    {
      Logger.debug("skip, is not-booked entry");
      return;
    }
    
    // Wenn der Umsatz schon von einem Offline-Konto kommt, legen
    // wir keine Gegenbuchung mehr an. Das fuehrt sonst zu einem Ping-Pong-Spiel ;)
    Konto k = u.getKonto();
    if (k.hasFlag(Konto.FLAG_OFFLINE))
    {
      Logger.debug("skip, source account is an offline account");
      return;
    }

    // Checken, ob wir ein lokal passendes Offline-Konto haben
    Konto gegenkonto = null;
    String s = StringUtils.trimToNull(u.getGegenkontoNummer());
    if (s == null)
    {
      Logger.debug("skip, have no account number for counter entry");
      return;
    }
    
    if (s.length() > 10)
    {
      // Das ist eine IBAN
      Logger.debug("searching for offline account with iban: " + s);
      gegenkonto = KontoUtil.findByIBAN(s,Konto.FLAG_OFFLINE);
    }
    else
    {
      // Konto mit Kto und BLZ
      String blz = u.getGegenkontoBLZ();
      Logger.debug("searching for offline account with kto: " + s + ", blz: " + blz);
      gegenkonto = KontoUtil.find(s, blz,Konto.FLAG_OFFLINE);
    }
    
    if (gegenkonto == null)
    {
      Logger.debug("skip, no matching account found");
      return; // Das Konto haben wir nicht
    }
    
    Logger.debug("found account [id: " + gegenkonto.getID() + "]");
    
    // Checken, ob fuer das Konto automatisch Umsaetze angelegt werden sollen
    SynchronizeOptions options = new SynchronizeOptions(gegenkonto);
    if (!options.getSyncOffline())
    {
      Logger.debug("skip, sync option disabled");
      return;
    }

    Logger.info("creating counter entry");

    // Kopie der Buchung erzeugen
    Umsatz gegenbuchung = u.duplicate();

    // Betrag negieren
    gegenbuchung.setBetrag(-gegenbuchung.getBetrag());

    // Konten tauschen
    gegenbuchung.setKonto(gegenkonto);
    gegenbuchung.setGegenkontoNummer(k.getKontonummer());
    gegenbuchung.setGegenkontoBLZ(k.getBLZ());
    gegenbuchung.setGegenkontoName(k.getName());
    
    // Art des Umsatzes setzen, Laenge ggf. auf DB-Feldlaenge küuerzen
    String art = i18n.tr("Auto-Buchung Offline-Konto");
    if (art.length()>100) art = art.substring(0, 100);
    gegenbuchung.setArt(art);
    
    // Saldo berechnen
    gegenbuchung.setSaldo(gegenkonto.getSaldo() + gegenbuchung.getBetrag());
    
    // Umsatztyp loeschen
    gegenbuchung.setUmsatzTyp(null);

    gegenbuchung.store(); // Umsatz speichern
    
    // neuen Umsatz bekannt geben
    Application.getMessagingFactory().sendMessage(new ImportMessage(gegenbuchung));
  }

}

/*******************************************************************************
 * $Log: CheckOfflineUmsatzMessageConsumer.java,v $
 * Revision 1.6  2011/04/13 08:46:15  willuhn
 * @B Vormerkbuchungen nicht mehr automatisch in Offline-Konten uebernehmen. Die werden dort naemlich nicht mehr geloescht und der User kann sie auch nicht loeschen
 *
 * Revision 1.5  2010-11-08 10:37:00  willuhn
 * @N BUGZILLA 945
 *
 ******************************************************************************/