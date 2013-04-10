/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize;

import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;


/**
 * Interface fuer ein Backend, welches Bank-Geschaeftsvorfaelle ausfuehren kann.
 * Die Standard-Implementierung von Hibiscus verwendet HBCI. Es koennen aber
 * weitere hinzugefuegt werden. Eine weitere verwendet z.Bsp. die Scripting-Funktionen.
 */
public interface SynchronizeBackend
{
  /**
   * Queue, an die der aktuelle Prozess-Status der Synchronisierung (RUNNING, ERROR, DONE, CANCEL) geschickt wird.
   */
  public final static String QUEUE_STATUS = "hibiscus.sync.status";
  
  /**
   * Queue, die im Fehlerfall benachrichtigt wird. Die Message wird synchron als QueryMessage
   * geschickt. Enthaelt sie in getData() als Rueckgabe-Wert TRUE, wird die Synchronisierung
   * trotz Fehler nur in diesem Fall beim naechsten Konto dennoch fortgesetzt. Andernfalls
   * wird sie abgebrochen.
   */
  public final static String QUEUE_ERROR  = "hibiscus.sync.error";
  
  /**
   * Liefert true, wenn das Backend den angegebenen Job-Typ fuer das angegebene Konto unterstuetzt.
   * @param type der zu pruefende Job-Typ.
   * @param konto das Konto.
   * @return true, wenn es ihn unterstuetzt, sonst false.
   */
  public boolean supports(Class<? extends SynchronizeJob> type, Konto konto);
  
  /**
   * Liefert eine Instanz des angegebenen Job-Typs.
   * @param type der zu erstellende Job-Typ.
   * @param konto das Konto.
   * @return die Instanz des Jobs.
   * @throws ApplicationException bei einem Anwendungsfehler.
   */
  public <T> T create(Class<? extends SynchronizeJob> type, Konto konto) throws ApplicationException;

  /**
   * Liefert eine Liste der auszufuehrenden Synchronisierungsjobs auf dem angegebenen Konto.
   * @param k das Konto.
   * Wenn kein Konto angegeben ist, werden die Jobs aller Konten zurueckgeliefert.
   * @return Liste der auszufuehrenden Jobs.
   * Die Funktion darf auch NULL liefern.
   */
  public List<SynchronizeJob> getSynchronizeJobs(Konto k);

  /**
   * Fuehrt die uebergebenen Jobs auf dem Backend aus.
   * @param jobs die auszufuehrenden Jobs.
   * @return die neue Session.
   * @throws ApplicationException bei einem Anwendungsfehler.
   * @throws OperationCanceledException wenn der User die Synchronisierung abgebrochen hat.
   */
  public SynchronizeSession execute(List<SynchronizeJob> jobs) throws ApplicationException, OperationCanceledException;

  /**
   * Liefert die aktuelle Session oder NULL wenn gerade keine laeuft.
   * @return die aktuelle Session oder NULL wenn gerade keine laeuft.
   */
  public SynchronizeSession getCurrentSession();
  
  /**
   * Liefert einen sprechenden Namen fuer das Backend.
   * @return sprechender Name fuer das Backend.
   */
  public String getName();
}


