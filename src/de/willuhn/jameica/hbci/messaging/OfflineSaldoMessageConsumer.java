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

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;

/**
 * Pr�ft eingehende oder geloeschte Ums�tze, ob sich diese auf ein Offline-Konto
 * beziehen und aktualisiert den Saldo in das Offline-Konto.
 */
public class OfflineSaldoMessageConsumer implements MessageConsumer
{
  @Override
  public boolean autoRegister()
  {
    // Wird manuell per Manifest registriert, um die Reihenfolge festzulegen. Muss VOR CheckOfflineUmsatzMessageConsumer passieren,
    // damit sichergestellt ist, dass der Saldo des Kontos aktualisiert wurde, wenn die Auto-Buchung erzeugt wird
    return false;
  }

  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{ImportMessage.class,ObjectDeletedMessage.class};
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    // Wenn es keine Import-Message ist ignorieren wir die folgenden
    if (message == null)
      return;

    // Andernfalls wurde der Umsatz geloescht
    boolean isImport = (message instanceof ImportMessage);
    
    GenericObject o = ((ObjectMessage)message).getObject();
    
    if (!(o instanceof Umsatz))
      return; // interessiert uns nicht
    
    // wir haben einen Umsatz, den es zu bearbeiten gilt...
    Umsatz u = (Umsatz) o;

    Konto k = u.getKonto();
    if (k == null)
      return;
    
    // Offline-Konto?
    if (!k.hasFlag(Konto.FLAG_OFFLINE))
      return;

    // Explizit abgeschaltet. Sie mail von Sebastian vom 09.12.2013
    SynchronizeOptions options = new SynchronizeOptions(k);
    if (!options.getAutoSaldo())
      return;
    
    // Wenn fuer das Offline-Konto das Synchronisieren des Saldos
    // aktiv ist, halten wir uns raus
    // Siehe Mail von Sebastian vom 08.05.2013
    
    // Update 2013-07-23: Das macht aber nur Sinn, wenn Scripting fuer
    // das Konto verfuegbar ist.
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeEngine engine = service.get(SynchronizeEngine.class);
    
    // Also wir haben prinzipiell Scripting fuer das Konto. Also checken,
    // ob das Abrufen des Saldos dort schon aktiviert ist
    if (engine.supports(SynchronizeJobKontoauszug.class,k))
    {
      if (options.getSyncSaldo()) // Das Script holt den Saldo. Also machen wir nichts
        return;
    }

    // Betrag der Buchung
    double betrag = u.getBetrag();
    if (Double.isNaN(betrag))
      return;

    // neuen Saldo ausrechnen
    double saldo = k.getSaldo();
    if (Double.isNaN(saldo))
      saldo = 0.0d;

    // Neuen Saldo uebernehmen
    if (isImport)
      k.setSaldo(saldo + betrag);
    else
      k.setSaldo(saldo - betrag);

    k.store();
    
    // Geaendertes Konto bekanntmachen
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(k));
  }
}

/*******************************************************************************
 * $Log: OfflineSaldoMessageConsumer.java,v $
 * Revision 1.1  2010/11/08 10:37:00  willuhn
 * @N BUGZILLA 945
 *
 ******************************************************************************/