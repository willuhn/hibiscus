/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/CheckOfflineUmsatzMessageConsumer.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/04/13 08:46:15 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
import de.willuhn.util.I18N;

/**
 * Prüft eingehende Umsätze, ob diese als Gegenkonto ein eigenes Offlinekonto haben.
 * Wenn ja, wird der entsprechende Umsatz für das Offlinekonto angelegt.
 */
public class CheckOfflineUmsatzMessageConsumer implements MessageConsumer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // Wird manuell per Manifest registriert, um die Reihenfolge festzulegen. Muss NACH OfflineSaldoMessageConsumer passieren,
    // damit sichergestellt ist, dass der Saldo des Kontos aktualisiert wurde, wenn wir dran sind.
    return false;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{ImportMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
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
    
    // Vormerkbuchungen ignorieren wir. Zum einen, weil wir sie in dem
    // Offline-Konto nicht wieder automatisch loeschen, zum anderen, weil
    // der User sie auch nicht loeschen kann
    if (u.hasFlag(Umsatz.FLAG_NOTBOOKED))
      return;
    
    // Wenn der Umsatz schon von einem Offline-Konto kommt, legen
    // wir keine Gegenbuchung mehr an. Das fuehrt sonst zu einem Ping-Pong-Spiel ;)
    Konto k = u.getKonto();
    if (k.hasFlag(Konto.FLAG_OFFLINE))
      return;

    // Checken, ob wir ein lokal passendes Offline-Konto haben
    Konto gegenkonto = null;
    if (StringUtils.trimToEmpty(u.getGegenkontoNummer()).length() > 10)
    {
      // Das ist eine IBAN
      gegenkonto = KontoUtil.findByIBAN(u.getGegenkontoNummer(),Konto.FLAG_OFFLINE);
    }
    else
    {
      // Konto mit Kto und BLZ
      gegenkonto = KontoUtil.find(u.getGegenkontoNummer(), u.getGegenkontoBLZ(),Konto.FLAG_OFFLINE);
    }
    
    if (gegenkonto == null)
      return; // Das Konto haben wir nicht
    
    // Checken, ob fuer das Konto automatisch Umsaetze angelegt werden sollen
    SynchronizeOptions options = new SynchronizeOptions(gegenkonto);
    if (!options.getSyncOffline())
      return;

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