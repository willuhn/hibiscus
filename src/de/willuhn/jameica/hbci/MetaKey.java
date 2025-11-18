/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.io.File;
import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.KontoauszugInterval;
import de.willuhn.jameica.hbci.server.AuslandsUeberweisungTyp;
import de.willuhn.jameica.system.Application;

/**
 * Listet bekannte Meta-Keys, die zu Fachobjekten gespeichert werden koennen.
 */
public enum MetaKey
{
  @SuppressWarnings("javadoc") REMINDER_UUID("reminder.uuid",        null, null),
  @SuppressWarnings("javadoc") REMINDER_TEMPLATE("reminder.template",null, null),
  @SuppressWarnings("javadoc") REMINDER_CREATOR("reminder.creator",  null, "unknown"),
  @SuppressWarnings("javadoc") ADDRESS_ID("address.id",              null, null),
  
  /**
   * SEPA-Mandatsreferenz.
   */
  SEPA_MANDATE_ID("sepa.mandateid", "SEPA-Mandatsreferenz",null),
  
  /**
   * Signatur-Datum des Mandats.
   */
  SEPA_MANDATE_SIGDATE("sepa.sigdate", "Signatur-Datum des Mandats",null),
  
  /**
   * SEPA-Lastschrift Sequenz-Typ.
   */
  SEPA_SEQUENCE_CODE("sepa.sequencetype", "Signatur-Datum des Mandats",null),

  /**
   * Gläubiger-Identifikation.
   */
  SEPA_CREDITOR_ID("sepa.creditor.id", "Gläubiger-Identifikation",null),
  
  /**
   * Batch-Book-Flag.
   */
  SEPA_BATCHBOOK("sepa.batchbook", "Umsätze dieses Sammelauftrages",null),

  /**
   * Soll das Abrufen der Kontoauszuege auch dann erlaubt werden, wenn PDF nicht unterstuetzt wird?
   */
  KONTOAUSZUG_IGNORE_FORMAT("kontoauszug.ignoreformat","Auch abrufen, wenn Kontoauszug nicht unterstützt oder kein PDF-Format angeboten wird","false"),

  /**
   * Sollen Kontoauszuege beim Abruf sofort als gelesen markiert werden?
   */
  KONTOAUSZUG_MARK_READ("kontoauszug.markread","Automatisch als gelesen markieren","false"),

  /**
   * Sollen die Empfangsquittungen automatisch gesendet werden?
   */
  KONTOAUSZUG_SEND_RECEIPT("kontoauszug.send.receipt","Automatisch Empfangsquittung an Bank senden","true"),

  /**
   * Abruf-Intervall fuer die Kontoauszuege im PDF-Format.
   */
  KONTOAUSZUG_INTERVAL("kontoauszug.interval","Intervall",KontoauszugInterval.DEFAULT.getId()),
  
  /**
   * Datum des letzten Abrufs der Kontoauszuege im PDF-Format.
   */
  KONTOAUSZUG_INTERVAL_LAST("kontoauszug.interval.last","Datum des letzten Abrufs",null),

  /**
   * True, wenn die Kontoauszuege per Messaging-Plugin gespeichert werden sollen.
   */
  KONTOAUSZUG_STORE_MESSAGING("kontoauszug.store.messaging","Kontoauszüge per Jameica Messaging speichern","false"),

  /**
   * Ordner, in dem die Kontoauszuege erstellt werden sollen.
   */
  KONTOAUSZUG_STORE_PATH("kontoauszug.store.path","Kontoauszüge speichern in",Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() + File.separator + "kontoauszuege"),

  /**
   * Template fuer den Unter-Ordner.
   */
  KONTOAUSZUG_TEMPLATE_PATH("kontoauszug.template.path","Vorlage für Unterordner","${iban}" + File.separator + "${jahr}"),

  /**
   * Template fuer den Dateinamen.
   */
  KONTOAUSZUG_TEMPLATE_NAME("kontoauszug.template.name","Vorlage für Dateinamen","${jahr}-${nummer}"),
  
  /**
   * Legt fest, ob CAMT fuer den Umsatz-Abruf verwendet werden soll.
   */
  UMSATZ_CAMT("umsatz.camt","CAMT-Format für Umsatz-Abruf verwenden",null),
  
  /**
   * TAN-Eingabe abgebrochen
   */
  TAN_CANCEL("sync.tan-cancel","TAN-Eingabe abgebrochen",null),
  
  /**
   * ID der Duplizierungsvorlage.
   */
  DUPLICATE_ID("duplicate.id","Duplizierungsvorlage",null),
  
  /**
   * True, wenn die Vorauswahl des Auftragstyps im Konto gespeichert werden soll.
   */
  UEBERWEISUNG_TYP_SAVE("ueberweisung.typ.save","Vorauswahl des Auftragstyps speichern",null),

  /**
   * Vorauswahl des Auftragstyps.
   */
  UEBERWEISUNG_TYP("ueberweisung.typ","Vorauswahl des Auftragstyps",AuslandsUeberweisungTyp.INSTANT.name()),


  ;

  private String name         = null;
  private String description  = null;
  private String defaultValue = null;
  
  /**
   * ct.
   * @param name
   * @param description
   * @param defaultValue
   */
  private MetaKey(String name, String description, String defaultValue)
  {
    this.name         = name;
    this.description  = description;
    this.defaultValue = defaultValue;
  }
  
  /**
   * Liefert den Namen des Meta-Keys.
   * @return der Name des Meta-Keys.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Liefert einen optionalen Beschreibungstext zu dem Meta-Key.
   * @return optionaler Beschreibungstext zu dem Meta-Key.
   */
  public String getDescription()
  {
    return this.description;
  }
  
  /**
   * Liefert den Default-Wert.
   * @return der Default-Wert.
   */
  public String getDefault()
  {
    return this.defaultValue;
  }
  
  /**
   * Liefert den Wert des Meta-Keys fuer das Objekt.
   * @param o das Objekt.
   * @return der Wert des Meta-Keys oder der Default-Wert, wenn kein Wert existiert.
   * @throws RemoteException
   */
  public String get(HibiscusDBObject o) throws RemoteException
  {
    return this.get(o,null);
  }
  
  /**
   * Speichert den Wert des Meta-Keys fuer das Objekt.
   * @param o das Objekt.
   * @param value der Wert des Meta-Keys.
   * @throws RemoteException
   */
  public void set(HibiscusDBObject o, String value) throws RemoteException
  {
    this.set(o,null,value);
  }
  
  /**
   * Liefert den Wert des Meta-Keys fuer das Objekt.
   * @param o das Objekt.
   * @param suffix optionaler Suffix, um verschiedene Varianten des Meta-Key verwenden zu koennen.
   * @return der Wert des Meta-Keys oder der Default-Wert, wenn kein Wert existiert.
   * @throws RemoteException
   */
  public String get(HibiscusDBObject o, String suffix) throws RemoteException
  {
    String key = this.name;
    if (suffix != null)
      key = key + "." + suffix;
    return o != null ? o.getMeta(key,this.defaultValue) : this.defaultValue;
  }
  
  /**
   * Speichert den Wert des Meta-Keys fuer das Objekt.
   * @param o das Objekt.
   * @param suffix optionaler Suffix, um verschiedene Varianten des Meta-Key verwenden zu koennen.
   * @param value der Wert des Meta-Keys.
   * @throws RemoteException
   */
  public void set(HibiscusDBObject o, String suffix, String value) throws RemoteException
  {
    String key = this.name;
    if (suffix != null)
      key = key + "." + suffix;
    o.setMeta(key,value);
  }

}


