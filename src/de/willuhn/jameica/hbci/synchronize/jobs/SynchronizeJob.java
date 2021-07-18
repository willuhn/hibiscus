/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.jobs;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer einen einzelnen Synchronisierungs-Job.
 */
public interface SynchronizeJob
{
  /**
   * Der Context-Name fuer das Fachobjekt.
   */
  public final static String CTX_ENTITY = "ctx.entity";

  /**
   * Liefert einen sprechenden Namen fuer den Job.
   * @return sprechender Name.
   * @throws ApplicationException
   */
  public String getName() throws ApplicationException;

  /**
   * Oeffnet den Synchronisierungs-Job zur Konfiguration.
   * @throws ApplicationException
   */
  public void configure() throws ApplicationException;

  /**
   * Prueft, ob es sich um einen wiederkehrenden Job handelt.
   * Saldo- und Umsatzabfragen sind zBsp wiederkehrend, Ueberweisungen
   * jedoch nicht.
   * BUGZILLA 583
   * @return true, wenn es sich um einen wiederholenden Job handelt.
   */
  public boolean isRecurring();

  /**
   * Liefert das Konto, ueber welches der Job abgewickelt werden soll.
   * @return das Konto.
   */
  public Konto getKonto();

  /**
   * Speichert das Konto, ueber welches der Job abgewickelt werden soll.
   * @param konto das Konto.
   */
  public void setKonto(Konto konto);

  /**
   * Speichert weitere Context-Informationen.
   * Das kann z.Bsp. das Fachobjekt selbst sein aber auch Addon-Infos
   * wie ein Zieldatum.
   * @param key Schluessel-Name des Context-Elements.
   * @param value Wert des Context-Elements.
   */
  public void setContext(String key, Object value);

  /**
   * Liefert den Wert der angegebenen Context-Information.
   * @param key der Schluessel-Name des Context-Elements.
   * @return der Wert des Context-Elements.
   */
  public Object getContext(String key);

}
