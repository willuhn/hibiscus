/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passport;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.ApplicationException;

/**
 * Bildet die direkte Verbindung zum HBCIHandler aus HBCI4Java ab.
 */
public interface PassportHandle extends Remote
{
  /**
   * Identifier fuer den Persistent-Parameter mit der Config, aus der
   * der Passport erstellt wurde.
   */
  public final static String CONTEXT_CONFIG = "hibiscus.context.config";
  
  /**
   * Identifier fuer den Persistent-Parameter mit ggf geaenderter Kunden- und Benutzerkennung.
   */
  public final static String CONTEXT_USERID_CHANGED = "hibiscus.context.userid.changed";
  
  /**
   * Identifier fuer den Persistent-Parameter der Liste der TAN-Verfahren
   */
  public final static String CONTEXT_SECMECHLIST = "hibiscus.context.secmechlist";

  /**
   * Identifier fuer den Persistent-Parameter mit der Liste der TAN-Medienbezeichnungen.
   */
  public final static String CONTEXT_TANMEDIALIST = "hibiscus.context.tanmedialist";

  /**
   * Oeffnet den Passport und liefert den HBCIHandler zrueck.
   * Diese Funktion wird von der HBCIFactory bei der Ausfuehrung von
   * HBCIJobs verwendet.<br>
   * <b>Hinweis:</b>Die Funktion hat den HBCIHandler fix und fertig
   * vorkonfiguriert auszuliefern. Die HBCIFactory verwendet ihn direkt
   * ohne weitere Parameter zu setzen.
   * @return Handler, der diese Verbindung repraesentiert.
   * @throws RemoteException muss geworfen werden, wenn die Initialisierung fehlschlaegt.
   * Die Exeption sollte einen sinnvollen Fehlertext enthalten.
   * @throws ApplicationException
   */
  public HBCIHandler open() throws RemoteException, ApplicationException;

  /**
   * Schliesst den Passport.
   * Die Funktion wird von der HBCIFactory nach Durchfuehrung
   * der HBCI-Jobs ausgefuehrt. In dieser Funktion sollte der
   * HBCIHandler geschlossen werden.
   * @throws RemoteException
   */
  public void close() throws RemoteException;

  /**
   * Prueft, ob der Passport offen ist.
   * @return true, wenn er offen ist.
   * @throws RemoteException
   */
  public boolean isOpen() throws RemoteException;

  /**
   * Liefert ein Array mit Konto-Objekten, die aus dem Medium gelesen wurden.
   * Es wird niemals <code>null</code> zurueckgeliefert sondern hoechstens ein leeres Array.<br>
   * Hinweis: Die Konten-Objekte duerfen nicht in der Datenbank gespeichert
   * werden. Diese Entscheidung bleibt dem Anwender ueberlassen.
   * Da der HBCIHandler von HBCI4Java ja Konto-Objekte vom Typ
   * <i>org.kapott.hbci.structures.Konto</i> liefert, koennen diese
   * via <i>de.willuhn.jameica.hbci.server.util.Converter.HBCIKonto2JameicaKonto(Konto)</i>
   * in Fachobjekte des HBCI-Plugins konvertiert werden.
   * @return Array mit Konten, die dieser Passport anbietet.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Konto[] getKonten() throws RemoteException, ApplicationException;

  /**
   * Durch Implementierung dieses Callback-Aufrufs, kann sich das Passport-Handle
   * in den HBCICallback einklinken.
   * Konkret wird das u.a. gebraucht, damit verbrauchte TANs gespeichert werden.
   * Diese Funktion wird in der gleichnamigen Funktion von {@link HBCICallbackSWT}
   * aufgerufen.
   * @param passport
   * @param reason
   * @param msg
   * @param datatype
   * @param retData
   * @see org.kapott.hbci.callback.HBCICallback#callback(org.kapott.hbci.passport.HBCIPassport, int, java.lang.String, int, java.lang.StringBuffer)
   * @return true, wenn der Handler den Callback behandeln konnte.
   * @throws Exception
   */
  public boolean callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) throws Exception;

}


/**********************************************************************
 * $Log: PassportHandle.java,v $
 * Revision 1.6  2010/09/08 15:04:53  willuhn
 * @N Config des Sicherheitsmediums als Context in Passport speichern
 *
 * Revision 1.5  2006/08/06 13:26:48  willuhn
 * @B bug 257
 *
 * Revision 1.4  2006/08/03 15:32:35  willuhn
 * @N Bug 62
 *
 * Revision 1.3  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.2  2004/10/19 23:40:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/